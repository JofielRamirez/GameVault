package com.jofielramirez.gamevault.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jofielramirez.gamevault.data.local.GameVaultDatabase
import com.jofielramirez.gamevault.data.model.Achievement
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.PlaySession
import com.jofielramirez.gamevault.data.model.Platform
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class OfflineGameRepositoryTest {

    private lateinit var db: GameVaultDatabase
    private lateinit var repository: OfflineGameRepository
    private var now = 1_000L

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GameVaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = OfflineGameRepository(db, clock = { now })
    }

    @After
    fun tearDown() = db.close()

    private suspend fun addZelda(status: GameStatus = GameStatus.WANT_TO_PLAY): Long =
        repository.addGame(
            Game(
                title = "The Legend of Zelda: Breath of the Wild",
                platform = Platform.NINTENDO_SWITCH,
                hoursPlayed = 35.0,
                completionPercent = 65,
                status = status
            )
        )

    @Test
    fun addGame_stampsDateAddedAndLastUpdated() = runTest {
        now = 42_000L
        val id = addZelda()

        val game = repository.observeGameDetails(id).first()!!.game
        assertEquals(42_000L, game.dateAdded)
        assertEquals(42_000L, game.lastUpdated)
    }

    @Test
    fun logPlaySession_addsHoursUpdatesProgressAndRecordsTheSession() = runTest {
        val id = addZelda(GameStatus.PLAYING)
        now = 5_000L

        repository.logPlaySession(PlaySession(gameId = id, durationHours = 2.5, progressAfter = 70))

        val details = repository.observeGameDetails(id).first()!!
        assertEquals(37.5, details.game.hoursPlayed, 0.0)
        assertEquals(70, details.game.completionPercent)
        assertEquals(5_000L, details.game.lastUpdated)
        assertEquals(1, details.sessions.size)
    }

    @Test
    fun logPlaySession_withoutProgress_keepsCurrentCompletion() = runTest {
        val id = addZelda(GameStatus.PLAYING)

        repository.logPlaySession(PlaySession(gameId = id, durationHours = 1.0))

        assertEquals(65, repository.observeGameDetails(id).first()!!.game.completionPercent)
    }

    @Test
    fun firstSessionOfABacklogGame_movesItToPlaying() = runTest {
        val id = addZelda(GameStatus.WANT_TO_PLAY)

        repository.logPlaySession(PlaySession(gameId = id, durationHours = 1.0))

        assertEquals(GameStatus.PLAYING, repository.observeGameDetails(id).first()!!.game.status)
    }

    @Test
    fun reaching100Percent_marksTheGameCompleted() = runTest {
        val id = addZelda(GameStatus.PLAYING)

        repository.logPlaySession(PlaySession(gameId = id, durationHours = 3.0, progressAfter = 100))

        val summary = repository.observeSummary().first()
        assertEquals(1, summary.completedGames)
        assertEquals(0, summary.playingGames)
    }

    @Test
    fun recentlyUpdated_putsTheLastPlayedGameFirst() = runTest {
        now = 1_000L
        val zelda = addZelda(GameStatus.PLAYING)
        now = 2_000L
        repository.addGame(Game(title = "Hades", platform = Platform.PC))
        now = 3_000L

        repository.logPlaySession(PlaySession(gameId = zelda, durationHours = 1.0))

        assertEquals(zelda, repository.observeRecentlyUpdated().first().first().id)
    }

    @Test
    fun setAchievementUnlocked_storesTheUnlockDate() = runTest {
        val gameId = addZelda()
        val achievementId = repository.addAchievement(Achievement(gameId = gameId, title = "Defeat Ganon"))
        now = 9_000L

        repository.setAchievementUnlocked(
            Achievement(id = achievementId, gameId = gameId, title = "Defeat Ganon"),
            unlocked = true
        )

        val achievement = repository.observeGameDetails(gameId).first()!!.achievements.single()
        assertTrue(achievement.isUnlocked)
        assertEquals(9_000L, achievement.unlockedAt)
    }
}
