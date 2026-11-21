package com.jofielramirez.gamevault.integration

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jofielramirez.gamevault.CrashReporter
import com.jofielramirez.gamevault.data.local.GameVaultDatabase
import com.jofielramirez.gamevault.data.model.Achievement
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.data.model.PlaySession
import com.jofielramirez.gamevault.data.preferences.InMemoryUserPreferencesRepository
import com.jofielramirez.gamevault.data.preferences.ThemeMode
import com.jofielramirez.gamevault.data.preferences.UserPreferences
import com.jofielramirez.gamevault.data.remote.RawgMapper
import com.jofielramirez.gamevault.data.repository.GameSearchRepository
import com.jofielramirez.gamevault.data.repository.OfflineGameRepository
import com.jofielramirez.gamevault.ui.form.GameFormState
import com.jofielramirez.gamevault.ui.form.GameFormViewModel
import com.jofielramirez.gamevault.ui.library.LibraryViewModel
import com.jofielramirez.gamevault.ui.library.SortOrder
import com.jofielramirez.gamevault.ui.settings.SettingsViewModel
import com.jofielramirez.gamevault.ui.stats.StatsCalculations
import com.jofielramirez.gamevault.ui.stats.StatsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Calendar

/** Settings, default sort, statistics by genre and month, form restore and platform mapping. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class FinalFeaturesTest {

    private lateinit var db: GameVaultDatabase
    private lateinit var repository: OfflineGameRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GameVaultDatabase::class.java).build()
        repository = OfflineGameRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    private fun millis(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply { clear(); set(year, month - 1, day, 12, 0) }.timeInMillis

    @Test
    fun settings_changeThemeAndDefaultSort_andDeleteEverything() = runTest {
        val prefs = InMemoryUserPreferencesRepository()
        val id = repository.addGame(Game(title = "Hades", platform = Platform.PC))
        repository.addAchievement(Achievement(gameId = id, title = "Escape"))
        val vm = SettingsViewModel(prefs, repository, GameSearchRepository.Unavailable, CrashReporter.None, null)
        backgroundScope.launch { vm.uiState.collect {} }
        vm.uiState.first { it.totalGames == 1 }

        vm.setThemeMode(ThemeMode.DARK)
        vm.setDefaultSort(SortOrder.PLAYTIME)
        vm.deleteAllGames()

        assertEquals(UserPreferences(ThemeMode.DARK, SortOrder.PLAYTIME), prefs.preferences.first())
        assertEquals(0, vm.uiState.first { it.totalGames == 0 }.totalGames)
        assertTrue(db.achievementDao().observeForGame(id).first().isEmpty())
    }

    @Test
    fun library_opensWithTheSortChosenInSettings() = runTest {
        repository.addGame(Game(title = "A short one", platform = Platform.PC, hoursPlayed = 2.0))
        repository.addGame(Game(title = "Zelda", platform = Platform.NINTENDO_SWITCH, hoursPlayed = 50.0))
        val prefs = InMemoryUserPreferencesRepository(UserPreferences(defaultSort = SortOrder.PLAYTIME))
        val library = LibraryViewModel(repository, prefs)
        backgroundScope.launch { library.uiState.collect {} }

        val state = library.uiState.first { it.filter.sort == SortOrder.PLAYTIME && it.games.size == 2 }

        assertEquals(listOf("Zelda", "A short one"), state.games.map { it.title })
    }

    @Test
    fun statistics_countGenresAndHoursPerMonth() = runTest {
        val now = millis(2026, 11, 21)
        val hades = repository.addGame(Game(title = "Hades", platform = Platform.PC, genre = "Adventure"))
        repository.addGame(Game(title = "Celeste", platform = Platform.PC, genre = "Platformer"))
        repository.addGame(Game(title = "Zelda", platform = Platform.NINTENDO_SWITCH, genre = "adventure"))
        repository.logPlaySession(PlaySession(gameId = hades, playedOn = millis(2026, 11, 2), durationHours = 2.0))
        repository.logPlaySession(PlaySession(gameId = hades, playedOn = millis(2026, 11, 20), durationHours = 1.5))
        repository.logPlaySession(PlaySession(gameId = hades, playedOn = millis(2026, 9, 10), durationHours = 4.0))
        repository.logPlaySession(PlaySession(gameId = hades, playedOn = millis(2026, 1, 10), durationHours = 9.0)) // too old
        val stats = StatsViewModel(repository, now)
        backgroundScope.launch { stats.uiState.collect {} }

        val state = stats.uiState.first { !it.isLoading && it.byGenre.isNotEmpty() && it.monthlyHours.isNotEmpty() }

        // "Adventure" and "adventure" are one genre
        assertEquals(2, state.byGenre.first().count)
        assertEquals(listOf("Jun", "Jul", "Aug", "Sep", "Oct", "Nov"), state.monthlyHours.map { it.label })
        assertEquals(listOf(0.0, 0.0, 0.0, 4.0, 0.0, 3.5), state.monthlyHours.map { it.hours })
    }

    @Test
    fun startOfMonth_handlesTheYearChange() {
        val january = millis(2027, 1, 15)
        val december = Calendar.getInstance().apply { timeInMillis = StatsCalculations.startOfMonth(january, 1) }
        assertEquals(2026, december.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, december.get(Calendar.MONTH))
        assertEquals(1, december.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun form_restoresWhatWasTypedAfterTheAppIsClosedInTheBackground() = runTest {
        val handle = SavedStateHandle()
        val first = GameFormViewModel(handle, repository)
        first.updateForm(GameFormState(title = "Celeste", genre = "Platformer", releaseYear = "2018", rating = 4))

        // Android recreates the screen with the same SavedStateHandle contents
        val restored = GameFormViewModel(SavedStateHandle(handle.keys().associateWith { handle.get<Any>(it) }), repository)

        assertEquals("Celeste", restored.formState.title)
        assertEquals("2018", restored.formState.releaseYear)
        assertEquals(4, restored.formState.rating)
    }

    @Test
    fun nintendoGames_areOnlySwitchWhenRawgListsASwitchRelease() {
        assertEquals(listOf(Platform.NINTENDO_SWITCH), RawgMapper.platforms(listOf("nintendo"), listOf("nintendo-switch")))
        assertEquals(listOf(Platform.OTHER), RawgMapper.platforms(listOf("nintendo"), listOf("wii", "nintendo-3ds")))
        // Without the exact consoles, Nintendo still means Switch
        assertEquals(listOf(Platform.NINTENDO_SWITCH), RawgMapper.platforms(listOf("nintendo")))
    }
}
