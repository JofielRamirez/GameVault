package com.jofielramirez.gamevault

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameDetails
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.ui.details.GameDetailsContent
import com.jofielramirez.gamevault.ui.details.GameDetailsUiState
import com.jofielramirez.gamevault.ui.theme.GameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Runs on an emulator or device: the dialog opens in its own window. */
@RunWith(AndroidJUnit4::class)
class LogSessionDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var logged: Pair<Double, Int>? = null

    private fun showDetails() {
        val zelda = Game(
            id = 1, title = "The Legend of Zelda: Breath of the Wild", platform = Platform.NINTENDO_SWITCH,
            hoursPlayed = 35.0, completionPercent = 65, status = GameStatus.PLAYING
        )
        composeRule.setContent {
            GameVaultTheme {
                GameDetailsContent(
                    uiState = GameDetailsUiState(GameDetails(zelda, emptyList(), emptyList()), isLoading = false),
                    onNavigateUp = {}, onEdit = {}, onDelete = {},
                    onLogSession = { hours, progress -> logged = hours to progress },
                    onMarkCompleted = {}, onToggleAchievement = {}, onAddAchievement = {}
                )
            }
        }
    }

    @Test
    fun logSession_sendsHoursAndCurrentProgress() {
        showDetails()

        composeRule.onNodeWithText("Log session").performClick()
        composeRule.onNodeWithText("Log a play session").assertIsDisplayed()
        composeRule.onNodeWithText("Hours played").performTextInput("2.5")
        composeRule.onNodeWithText("Save").performClick()

        assertEquals(2.5 to 65, logged)
    }

    @Test
    fun logSession_saveIsDisabledUntilHoursAreValid() {
        showDetails()

        composeRule.onNodeWithText("Log session").performClick()
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        composeRule.onNodeWithText("Hours played").performTextInput("30")
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
    }
}
