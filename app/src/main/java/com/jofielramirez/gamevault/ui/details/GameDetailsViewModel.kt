package com.jofielramirez.gamevault.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jofielramirez.gamevault.data.model.Achievement
import com.jofielramirez.gamevault.data.model.GameDetails
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.PlaySession
import com.jofielramirez.gamevault.data.repository.GameRepository
import com.jofielramirez.gamevault.ui.navigation.GameVaultDestinations
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GameDetailsUiState(
    val details: GameDetails? = null,
    val isLoading: Boolean = true
)

class GameDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: GameRepository
) : ViewModel() {

    private val gameId: Long = checkNotNull(savedStateHandle[GameVaultDestinations.GAME_ID_ARG])

    val uiState: StateFlow<GameDetailsUiState> = repository.observeGameDetails(gameId)
        .map { GameDetailsUiState(details = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameDetailsUiState())

    fun logSession(hours: Double, progressAfter: Int) {
        viewModelScope.launch {
            repository.logPlaySession(
                PlaySession(gameId = gameId, durationHours = hours, progressAfter = progressAfter)
            )
        }
    }

    fun markCompleted() {
        val game = uiState.value.details?.game ?: return
        viewModelScope.launch {
            repository.updateGame(game.copy(status = GameStatus.COMPLETED, completionPercent = 100))
        }
    }

    fun addAchievement(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addAchievement(Achievement(gameId = gameId, title = title.trim()))
        }
    }

    fun toggleAchievement(achievement: Achievement) {
        viewModelScope.launch {
            repository.setAchievementUnlocked(achievement, unlocked = !achievement.isUnlocked)
        }
    }

    suspend fun deleteGame() {
        uiState.value.details?.game?.let { repository.deleteGame(it) }
    }
}
