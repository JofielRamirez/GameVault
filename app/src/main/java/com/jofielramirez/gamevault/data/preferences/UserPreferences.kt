package com.jofielramirez.gamevault.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jofielramirez.gamevault.ui.library.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

enum class ThemeMode(val label: String) {
    SYSTEM("Same as the phone"),
    LIGHT("Light"),
    DARK("Dark")
}

/** Small settings the user chooses once and expects the app to remember. */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Order the library uses when it opens. */
    val defaultSort: SortOrder = SortOrder.TITLE,
    /** Lets the user turn off anonymous crash reports (Firebase Crashlytics). */
    val crashReportsEnabled: Boolean = true
)

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDefaultSort(sort: SortOrder)
    suspend fun setCrashReportsEnabled(enabled: Boolean)
}

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Stores the settings with Jetpack DataStore (a small key-value file in the app's private storage). */
class DataStoreUserPreferencesRepository(context: Context) : UserPreferencesRepository {

    private val dataStore = context.applicationContext.settingsDataStore

    override val preferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            themeMode = prefs[THEME]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } } ?: ThemeMode.SYSTEM,
            defaultSort = prefs[SORT]?.let { name -> SortOrder.entries.firstOrNull { it.name == name } } ?: SortOrder.TITLE,
            crashReportsEnabled = prefs[CRASH_REPORTS] ?: true
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME] = mode.name }
    }

    override suspend fun setDefaultSort(sort: SortOrder) {
        dataStore.edit { it[SORT] = sort.name }
    }

    override suspend fun setCrashReportsEnabled(enabled: Boolean) {
        dataStore.edit { it[CRASH_REPORTS] = enabled }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme_mode")
        val SORT = stringPreferencesKey("default_sort")
        val CRASH_REPORTS = booleanPreferencesKey("crash_reports")
    }
}

/** Keeps the settings in memory only; used by tests. */
class InMemoryUserPreferencesRepository(initial: UserPreferences = UserPreferences()) : UserPreferencesRepository {
    private val state = MutableStateFlow(initial)
    override val preferences: Flow<UserPreferences> = state
    override suspend fun setThemeMode(mode: ThemeMode) = state.update { it.copy(themeMode = mode) }
    override suspend fun setDefaultSort(sort: SortOrder) = state.update { it.copy(defaultSort = sort) }
    override suspend fun setCrashReportsEnabled(enabled: Boolean) = state.update { it.copy(crashReportsEnabled = enabled) }
}
