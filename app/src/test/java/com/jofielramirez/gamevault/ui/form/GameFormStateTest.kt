package com.jofielramirez.gamevault.ui.form

import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFormStateTest {

    @Test
    fun emptyForm_isInvalidBecauseOfTheTitle() {
        val state = GameFormState()
        assertFalse(state.isValid)
        assertNotNull(state.titleError)
    }

    @Test
    fun yearAndHours_areValidated() {
        assertNotNull(GameFormState(title = "Hades", releaseYear = "1900").releaseYearError)
        assertNull(GameFormState(title = "Hades", releaseYear = "2020").releaseYearError)
        assertNotNull(GameFormState(title = "Hades", hoursPlayed = "1.2.3").hoursError)
        assertTrue(GameFormState(title = "Hades", hoursPlayed = "12,5").isValid)
    }

    @Test
    fun toGame_convertsTextFieldsToTypedValues() {
        val game = GameFormState(
            title = "  Hades ", platform = Platform.PC, releaseYear = "2020",
            hoursPlayed = "12,5", completionPercent = 40, rating = 4
        ).toGame(base = null)

        assertEquals("Hades", game.title)
        assertEquals(2020, game.releaseYear)
        assertEquals(12.5, game.hoursPlayed, 0.0)
        assertEquals(40, game.completionPercent)
    }

    @Test
    fun toGame_whenEditing_keepsIdAndDateAdded() {
        val original = Game(id = 7, title = "Hades", platform = Platform.PC, dateAdded = 123L)

        val edited = GameFormState.from(original).copy(status = GameStatus.PLAYING).toGame(original)

        assertEquals(7, edited.id)
        assertEquals(123L, edited.dateAdded)
        assertEquals(GameStatus.PLAYING, edited.status)
    }
}
