package com.jofielramirez.gamevault

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jofielramirez.gamevault.data.local.GameVaultDatabase
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.data.repository.GameSearchRepository
import com.jofielramirez.gamevault.data.repository.OfflineGameRepository
import com.jofielramirez.gamevault.ui.navigation.GameVaultApp
import com.jofielramirez.gamevault.ui.navigation.GameVaultDestinations
import com.jofielramirez.gamevault.ui.theme.GameVaultTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end flows that use dialogs, so they run on an emulator or device.
 * The app runs with an in-memory database: the user's real library on the device is not touched.
 */
@RunWith(AndroidJUnit4::class)
class GameVaultEndToEndTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var db: GameVaultDatabase
    private lateinit var repository: OfflineGameRepository
    private lateinit var navController: NavHostController
    private var originalContainer: AppContainer? = null

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<GameVaultApplication>()
        originalContainer = app.container
        db = Room.inMemoryDatabaseBuilder(app, GameVaultDatabase::class.java).build()
        repository = OfflineGameRepository(db)
        app.replaceContainer(AppContainer({ repository }, { GameSearchRepository.Unavailable }))
        runBlocking {
            repository.addGame(Game(title = "Hades", platform = Platform.PC, genre = "Adventure", hoursPlayed = 10.0, completionPercent = 20))
        }
        composeRule.setContent {
            navController = rememberNavController()
            GameVaultTheme { GameVaultApp(navController) }
        }
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasText("PC · Adventure")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("PC · Adventure").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasText("Log session")).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @After
    fun tearDown() {
        db.close()
        val app = ApplicationProvider.getApplicationContext<GameVaultApplication>()
        originalContainer?.let { app.replaceContainer(it) }
    }

    private fun game() = runBlocking { repository.observeGames().first().single() }

    @Test
    fun logSessionDialog_addsHoursAndStartsPlaying() {
        composeRule.onNodeWithText("Log session").performClick()
        composeRule.onNodeWithText("Hours played").performTextInput("2.5")
        composeRule.onNodeWithText("Save").performClick()

        composeRule.waitUntil(5_000) { game().hoursPlayed == 12.5 }
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasText("12.5 h")).fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals(GameStatus.PLAYING, game().status)
    }

    @Test
    fun deleteDialog_removesTheGameAndGoesBack() {
        composeRule.onNodeWithContentDescription("Delete game").performClick()
        composeRule.onNodeWithText("Delete").performClick()

        composeRule.waitUntil(5_000) { runBlocking { repository.observeGames().first().isEmpty() } }
        composeRule.waitUntil(5_000) {
            navController.currentBackStackEntry?.destination?.route == GameVaultDestinations.HOME
        }
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasText("Your vault is empty")).fetchSemanticsNodes().isNotEmpty()
        }
        assertTrue(runBlocking { repository.observeGames().first() }.isEmpty())
    }
}
