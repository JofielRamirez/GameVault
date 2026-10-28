package com.jofiel.gamevault.ui.form

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jofiel.gamevault.data.Game
import com.jofiel.gamevault.data.GameRepository
import com.jofiel.gamevault.data.GameStatus
import com.jofiel.gamevault.data.Platform
import kotlinx.coroutines.launch

/**
 * Backs the Add / Edit form. The same screen creates a game (no id) or edits one (id from navigation).
 */
class GameFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: GameRepository,
) : ViewModel() {

    private val gameId: Long = savedStateHandle.get<Long>(GAME_ID_ARG) ?: NEW_GAME_ID
    private var loadedGame: Game? = null

    var state by mutableStateOf(GameFormState())
        private set

    var errors by mutableStateOf(GameFormErrors())
        private set

    var isLoading by mutableStateOf(gameId != NEW_GAME_ID)
        private set

    var isSaving by mutableStateOf(false)
        private set

    init {
        if (gameId != NEW_GAME_ID) {
            viewModelScope.launch {
                val game = repository.getGame(gameId)
                loadedGame = game
                if (game != null) state = game.toFormState()
                isLoading = false
            }
        }
    }

    fun onTitleChange(value: String) {
        state = state.copy(title = value)
        if (errors.title != null) errors = errors.copy(title = null)
    }

    fun onPlatformChange(value: Platform) {
        state = state.copy(platform = value)
    }

    fun onGenreChange(value: String) {
        state = state.copy(genre = value)
    }

    fun onStatusChange(value: GameStatus) {
        // Marking a game as completed fills the progress bar automatically.
        val progress = if (value == GameStatus.COMPLETED) 100 else state.progress
        state = state.copy(status = value, progress = progress)
    }

    fun onHoursChange(value: String) {
        // Digits only, so the keyboard can't produce negative or decimal values.
        if (value.length <= 5 && value.all { it.isDigit() }) {
            state = state.copy(hoursPlayed = value)
            if (errors.hours != null) errors = errors.copy(hours = null)
        }
    }

    fun onProgressChange(value: Int) {
        state = state.copy(progress = value.coerceIn(0, 100))
    }

    fun onRatingChange(value: Int) {
        state = state.copy(rating = value.coerceIn(0, 5))
    }

    fun onNotesChange(value: String) {
        state = state.copy(notes = value)
    }

    fun save(onSaved: () -> Unit) {
        if (isSaving) return
        val validation = GameFormValidator.validate(state)
        errors = validation
        if (validation.hasErrors) return

        isSaving = true
        viewModelScope.launch {
            repository.save(state.toGame())
            isSaving = false
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val game = loadedGame ?: return
        viewModelScope.launch {
            repository.delete(game)
            onDeleted()
        }
    }

    companion object {
        const val GAME_ID_ARG = "gameId"
        const val NEW_GAME_ID = -1L
    }
}
