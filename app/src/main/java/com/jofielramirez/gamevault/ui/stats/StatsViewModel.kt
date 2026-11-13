package com.jofielramirez.gamevault.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.LibrarySummary
import com.jofielramirez.gamevault.data.model.PlatformCount
import com.jofielramirez.gamevault.data.model.StatusCount
import com.jofielramirez.gamevault.data.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.math.roundToInt

data class StatsUiState(
    val summary: LibrarySummary = LibrarySummary(0, 0, 0, 0.0, 0.0),
    val byPlatform: List<PlatformCount> = emptyList(),
    val byStatus: List<StatusCount> = emptyList(),
    val mostPlayed: List<Game> = emptyList(),
    val isLoading: Boolean = true
) {
    val isEmpty: Boolean get() = !isLoading && summary.totalGames == 0

    /** Share of the library that is completed, 0–100. */
    val completionRate: Int
        get() = if (summary.totalGames == 0) 0 else (summary.completedGames * 100.0 / summary.totalGames).roundToInt()
}

class StatsViewModel(repository: GameRepository) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        repository.observeSummary(),
        repository.observeCountByPlatform(),
        repository.observeCountByStatus(),
        repository.observeMostPlayed(limit = MOST_PLAYED_LIMIT)
    ) { summary, byPlatform, byStatus, mostPlayed ->
        StatsUiState(summary, byPlatform, byStatus, mostPlayed, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    companion object {
        const val MOST_PLAYED_LIMIT = 5
    }
}
