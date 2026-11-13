package com.jofielramirez.gamevault.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jofielramirez.gamevault.data.SampleLibrary
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.LibrarySummary
import com.jofielramirez.gamevault.data.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val summary: LibrarySummary = LibrarySummary(0, 0, 0, 0.0, 0.0),
    val continuePlaying: List<Game> = emptyList(),
    val recentlyUpdated: List<Game> = emptyList(),
    val isLoading: Boolean = true
) {
    val isLibraryEmpty: Boolean get() = !isLoading && summary.totalGames == 0
}

class HomeViewModel(private val repository: GameRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeSummary(),
        repository.observeGamesByStatus(GameStatus.PLAYING),
        repository.observeRecentlyUpdated(limit = RECENT_LIMIT)
    ) { summary, playing, recent ->
        HomeUiState(
            summary = summary,
            continuePlaying = playing,
            recentlyUpdated = recent,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun loadSampleLibrary() {
        viewModelScope.launch { SampleLibrary.populate(repository) }
    }

    companion object {
        const val RECENT_LIMIT = 4
    }
}
