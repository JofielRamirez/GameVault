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
import com.jofielramirez.gamevault.ui.library.SortOrder
import com.jofielramirez.gamevault.ui.home.HomeContent
import com.jofielramirez.gamevault.ui.home.HomeUiState
import com.jofielramirez.gamevault.ui.stats.StatsContent
import com.jofielramirez.gamevault.ui.stats.StatsUiState
import com.jofielramirez.gamevault.data.model.PlatformCount
import com.jofielramirez.gamevault.data.model.StatusCount
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

    private fun libraryState(vararg games: Game) = LibraryUiState(
        games = games.toList(),
        totalGames = games.size,
        availablePlatforms = games.map { it.platform }.distinct(),
        availableGenres = games.map { it.genre }.distinct(),
        isLoading = false
    )

    @Test
    fun library_showsEveryGameWithItsProgress() {
        composeRule.setContent {
            GameVaultTheme { LibraryContent(uiState = libraryState(zelda, hades), onGameClick = {}, onAddGame = {}) }
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
                LibraryContent(uiState = libraryState(zelda), onGameClick = {}, onAddGame = {}, onStatusFilterChange = { selected = it })
            }
        }

        composeRule.onNodeWithText("On Hold").performClick()

        assertEquals(GameStatus.ON_HOLD, selected)
    }

    @Test
    fun library_sortMenu_changesTheSortOrder() {
        var sort: SortOrder? = null
        composeRule.setContent {
            GameVaultTheme {
                LibraryContent(uiState = libraryState(zelda, hades), onGameClick = {}, onAddGame = {}, onSortChange = { sort = it })
            }
        }

        composeRule.onNodeWithText("Sort: Title (A–Z)").performClick()
        composeRule.onNodeWithText("Most played").performClick()

        assertEquals(SortOrder.PLAYTIME, sort)
    }

    @Test
    fun library_platformMenu_onlyOffersPlatformsInTheLibrary() {
        var platform: Platform? = null
        composeRule.setContent {
            GameVaultTheme {
                LibraryContent(uiState = libraryState(zelda, hades), onGameClick = {}, onAddGame = {}, onPlatformFilterChange = { platform = it })
            }
        }

        composeRule.onNodeWithText("Platform").performClick()
        composeRule.onNodeWithText("Xbox").assertDoesNotExist()
        composeRule.onNodeWithText("Nintendo Switch").performClick()

        assertEquals(Platform.NINTENDO_SWITCH, platform)
    }

    @Test
    fun emptyLibrary_offersToAddAGameOrLoadSamples() {
        var samplesRequested = false
        composeRule.setContent {
            GameVaultTheme {
                LibraryContent(
                    uiState = LibraryUiState(isLoading = false),
                    onGameClick = {}, onAddGame = {},
                    onLoadSampleLibrary = { samplesRequested = true }
                )
            }
        }

        composeRule.onNodeWithText("Your vault is empty").assertIsDisplayed()
        composeRule.onNodeWithText("Explore with sample games").performClick()
        assertTrue(samplesRequested)
    }

    // ---------------------------------------------------------------- Home

    @Test
    fun home_showsSummaryAndGamesInProgress() {
        composeRule.setContent {
            GameVaultTheme {
                HomeContent(
                    uiState = HomeUiState(
                        summary = LibrarySummary(7, 2, 2, 204.5, 51.0),
                        continuePlaying = listOf(zelda),
                        recentlyUpdated = listOf(hades),
                        isLoading = false
                    ),
                    onGameClick = {}, onAddGame = {}
                )
            }
        }

        composeRule.onNodeWithText("204.5 h").assertIsDisplayed()
        composeRule.onNodeWithText("Continue playing").assertIsDisplayed()
        composeRule.onNodeWithText(zelda.title).assertIsDisplayed()
        composeRule.onNodeWithText("Hades").assertIsDisplayed()
    }

    @Test
    fun home_tappingAGameInProgress_opensIt() {
        var opened: Long? = null
        composeRule.setContent {
            GameVaultTheme {
                HomeContent(
                    uiState = HomeUiState(summary = LibrarySummary(1, 1, 0, 35.0, 65.0), continuePlaying = listOf(zelda), isLoading = false),
                    onGameClick = { opened = it }, onAddGame = {}
                )
            }
        }

        composeRule.onNodeWithText(zelda.title).performClick()

        assertEquals(zelda.id, opened)
    }

    // ---------------------------------------------------------------- Statistics

    @Test
    fun stats_showsTotalsAndPlatformBreakdown() {
        composeRule.setContent {
            GameVaultTheme {
                StatsContent(
                    StatsUiState(
                        summary = LibrarySummary(4, 1, 2, 120.0, 70.0),
                        byPlatform = listOf(PlatformCount(Platform.PC, 3), PlatformCount(Platform.XBOX, 1)),
                        byStatus = listOf(StatusCount(GameStatus.COMPLETED, 2), StatusCount(GameStatus.PLAYING, 1), StatusCount(GameStatus.DROPPED, 1)),
                        mostPlayed = listOf(hades),
                        isLoading = false
                    )
                )
            }
        }

        composeRule.onNodeWithText("Games by platform").assertIsDisplayed()
        composeRule.onNodeWithText("50%").assertIsDisplayed()          // 2 of 4 games completed
        composeRule.onNodeWithText("2 · 50%").assertIsDisplayed()       // Completed slice of the status chart
    }

    @Test
    fun stats_emptyLibrary_explainsWhyThereAreNoCharts() {
        composeRule.setContent { GameVaultTheme { StatsContent(StatsUiState(isLoading = false)) } }

        composeRule.onNodeWithText("No statistics yet").assertIsDisplayed()
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
