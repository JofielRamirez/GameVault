package com.jofielramirez.gamevault.ui.form

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Genres
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.data.model.ProgressRules
import com.jofielramirez.gamevault.data.remote.GameSuggestion
import com.jofielramirez.gamevault.data.repository.GameRepository
import com.jofielramirez.gamevault.data.repository.GameSearchRepository
import com.jofielramirez.gamevault.ui.navigation.GameVaultDestinations
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** What the user typed in the Add / Edit form. Numbers stay as text until they are saved. */
data class GameFormState(
    val title: String = "",
    val platform: Platform = Platform.PC,
    val genre: String = "",
    val releaseYear: String = "",
    val status: GameStatus = GameStatus.WANT_TO_PLAY,
    val hoursPlayed: String = "",
    val completionPercent: Int = 0,
    val rating: Int = 0,
    val notes: String = "",
    /** Cover image URL filled in from the game search. */
    val coverUrl: String? = null,
    /** RAWG id of the search result the details came from, or null if typed by hand. */
    val rawgId: Int? = null,
    /** Errors are only shown after the first save attempt, so an empty form is not all red. */
    val showErrors: Boolean = false
) {
    val titleError: String? get() = if (title.isBlank()) "Enter the game's title" else null

    val releaseYearError: String?
        get() {
            if (releaseYear.isBlank()) return null
            val year = releaseYear.toIntOrNull()
            return if (year == null || year !in Game.FIRST_RELEASE_YEAR..Game.LAST_RELEASE_YEAR) {
                "Use a year between ${Game.FIRST_RELEASE_YEAR} and ${Game.LAST_RELEASE_YEAR}"
            } else null
        }

    val hoursError: String?
        get() {
            if (hoursPlayed.isBlank()) return null
            val hours = hoursPlayed.replace(',', '.').toDoubleOrNull()
            return if (hours == null || hours < 0) "Enter a number of hours, e.g. 12.5" else null
        }

    val isValid: Boolean get() = titleError == null && releaseYearError == null && hoursError == null

    fun toGame(base: Game?): Game {
        val values = Game(
            title = title.trim(),
            platform = platform,
            genre = Genres.normalize(genre),
            releaseYear = releaseYear.toIntOrNull(),
            rating = rating,
            hoursPlayed = hoursPlayed.replace(',', '.').toDoubleOrNull() ?: 0.0,
            completionPercent = completionPercent,
            status = status,
            coverUri = coverUrl,
            rawgId = rawgId,
            notes = notes.trim()
        )
        return base?.let {
            it.copy(
                title = values.title, platform = values.platform, genre = values.genre,
                releaseYear = values.releaseYear, rating = values.rating, hoursPlayed = values.hoursPlayed,
                completionPercent = values.completionPercent, status = values.status, notes = values.notes,
                coverUri = values.coverUri, rawgId = values.rawgId
            )
        } ?: values
    }

    /**
     * Fills the form with a search result. Progress, rating and notes are the user's own,
     * so they are kept. The platform is kept when the game exists on it; otherwise the
     * first platform the game was released on is chosen.
     */
    fun withSuggestion(suggestion: GameSuggestion): GameFormState = copy(
        title = suggestion.title,
        genre = suggestion.genre.ifBlank { genre },
        releaseYear = suggestion.releaseYear?.toString() ?: releaseYear,
        platform = if (suggestion.platforms.isEmpty() || platform in suggestion.platforms) platform
        else suggestion.platforms.first(),
        coverUrl = suggestion.coverUrl,
        rawgId = suggestion.rawgId
    )

    companion object {
        fun from(game: Game) = GameFormState(
            title = game.title,
            platform = game.platform,
            genre = game.genre,
            releaseYear = game.releaseYear?.toString().orEmpty(),
            status = game.status,
            hoursPlayed = formatHoursInput(game.hoursPlayed),
            completionPercent = game.completionPercent,
            rating = game.rating,
            notes = game.notes,
            coverUrl = game.coverUri,
            rawgId = game.rawgId
        )

        private fun formatHoursInput(hours: Double): String = when {
            hours == 0.0 -> ""
            hours % 1.0 == 0.0 -> hours.toInt().toString()
            else -> hours.toString()
        }
    }
}

/** What the search box under the title shows. */
sealed interface GameSearchState {
    /** Nothing typed yet, or the user already picked a result. */
    data object Idle : GameSearchState
    /** The app was built without a RAWG API key. */
    data object NotConfigured : GameSearchState
    data object Loading : GameSearchState
    data class Results(val query: String, val suggestions: List<GameSuggestion>) : GameSearchState
    data object Error : GameSearchState
}

@OptIn(FlowPreview::class)
class GameFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: GameRepository,
    private val searchRepository: GameSearchRepository = GameSearchRepository.Unavailable
) : ViewModel() {

    private val gameId: Long? = savedStateHandle.get<Long>(GameVaultDestinations.GAME_ID_ARG)
    private var originalGame: Game? = null

    val isEditing: Boolean = gameId != null

    var formState by mutableStateOf(GameFormState())
        private set

    var searchState by mutableStateOf<GameSearchState>(GameSearchState.Idle)
        private set

    /** Title text to look up. Only changes when the user types, not when a game is loaded or picked. */
    private val searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            searchQuery
                // Wait until the user stops typing so every key press is not a web request
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { query -> runSearch(query) }
        }
        if (gameId != null) {
            viewModelScope.launch {
                repository.getGame(gameId)?.let { game ->
                    originalGame = game
                    formState = GameFormState.from(game)
                }
            }
        }
    }

    fun updateForm(newState: GameFormState) {
        // Same rule as the repository, applied live so the user sees status and progress stay in sync
        val (status, progress) = ProgressRules.reconcile(
            previousStatus = formState.status,
            previousProgress = formState.completionPercent,
            status = newState.status,
            progress = newState.completionPercent
        )
        val titleChanged = newState.title != formState.title
        formState = newState.copy(status = status, completionPercent = progress)
        if (titleChanged) searchQuery.value = newState.title.trim()
    }

    /** Copies the chosen search result into the form and closes the result list. */
    fun selectSuggestion(suggestion: GameSuggestion) {
        searchQuery.value = ""
        searchState = GameSearchState.Idle
        formState = formState.withSuggestion(suggestion)
    }

    fun dismissSearch() {
        searchQuery.value = ""
        searchState = GameSearchState.Idle
    }

    /** Removes the cover and the link to the catalog, e.g. when the wrong game was picked. */
    fun clearCover() {
        formState = formState.copy(coverUrl = null, rawgId = null)
    }

    private suspend fun runSearch(query: String) {
        if (query.length < MIN_QUERY_LENGTH) {
            searchState = GameSearchState.Idle
            return
        }
        if (!searchRepository.isAvailable) {
            searchState = GameSearchState.NotConfigured
            return
        }
        searchState = GameSearchState.Loading
        val result = try {
            GameSearchState.Results(query, searchRepository.search(query))
        } catch (e: CancellationException) {
            throw e // the user typed again; collectLatest started a newer search
        } catch (e: Exception) {
            GameSearchState.Error
        }
        // If a result was picked (or the list closed) while the request was running, drop it
        if (searchQuery.value == query) searchState = result
    }

    /** Returns true when the game was saved and the screen can close. */
    suspend fun save(): Boolean {
        if (!formState.isValid) {
            formState = formState.copy(showErrors = true)
            return false
        }
        val game = formState.toGame(originalGame)
        if (originalGame == null) repository.addGame(game) else repository.updateGame(game)
        return true
    }

    companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
        const val MIN_QUERY_LENGTH = 3
    }
}
