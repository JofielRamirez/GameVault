package com.jofiel.gamevault.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jofiel.gamevault.MainActivity
import com.jofiel.gamevault.ui.form.GameFormTestTags
import com.jofiel.gamevault.ui.library.LibraryTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end MVP flow through the real UI and database:
 * create a game, edit it, then delete it.
 * Uses a unique title so it never collides with games you already have on the emulator.
 */
@RunWith(AndroidJUnit4::class)
class GameVaultFlowTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val timeout = 5_000L

    private fun waitForLibrary() {
        rule.waitUntil(timeout) {
            rule.onAllNodes(hasTestTag(GameFormTestTags.TITLE)).fetchSemanticsNodes().isEmpty() &&
                rule.onAllNodes(hasTestTag(LibraryTestTags.ADD_BUTTON)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForText(text: String) {
        rule.waitUntil(timeout) {
            rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForFormLoaded(title: String) {
        rule.waitUntil(timeout) {
            rule.onAllNodes(hasTestTag(GameFormTestTags.TITLE) and hasText(title))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun createEditAndDeleteGame() {
        val title = "MVP Test ${System.currentTimeMillis()}"
        val editedTitle = "$title edited"

        // Create
        waitForLibrary()
        rule.onNodeWithTag(LibraryTestTags.ADD_BUTTON).performClick()
        rule.onNodeWithTag(GameFormTestTags.TITLE).performTextInput(title)
        rule.onNodeWithTag(GameFormTestTags.HOURS).performTextInput("12")
        rule.onNodeWithTag(GameFormTestTags.SAVE).performScrollTo().performClick()

        waitForLibrary()
        waitForText(title)
        rule.onNodeWithText(title).assertIsDisplayed()

        // Edit
        rule.onNodeWithText(title).performClick()
        waitForFormLoaded(title)
        rule.onNodeWithTag(GameFormTestTags.TITLE).performTextClearance()
        rule.onNodeWithTag(GameFormTestTags.TITLE).performTextInput(editedTitle)
        rule.onNodeWithTag(GameFormTestTags.SAVE).performScrollTo().performClick()

        waitForLibrary()
        waitForText(editedTitle)
        rule.onNodeWithText(editedTitle).assertIsDisplayed()

        // Delete
        rule.onNodeWithText(editedTitle).performClick()
        waitForFormLoaded(editedTitle)
        rule.onNodeWithTag(GameFormTestTags.DELETE).performClick()
        rule.onNodeWithTag(GameFormTestTags.CONFIRM_DELETE).performClick()

        waitForLibrary()
        rule.waitUntil(timeout) {
            rule.onAllNodes(hasText(editedTitle)).fetchSemanticsNodes().isEmpty()
        }
        rule.onNodeWithText(editedTitle).assertDoesNotExist()
    }
}
