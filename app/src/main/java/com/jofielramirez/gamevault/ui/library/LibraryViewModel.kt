package com.jofielramirez.gamevault.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jofielramirez.gamevault.data.SampleLibrary
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.data.repository.GameRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    /** Games after search, filters and sorting. */
    val games: List<Game> = emptyList(),
    /** Size of the whole library, to tell "empty library" apart from "nothing matches". */
    val totalGames: Int = 0,
    val query: String = "",
    val filter: LibraryFilter = LibraryFilter(),
    val availablePlatforms: List<Platform> = emptyList(),
    val availableGenres: List<String> = emptyList(),
    val isLoading: Boolean = true
) {
    val isLibraryEmpty: Boolean get() = !isLoading && totalGames == 0
}

class LibraryViewModel(private val repository: GameRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(LibraryFilter())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val matchingGames = query.flatMapLatest { q ->
        if (q.isBlank()) repository.observeGames() else repository.searchGames(q)
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        matchingGames, repository.observeGames(), filter, query
    ) { matches, allGames, currentFilter, q ->
        LibraryUiState(
            games = LibraryFilters.apply(matches, currentFilter),
            totalGames = allGames.size,
            query = q,
            filter = currentFilter,
            availablePlatforms = LibraryFilters.platformsOf(allGames),
            availableGenres = LibraryFilters.genresOf(allGames),
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun onStatusFilterChange(status: GameStatus?) = filter.update { it.copy(status = status) }
    fun onPlatformFilterChange(platform: Platform?) = filter.update { it.copy(platform = platform) }
    fun onGenreFilterChange(genre: String?) = filter.update { it.copy(genre = genre) }
    fun onSortChange(sort: SortOrder) = filter.update { it.copy(sort = sort) }
    fun clearFilters() = filter.update { LibraryFilter(sort = it.sort) }

    fun loadSampleLibrary() {
        viewModelScope.launch { SampleLibrary.populate(repository) }
    }
}
