package com.jofielramirez.gamevault.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The init blocks keep invalid data from ever being created, so it can never reach the database. */
class ModelValidationTest {

    @Test
    fun game_defaultValues_describeANewBacklogEntry() {
        val game = Game(title = "Hades", platform = Platform.PC, dateAdded = 1_000L)

        assertEquals(GameStatus.WANT_TO_PLAY, game.status)
        assertEquals(0.0, game.hoursPlayed, 0.0)
        assertEquals(0, game.completionPercent)
        assertEquals(0, game.rating)
        assertNull(game.coverUri)
        assertEquals("lastUpdated starts equal to dateAdded", 1_000L, game.lastUpdated)
    }

    @Test(expected = IllegalArgumentException::class)
    fun game_blankTitle_isRejected() {
        Game(title = "   ", platform = Platform.PC)
    }

    @Test(expected = IllegalArgumentException::class)
    fun game_completionAbove100_isRejected() {
        Game(title = "Zelda", platform = Platform.NINTENDO_SWITCH, completionPercent = 150)
    }

    @Test(expected = IllegalArgumentException::class)
    fun game_negativeHours_isRejected() {
        Game(title = "Zelda", platform = Platform.NINTENDO_SWITCH, hoursPlayed = -1.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun game_ratingAbove5_isRejected() {
        Game(title = "Zelda", platform = Platform.NINTENDO_SWITCH, rating = 6)
    }

    @Test(expected = IllegalArgumentException::class)
    fun playSession_zeroHours_isRejected() {
        PlaySession(gameId = 1, durationHours = 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun playSession_moreThanADay_isRejected() {
        PlaySession(gameId = 1, durationHours = 30.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun achievement_unlockedWithoutDate_isRejected() {
        Achievement(gameId = 1, title = "Defeat Ganon", isUnlocked = true, unlockedAt = null)
    }

    @Test
    fun achievement_unlockAndLock_keepFlagAndDateConsistent() {
        val locked = Achievement(gameId = 1, title = "Defeat Ganon")

        val unlocked = locked.unlock(at = 5_000L)
        assertTrue(unlocked.isUnlocked)
        assertEquals(5_000L, unlocked.unlockedAt)

        val relocked = unlocked.lock()
        assertFalse(relocked.isUnlocked)
        assertNull(relocked.unlockedAt)
    }
}
