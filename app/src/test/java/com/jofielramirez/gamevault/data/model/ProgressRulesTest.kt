package com.jofielramirez.gamevault.data.model

import com.jofielramirez.gamevault.data.model.GameStatus.COMPLETED
import com.jofielramirez.gamevault.data.model.GameStatus.DROPPED
import com.jofielramirez.gamevault.data.model.GameStatus.ON_HOLD
import com.jofielramirez.gamevault.data.model.GameStatus.PLAYING
import com.jofielramirez.gamevault.data.model.GameStatus.WANT_TO_PLAY
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressRulesTest {

    private fun reconcile(previous: Pair<GameStatus, Int>?, new: Pair<GameStatus, Int>) =
        ProgressRules.reconcile(previous?.first, previous?.second, new.first, new.second)

    @Test
    fun choosingCompleted_fillsTheProgressTo100() {
        assertEquals(COMPLETED to 100, reconcile(PLAYING to 40, COMPLETED to 40))
    }

    @Test
    fun loweringTheProgressOfACompletedGame_movesItBackToPlaying() {
        assertEquals(PLAYING to 40, reconcile(COMPLETED to 100, COMPLETED to 40))
    }

    @Test
    fun reaching100Percent_completesTheGame() {
        assertEquals(COMPLETED to 100, reconcile(PLAYING to 90, PLAYING to 100))
    }

    @Test
    fun progressOnABacklogGame_startsIt() {
        assertEquals(PLAYING to 10, reconcile(WANT_TO_PLAY to 0, WANT_TO_PLAY to 10))
    }

    @Test
    fun onHoldAndDropped_keepAnyPercentage() {
        assertEquals(ON_HOLD to 30, reconcile(PLAYING to 30, ON_HOLD to 30))
        assertEquals(DROPPED to 15, reconcile(PLAYING to 15, DROPPED to 15))
    }

    @Test
    fun reopeningACompletedGame_respectsTheUsersChoice() {
        // e.g. starting a replay: the user explicitly picks "Currently Playing"
        assertEquals(PLAYING to 100, reconcile(COMPLETED to 100, PLAYING to 100))
    }

    @Test
    fun newGames_followTheSameRules() {
        assertEquals(COMPLETED to 100, reconcile(null, COMPLETED to 0))
        assertEquals(COMPLETED to 100, reconcile(null, PLAYING to 100))
        assertEquals(PLAYING to 25, reconcile(null, WANT_TO_PLAY to 25))
        assertEquals(WANT_TO_PLAY to 0, reconcile(null, WANT_TO_PLAY to 0))
    }
}
