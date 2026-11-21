package com.jofielramirez.gamevault.integration

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jofielramirez.gamevault.data.SampleLibrary
import com.jofielramirez.gamevault.data.local.GameVaultDatabase
import com.jofielramirez.gamevault.data.model.Achievement
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.data.remote.RawgApi
import com.jofielramirez.gamevault.data.remote.RawgSearchResponse
import com.jofielramirez.gamevault.data.repository.GameSearchException
import com.jofielramirez.gamevault.data.repository.OfflineGameRepository
import com.jofielramirez.gamevault.data.repository.RawgGameSearchRepository
import com.jofielramirez.gamevault.data.repository.SearchFailure
import com.jofielramirez.gamevault.ui.details.GameDetailsViewModel
import com.jofielramirez.gamevault.ui.home.HomeViewModel
import com.jofielramirez.gamevault.ui.library.LibraryViewModel
import com.jofielramirez.gamevault.ui.navigation.GameVaultDestinations
import com.jofielramirez.gamevault.ui.stats.StatsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Integration tests below the UI: real ViewModels + real repository + Room database.
 * A change made from one screen's ViewModel must show up in the state of the other screens.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class DataFlowIntegrationTest {

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

    private fun details(gameId: Long) =
        GameDetailsViewModel(SavedStateHandle(mapOf(GameVaultDestinations.GAME_ID_ARG to gameId)), repository)

    @Test
    fun loggingASession_updatesHomeLibraryAndStatistics() = runTest {
        val id = repository.addGame(Game(title = "Hades", platform = Platform.PC, genre = "Adventure"))
        val home = HomeViewModel(repository)
        val library = LibraryViewModel(repository)
        val stats = StatsViewModel(repository)
        val detailsVm = details(id)
        // Screens collect their state while visible; do the same here
        backgroundScope.launch { home.uiState.collect {} }
        backgroundScope.launch { library.uiState.collect {} }
        backgroundScope.launch { stats.uiState.collect {} }
        backgroundScope.launch { detailsVm.uiState.collect {} }
        detailsVm.uiState.first { it.details != null }

        detailsVm.logSession(hours = 2.5, progressAfter = 30)

        // Details: hours added, "Want to play" became "Playing", session in the history
        val updated = detailsVm.uiState.first { it.details?.game?.hoursPlayed == 2.5 }.details!!
        assertEquals(GameStatus.PLAYING, updated.game.status)
        assertEquals(30, updated.game.completionPercent)
        assertEquals(1, updated.sessions.size)
        // Home: shown in "Continue playing" and in the totals
        // Home combines three queries; wait until all of them reflect the change
        val homeState = home.uiState.first { it.summary.totalHours == 2.5 && it.continuePlaying.isNotEmpty() }
        assertEquals(listOf("Hades"), homeState.continuePlaying.map { it.title })
        assertEquals(1, homeState.summary.playingGames)
        // Library and Statistics read the same row
        assertEquals(30, library.uiState.first { it.games.firstOrNull()?.completionPercent == 30 }.games.single().completionPercent)
        val statsState = stats.uiState.first { it.summary.totalHours == 2.5 && it.mostPlayed.firstOrNull()?.hoursPlayed == 2.5 }
        assertEquals(listOf("Hades"), statsState.mostPlayed.map { it.title })
    }

    @Test
    fun markingCompleted_movesTheGameOutOfContinuePlaying() = runTest {
        val id = repository.addGame(Game(title = "Celeste", platform = Platform.PC, status = GameStatus.PLAYING, completionPercent = 80))
        val home = HomeViewModel(repository)
        val detailsVm = details(id)
        backgroundScope.launch { home.uiState.collect {} }
        backgroundScope.launch { detailsVm.uiState.collect {} }
        home.uiState.first { it.continuePlaying.size == 1 }
        detailsVm.uiState.first { it.details != null }

        detailsVm.markCompleted()

        val homeState = home.uiState.first { it.summary.completedGames == 1 && it.continuePlaying.isEmpty() }
        assertTrue(homeState.continuePlaying.isEmpty())
        assertEquals(100, repository.getGame(id)!!.completionPercent)
    }

    @Test
    fun deletingAGame_removesItAndItsHistoryEverywhere() = runTest {
        val id = repository.addGame(Game(title = "Halo Infinite", platform = Platform.XBOX, status = GameStatus.PLAYING))
        repository.addGame(Game(title = "Celeste", platform = Platform.PC))
        repository.addAchievement(Achievement(gameId = id, title = "Finish the campaign"))
        val library = LibraryViewModel(repository)
        val stats = StatsViewModel(repository)
        val detailsVm = details(id)
        backgroundScope.launch { library.uiState.collect {} }
        backgroundScope.launch { stats.uiState.collect {} }
        backgroundScope.launch { detailsVm.uiState.collect {} }
        detailsVm.uiState.first { it.details != null }

        detailsVm.deleteGame()

        assertEquals(listOf("Celeste"), library.uiState.first { it.totalGames == 1 && it.games.size == 1 }.games.map { it.title })
        assertEquals(1, stats.uiState.first { it.summary.totalGames == 1 }.summary.totalGames)
        // The details screen now knows the game is gone (it shows "Game not found")
        val gone = detailsVm.uiState.first { !it.isLoading && it.details == null }
        assertFalse(gone.isLoading)
        assertTrue(db.achievementDao().observeForGame(id).first().isEmpty())
    }

    @Test
    fun sampleLibrary_requestedTwiceQuickly_isLoadedOnce() = runTest {
        // Bug found in testing: a double tap on "Explore with sample games" added 14 games
        val first = async { SampleLibrary.populate(repository) }
        val second = async { SampleLibrary.populate(repository) }
        first.await(); second.await()

        val games = repository.observeGames().first()
        assertEquals(7, games.size)
        // Samples use RAWG genres and covers, like games added through the search
        assertTrue(games.all { it.coverUri != null && it.rawgId != null })
        assertEquals(listOf("Action", "Adventure", "Platformer", "Shooter"), games.map { it.genre }.distinct().sorted())
    }

    // ---------------------------------------------------------------- RAWG errors (fake API, no network)

    private fun repositoryThrowing(error: Exception) = RawgGameSearchRepository(object : RawgApi {
        override suspend fun searchGames(query: String, pageSize: Int, precise: Boolean): RawgSearchResponse = throw error
    })

    private suspend fun failureFor(error: Exception): SearchFailure =
        try {
            repositoryThrowing(error).search("zelda")
            fail("Expected the search to fail")
            error("unreachable")
        } catch (e: GameSearchException) {
            e.failure
        }

    private fun httpError(code: Int) = HttpException(Response.error<Any>(code, "{}".toResponseBody()))

    @Test
    fun rawgErrors_areTranslatedForTheForm() = runTest {
        assertEquals(SearchFailure.KEY_REJECTED, failureFor(httpError(401)))
        assertEquals(SearchFailure.SERVER, failureFor(httpError(502)))
        assertEquals(SearchFailure.CONNECTION, failureFor(IOException("timeout")))
    }
}
