package com.jofielramirez.gamevault.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jofielramirez.gamevault.data.model.Achievement
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameDetails
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.LibrarySummary
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.ui.details.GameDetailsContent
import com.jofielramirez.gamevault.ui.details.GameDetailsUiState
import com.jofielramirez.gamevault.ui.form.GameFormContent
import com.jofielramirez.gamevault.ui.form.GameFormState
import com.jofielramirez.gamevault.ui.library.LibraryContent
import com.jofielramirez.gamevault.ui.library.LibraryUiState
import com.jofielramirez.gamevault.ui.theme.GameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Compose UI tests for the three screens, run on the JVM with Robolectric.
 * The qualifiers simulate a regular phone (411 x 914 dp); Robolectric's default
 * screen is so small that cards further down the list are never composed.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w411dp-h914dp-xxhdpi")
class GameVaultScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val zelda = Game(
        id = 1, title = "The Legend of Zelda: Breath of the Wild", platform = Platform.NINTENDO_SWITCH,
        genre = "Action-adventure", hoursPlayed = 35.0, completionPercent = 65, status = GameStatus.PLAYING
    )
    private val hades = Game(
        id = 2, title = "Hades", platform = Platform.PC, genre = "Roguelike",
        hoursPlayed = 60.0, completionPercent = 100, status = GameStatus.COMPLETED
    )

    // ---------------------------------------------------------------- Library

    @Test
    fun library_showsEveryGameWithItsProgress() {
        composeRule.setContent {
            GameVaultTheme {
                LibraryContent(
                    uiState = LibraryUiState(games = listOf(zelda, hades), summary = LibrarySummary(2, 1, 1, 95.0, 82.5), isLoading = false),
                    onQueryChange = {}, onStatusFilterChange = {}, onGameClick = {}, onAddGame = {}, onLoadSampleLibrary = {}
                )
            }
        }

        composeRule.onNodeWithText(zelda.title).assertIsDisplayed()
        composeRule.onNodeWithText("Hades").assertIsDisplayed()
        composeRule.onNodeWithText("35 h · 65%").assertIsDisplayed()
    }

    @Test
    fun library_tappingAStatusChip_filtersByThatStatus() {
        var selected: GameStatus? = null
        composeRule.setContent {
            GameVaultTheme {
                LibraryContent(
                    uiState = LibraryUiState(games = listOf(zelda), summary = LibrarySummary(1, 1, 0, 35.0, 65.0), isLoading = false),
                    onQueryChange = {}, onStatusFilterChange = { selected = it }, onGameClick = {}, onAddGame = {}, onLoadSampleLibrary = {}
                )
            }
        }

        composeRule.onNodeWithText("On Hold").performClick()

        assertEquals(GameStatus.ON_HOLD, selected)
    }

    @Test
    fun emptyLibrary_offersToAddAGameOrLoadSamples() {
        var samplesRequested = false
        composeRule.setContent {
            GameVaultTheme {
                LibraryContent(
                    uiState = LibraryUiState(isLoading = false),
                    onQueryChange = {}, onStatusFilterChange = {}, onGameClick = {}, onAddGame = {},
                    onLoadSampleLibrary = { samplesRequested = true }
                )
            }
        }

        composeRule.onNodeWithText("Your vault is empty").assertIsDisplayed()
        composeRule.onNodeWithText("Explore with sample games").performClick()
        assertTrue(samplesRequested)
    }

    // ---------------------------------------------------------------- Form

    @Test
    fun form_afterSavingWithoutTitle_showsTheError() {
        composeRule.setContent {
            GameVaultTheme {
                GameFormContent(
                    state = GameFormState(showErrors = true),
                    isEditing = false, onStateChange = {}, onSave = {}, onClose = {}
                )
            }
        }

        composeRule.onNodeWithText("Enter the game's title").assertIsDisplayed()
    }

    @Test
    fun form_typingATitle_updatesTheState() {
        var state = GameFormState()
        composeRule.setContent {
            GameVaultTheme {
                GameFormContent(state = state, isEditing = false, onStateChange = { state = it }, onSave = {}, onClose = {})
            }
        }

        composeRule.onNodeWithTag("title_field").performTextInput("Celeste")

        assertEquals("Celeste", state.title)
    }

    // ---------------------------------------------------------------- Details

    private fun setDetails(
        game: Game = zelda,
        onMarkCompleted: () -> Unit = {},
        onToggle: (Achievement) -> Unit = {},
        onLogSession: (Double, Int) -> Unit = { _, _ -> }
    ) {
        val achievements = listOf(
            Achievement(id = 1, gameId = 1, title = "Free Vah Ruta", isUnlocked = true, unlockedAt = 0L),
            Achievement(id = 2, gameId = 1, title = "Defeat Calamity Ganon")
        )
        composeRule.setContent {
            GameVaultTheme {
                GameDetailsContent(
                    uiState = GameDetailsUiState(GameDetails(game, emptyList(), achievements), isLoading = false),
                    onNavigateUp = {}, onEdit = {}, onDelete = {}, onLogSession = onLogSession,
                    onMarkCompleted = onMarkCompleted, onToggleAchievement = onToggle, onAddAchievement = {}
                )
            }
        }
    }

    @Test
    fun details_tappingAnAchievement_marksItAsCompleted() {
        var toggled: Achievement? = null
        setDetails(onToggle = { toggled = it })

        composeRule.onNodeWithText("Defeat Calamity Ganon").performClick()

        assertEquals("Defeat Calamity Ganon", toggled?.title)
    }

    @Test
    fun details_markCompletedButton_completesTheGame() {
        var completed = false
        setDetails(onMarkCompleted = { completed = true })

        composeRule.onNodeWithTag("mark_completed").performClick()

        assertTrue(completed)
    }

    @Test
    fun details_completedGame_disablesMarkCompleted() {
        setDetails(game = hades)

        composeRule.onNodeWithTag("mark_completed").assertIsNotEnabled()
    }

    // The "Log session" dialog is tested on a device (androidTest/LogSessionDialogTest):
    // Robolectric never becomes idle when a Compose AlertDialog window opens.
}
