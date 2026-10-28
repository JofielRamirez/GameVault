package com.jofiel.gamevault.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jofiel.gamevault.data.Game
import com.jofiel.gamevault.data.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class LibraryUiState(
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = true,
) {
    val totalHours: Int get() = games.sumOf { it.hoursPlayed }
}

/** Exposes the library as state; Room's Flow keeps it updated after every create/edit/delete. */
class LibraryViewModel(repository: GameRepository) : ViewModel() {

    val uiState: StateFlow<LibraryUiState> = repository.observeGames()
        .map { games -> LibraryUiState(games = games, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState(),
        )
}
