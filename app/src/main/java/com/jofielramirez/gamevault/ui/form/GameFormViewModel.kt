package com.jofielramirez.gamevault.ui.form

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.data.model.ProgressRules
import com.jofielramirez.gamevault.data.repository.GameRepository
import com.jofielramirez.gamevault.ui.navigation.GameVaultDestinations
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
            genre = genre.trim(),
            releaseYear = releaseYear.toIntOrNull(),
            rating = rating,
            hoursPlayed = hoursPlayed.replace(',', '.').toDoubleOrNull() ?: 0.0,
            completionPercent = completionPercent,
            status = status,
            notes = notes.trim()
        )
        return base?.let {
            it.copy(
                title = values.title, platform = values.platform, genre = values.genre,
                releaseYear = values.releaseYear, rating = values.rating, hoursPlayed = values.hoursPlayed,
                completionPercent = values.completionPercent, status = values.status, notes = values.notes
            )
        } ?: values
    }

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
            notes = game.notes
        )

        private fun formatHoursInput(hours: Double): String = when {
            hours == 0.0 -> ""
            hours % 1.0 == 0.0 -> hours.toInt().toString()
            else -> hours.toString()
        }
    }
}

class GameFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: GameRepository
) : ViewModel() {

    private val gameId: Long? = savedStateHandle.get<Long>(GameVaultDestinations.GAME_ID_ARG)
    private var originalGame: Game? = null

    val isEditing: Boolean = gameId != null

    var formState by mutableStateOf(GameFormState())
        private set

    init {
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
        formState = newState.copy(status = status, completionPercent = progress)
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
}
