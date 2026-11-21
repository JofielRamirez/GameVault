package com.jofielramirez.gamevault.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jofielramirez.gamevault.CrashReporter
import com.jofielramirez.gamevault.data.CoverStorage
import com.jofielramirez.gamevault.data.SampleLibrary
import com.jofielramirez.gamevault.data.preferences.ThemeMode
import com.jofielramirez.gamevault.data.preferences.UserPreferences
import com.jofielramirez.gamevault.data.preferences.UserPreferencesRepository
import com.jofielramirez.gamevault.data.repository.GameRepository
import com.jofielramirez.gamevault.data.repository.GameSearchRepository
import com.jofielramirez.gamevault.ui.library.SortOrder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val totalGames: Int = 0,
    val searchAvailable: Boolean = false,
    val crashReportsAvailable: Boolean = false,
    val isLoading: Boolean = true
)

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val gameRepository: GameRepository,
    searchRepository: GameSearchRepository,
    private val crashReporter: CrashReporter,
    private val coverStorage: CoverStorage?
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.preferences,
        gameRepository.observeSummary()
    ) { prefs, summary ->
        SettingsUiState(
            preferences = prefs,
            totalGames = summary.totalGames,
            searchAvailable = searchRepository.isAvailable,
            crashReportsAvailable = crashReporter.isAvailable,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    /** Short confirmation shown under the "Your data" section after an action. */
    var message by mutableStateOf<String?>(null)
        private set

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setDefaultSort(sort: SortOrder) {
        viewModelScope.launch { preferencesRepository.setDefaultSort(sort) }
    }

    fun setCrashReportsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setCrashReportsEnabled(enabled)
            crashReporter.setEnabled(enabled)
        }
    }

    fun loadSampleLibrary() {
        viewModelScope.launch {
            SampleLibrary.populate(gameRepository)
            message = "Sample games added."
        }
    }

    fun deleteAllGames() {
        viewModelScope.launch {
            gameRepository.deleteAllGames()
            coverStorage?.deleteAll()
            message = "Your library is empty now."
        }
    }
}
