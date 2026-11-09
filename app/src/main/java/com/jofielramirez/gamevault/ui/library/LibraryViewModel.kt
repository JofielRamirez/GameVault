package com.jofielramirez.gamevault.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jofielramirez.gamevault.data.SampleLibrary
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.LibrarySummary
import com.jofielramirez.gamevault.data.repository.GameRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryUiState(
    val games: List<Game> = emptyList(),
    val summary: LibrarySummary = LibrarySummary(0, 0, 0, 0.0, 0.0),
    val query: String = "",
    val statusFilter: GameStatus? = null,
    val isLoading: Boolean = true
) {
    /** True only when the user has no games at all (not when a filter hides them). */
    val isLibraryEmpty: Boolean get() = !isLoading && summary.totalGames == 0
}

class LibraryViewModel(private val repository: GameRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val statusFilter = MutableStateFlow<GameStatus?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val matchingGames = query.flatMapLatest { q ->
        if (q.isBlank()) repository.observeGames() else repository.searchGames(q)
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        matchingGames, statusFilter, repository.observeSummary(), query
    ) { games, filter, summary, q ->
        LibraryUiState(
            games = if (filter == null) games else games.filter { it.status == filter },
            summary = summary,
            query = q,
            statusFilter = filter,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun onStatusFilterChange(status: GameStatus?) {
        statusFilter.value = status
    }

    fun loadSampleLibrary() {
        viewModelScope.launch { SampleLibrary.populate(repository) }
    }
}
