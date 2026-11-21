package com.jofielramirez.gamevault.integration

import android.os.Looper
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.intercept.Interceptor
import coil3.request.ErrorResult
import com.jofielramirez.gamevault.AppContainer
import com.jofielramirez.gamevault.GameVaultApplication
import com.jofielramirez.gamevault.data.local.GameVaultDatabase
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.data.remote.GameSuggestion
import com.jofielramirez.gamevault.data.repository.GameSearchException
import com.jofielramirez.gamevault.data.repository.GameSearchRepository
import com.jofielramirez.gamevault.data.repository.OfflineGameRepository
import com.jofielramirez.gamevault.data.repository.SearchFailure
import com.jofielramirez.gamevault.ui.navigation.GameVaultApp
import com.jofielramirez.gamevault.ui.navigation.GameVaultDestinations
import com.jofielramirez.gamevault.ui.theme.GameVaultTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.IOException
import java.time.Duration

/**
 * End-to-end integration tests of the whole app on the JVM: real navigation, ViewModels,
 * repository and Room (in memory). Only the RAWG catalog is replaced by a fake, so the tests
 * do not depend on the internet or use the API quota.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w411dp-h914dp-xxhdpi")
class AppNavigationIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var db: GameVaultDatabase
    private lateinit var repository: OfflineGameRepository
    private lateinit var navController: NavHostController
    private val catalog = FakeCatalog()

    private class FakeCatalog : GameSearchRepository {
        var results: List<GameSuggestion> = emptyList()
        var failure: Exception? = null
        override val isAvailable = true
        override suspend fun search(query: String): List<GameSuggestion> {
            failure?.let { throw it }
            return results
        }
    }

    private val celeste = GameSuggestion(
        rawgId = 22121, title = "Celeste", releaseYear = 2018, genre = "Platformer",
        platforms = listOf(Platform.PC, Platform.NINTENDO_SWITCH), coverUrl = null
    )

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<GameVaultApplication>()
        db = Room.inMemoryDatabaseBuilder(app, GameVaultDatabase::class.java).build()
        repository = OfflineGameRepository(db)
        app.replaceContainer(AppContainer({ repository }, { catalog }))
        // Covers fail fast instead of downloading images during tests
        SingletonImageLoader.setUnsafe(ImageLoader.Builder(app).components { add(Interceptor { chain -> ErrorResult(null, chain.request, IOException("No network in tests")) }) }.build())
    }

    @After
    fun tearDown() = db.close()

    private fun launchApp() {
        composeRule.setContent {
            navController = rememberNavController()
            GameVaultTheme { GameVaultApp(navController) }
        }
    }

    /** Room and the ViewModels work on other threads; wait until [text] is on screen. */
    private fun waitForText(text: String, substring: Boolean = false) {
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasText(text, substring = substring)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(5_000) { composeRule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }
    }

    /** Lets the 400 ms search debounce pass (delays on the main thread run on Robolectric's clock). */
    private fun passSearchDebounce() {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))
        composeRule.waitForIdle()
    }

    private fun currentRoute() = navController.currentBackStackEntry?.destination?.route

    private fun games(): List<Game> = runBlocking { repository.observeGames().first() }

    // ---------------------------------------------------------------- Main workflow

    @Test
    fun emptyVault_loadSamples_everyTabShowsTheSameLibrary() {
        launchApp()
        waitForText("Your vault is empty")

        composeRule.onNodeWithText("Explore with sample games").performClick()

        waitForText("Continue playing")
        composeRule.onNodeWithText("Games in library").assertIsDisplayed()
        composeRule.onNodeWithText("Library").performClick()
        waitForText("My library")
        composeRule.onNodeWithText("Celeste").assertIsDisplayed()
        composeRule.onNodeWithText("Statistics").performClick()
        waitForText("Games by platform")
        composeRule.onNodeWithText("Library completed").assertIsDisplayed()

        assertEquals(7, games().size)
    }

    @Test
    fun addGameWithSearch_fillsTheFormAndShowsUpOnHome() {
        catalog.results = listOf(celeste)
        launchApp()
        waitForText("Add your first game")
        composeRule.onNodeWithText("Add your first game").performClick()
        assertEquals(GameVaultDestinations.ADD_GAME, currentRoute())

        composeRule.onNodeWithTag("title_field").performTextInput("celes")
        passSearchDebounce()
        waitForTag("suggestion_22121")
        composeRule.onNodeWithText("2018 · Platformer · PC, Nintendo Switch").assertIsDisplayed()
        composeRule.onNodeWithTag("suggestion_22121").performClick()

        // Year and genre were filled in by the search
        composeRule.onNodeWithTag("year_field").assert(hasText("2018"))
        composeRule.onNodeWithTag("genre_field").assert(hasText("Platformer"))
        composeRule.onNodeWithTag("save_button").performScrollTo().performClick()

        waitForText("PC · Platformer")
        assertEquals(GameVaultDestinations.HOME, currentRoute())
        val saved = games().single()
        assertEquals("Celeste", saved.title)
        assertEquals(2018, saved.releaseYear)
        assertEquals(22121, saved.rawgId)
    }

    @Test
    fun failedSearch_explainsTheProblem_andTheGameCanStillBeTypedByHand() {
        catalog.failure = GameSearchException(SearchFailure.CONNECTION, IOException("offline"))
        launchApp()
        waitForText("Add your first game")
        composeRule.onNodeWithText("Add your first game").performClick()

        composeRule.onNodeWithTag("title_field").performTextInput("My Indie Game")
        passSearchDebounce()
        waitForText("Could not reach the game catalog", substring = true)
        composeRule.onNodeWithText("Try again").assertIsDisplayed()

        composeRule.onNodeWithTag("genre_field").performTextInput("rpg")
        composeRule.onNodeWithTag("save_button").performScrollTo().performClick()

        waitForText("My Indie Game")
        assertEquals("RPG", games().single().genre) // stored with the standard spelling
    }

    @Test
    fun invalidInput_keepsTheUserOnTheFormWithClearMessages() {
        launchApp()
        waitForText("Add your first game")
        composeRule.onNodeWithText("Add your first game").performClick()

        composeRule.onNodeWithTag("save_button").performScrollTo().performClick()
        composeRule.onNodeWithText("Enter the game's title").assertIsDisplayed()

        composeRule.onNodeWithTag("title_field").performTextInput("Old Game")
        composeRule.onNodeWithTag("year_field").performTextInput("1800")
        composeRule.onNodeWithText("Use a year between 1950 and 2100").assertIsDisplayed()
        composeRule.onNodeWithTag("save_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(GameVaultDestinations.ADD_GAME, currentRoute())
        assertEquals(0, games().size)

        composeRule.onNodeWithTag("year_field").performTextReplacement("1985")
        composeRule.onNodeWithTag("save_button").performScrollTo().performClick()
        waitForText("Old Game")
        assertEquals(1985, games().single().releaseYear)
    }

    // ---------------------------------------------------------------- Details and editing

    @Test
    fun openingAGame_passesItsIdToDetailsAndEdit_andChangesReachHome() {
        runBlocking {
            repository.addGame(Game(title = "Hades", platform = Platform.PC, genre = "Adventure", hoursPlayed = 10.0, completionPercent = 40, status = GameStatus.PLAYING))
        }
        launchApp()
        waitForText("Recently updated")
        composeRule.onNodeWithText("Recently updated").performScrollTo()
        composeRule.onNodeWithText("PC · Adventure").performClick()

        waitForText("Log session")
        assertEquals(GameVaultDestinations.GAME_DETAILS, currentRoute())
        composeRule.onNodeWithText("40%").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Edit game").performClick()
        waitForText("Edit game")
        composeRule.onNodeWithTag("title_field").assert(hasText("Hades"))
        composeRule.onNodeWithTag("hours_field").performTextReplacement("25")
        composeRule.onNodeWithTag("save_button").performScrollTo().performClick()

        waitForText("25 h")
        assertEquals(GameVaultDestinations.GAME_DETAILS, currentRoute())
        composeRule.onNodeWithContentDescription("Back").performClick()
        waitForText("Total playtime")
        assertEquals(25.0, games().single().hoursPlayed, 0.0)
    }

    @Test
    fun completeButton_updatesTheStatusOnDetailsAndStatistics() {
        runBlocking {
            repository.addGame(Game(title = "Celeste", platform = Platform.PC, genre = "Platformer", completionPercent = 90, status = GameStatus.PLAYING))
        }
        launchApp()
        waitForText("PC · Platformer")
        composeRule.onNodeWithText("PC · Platformer").performClick()
        waitForTag("mark_completed")

        composeRule.onNodeWithTag("mark_completed").performClick()
        waitForText("100%")

        composeRule.onNodeWithContentDescription("Back").performClick()
        waitForText("Statistics")
        composeRule.onNodeWithText("Statistics").performClick()
        waitForText("Library completed")
        waitForText("100%")
        assertEquals(GameStatus.COMPLETED, games().single().status)
    }

    @Test
    fun doubleTapOnAGame_opensItsDetailsOnlyOnce() {
        runBlocking { repository.addGame(Game(title = "Hades", platform = Platform.PC, genre = "Adventure")) }
        launchApp()
        waitForText("PC · Adventure")

        // Two taps before the screen transition can finish
        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithText("PC · Adventure").performClick()
        composeRule.onNodeWithText("PC · Adventure").performClick()
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()

        assertEquals(GameVaultDestinations.GAME_DETAILS, currentRoute())
        assertEquals(GameVaultDestinations.HOME, navController.previousBackStackEntry?.destination?.route)
    }
}
