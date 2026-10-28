package com.jofiel.gamevault.ui.form

import com.jofiel.gamevault.data.Game
import com.jofiel.gamevault.data.GameStatus
import com.jofiel.gamevault.data.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFormValidatorTest {

    private val valid = GameFormState(title = "Hades", platform = Platform.PC, hoursPlayed = "12")

    @Test
    fun validForm_hasNoErrors() {
        assertFalse(GameFormValidator.validate(valid).hasErrors)
    }

    @Test
    fun blankTitle_isRejected() {
        val errors = GameFormValidator.validate(valid.copy(title = "   "))
        assertNotNull(errors.title)
        assertTrue(errors.hasErrors)
    }

    @Test
    fun tooLongTitle_isRejected() {
        val longTitle = "a".repeat(GameFormValidator.MAX_TITLE_LENGTH + 1)
        assertNotNull(GameFormValidator.validate(valid.copy(title = longTitle)).title)
    }

    @Test
    fun emptyHours_meansZeroAndIsValid() {
        val state = valid.copy(hoursPlayed = "")
        assertNull(GameFormValidator.validate(state).hours)
        assertEquals(0, state.toGame(now = 1L).hoursPlayed)
    }

    @Test
    fun nonNumericOrNegativeHours_areRejected() {
        assertNotNull(GameFormValidator.validate(valid.copy(hoursPlayed = "abc")).hours)
        assertNotNull(GameFormValidator.validate(valid.copy(hoursPlayed = "-5")).hours)
        assertNotNull(GameFormValidator.validate(valid.copy(hoursPlayed = "100000")).hours)
    }

    @Test
    fun toGame_trimsTextAndClampsValues() {
        val game = valid.copy(title = "  Hades  ", genre = " Roguelike ", progress = 150, rating = 9)
            .toGame(now = 42L)
        assertEquals("Hades", game.title)
        assertEquals("Roguelike", game.genre)
        assertEquals(100, game.progress)
        assertEquals(5, game.rating)
        assertEquals(42L, game.createdAt)
        assertEquals(42L, game.updatedAt)
    }

    @Test
    fun editingKeepsOriginalCreationDate() {
        val original = Game(
            id = 7, title = "Celeste", platform = Platform.NINTENDO_SWITCH,
            status = GameStatus.PLAYING, hoursPlayed = 10, progress = 40, createdAt = 1_000L,
        )
        val edited = original.toFormState().copy(hoursPlayed = "15").toGame(now = 5_000L)
        assertEquals(7L, edited.id)
        assertEquals(1_000L, edited.createdAt)
        assertEquals(5_000L, edited.updatedAt)
        assertEquals(15, edited.hoursPlayed)
    }
}
