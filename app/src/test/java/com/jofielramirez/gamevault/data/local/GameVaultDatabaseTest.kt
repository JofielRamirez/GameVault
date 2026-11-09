package com.jofielramirez.gamevault.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jofielramirez.gamevault.data.model.Achievement
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.PlaySession
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.data.model.PlatformCount
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Runs the real Room database (in memory) on the JVM through Robolectric. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class GameVaultDatabaseTest {

    private lateinit var db: GameVaultDatabase
    private lateinit var gameDao: GameDao
    private lateinit var sessionDao: PlaySessionDao
    private lateinit var achievementDao: AchievementDao

    private val zelda = Game(
        title = "The Legend of Zelda: Breath of the Wild",
        platform = Platform.NINTENDO_SWITCH,
        genre = "Action-adventure",
        releaseYear = 2017,
        rating = 5,
        hoursPlayed = 35.0,
        completionPercent = 65,
        status = GameStatus.PLAYING
    )
    private val hades = Game(
        title = "Hades", platform = Platform.PC, genre = "Roguelike",
        hoursPlayed = 60.0, completionPercent = 100, status = GameStatus.COMPLETED
    )
    private val halo = Game(title = "Halo Infinite", platform = Platform.XBOX, genre = "Shooter")

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GameVaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        gameDao = db.gameDao()
        sessionDao = db.playSessionDao()
        achievementDao = db.achievementDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun insertGame_readsBackEveryField() = runTest {
        val id = gameDao.insert(zelda)

        val stored = gameDao.getById(id)

        assertEquals(zelda.copy(id = id), stored)
    }

    @Test
    fun updateGame_persistsNewProgress() = runTest {
        val id = gameDao.insert(zelda)

        gameDao.update(zelda.copy(id = id, completionPercent = 70, hoursPlayed = 38.5))

        val stored = gameDao.observeById(id).first()!!
        assertEquals(70, stored.completionPercent)
        assertEquals(38.5, stored.hoursPlayed, 0.0)
    }

    @Test
    fun search_isCaseInsensitiveAndMatchesPartOfTheTitle() = runTest {
        gameDao.insert(zelda)
        gameDao.insert(hades)

        val results = gameDao.search("zELDa").first()

        assertEquals(listOf(zelda.title), results.map { it.title })
    }

    @Test
    fun summary_aggregatesTheWholeLibrary() = runTest {
        gameDao.insert(zelda)
        gameDao.insert(hades)
        gameDao.insert(halo)

        val summary = gameDao.observeSummary().first()

        assertEquals(3, summary.totalGames)
        assertEquals(1, summary.playingGames)
        assertEquals(1, summary.completedGames)
        assertEquals(95.0, summary.totalHours, 0.001)
        assertEquals((65 + 100 + 0) / 3.0, summary.averageCompletion, 0.001)
    }

    @Test
    fun summary_ofEmptyLibrary_isAllZeros() = runTest {
        val summary = gameDao.observeSummary().first()

        assertEquals(0, summary.totalGames)
        assertEquals(0.0, summary.totalHours, 0.0)
    }

    @Test
    fun countByPlatform_groupsGames() = runTest {
        gameDao.insert(zelda)
        gameDao.insert(hades)
        gameDao.insert(hades.copy(title = "Celeste"))

        val counts = gameDao.observeCountByPlatform().first()

        assertEquals(
            listOf(PlatformCount(Platform.PC, 2), PlatformCount(Platform.NINTENDO_SWITCH, 1)),
            counts
        )
    }

    @Test
    fun gameDetails_loadsSessionsAndAchievements() = runTest {
        val id = gameDao.insert(zelda)
        sessionDao.insert(PlaySession(gameId = id, durationHours = 2.0))
        achievementDao.insert(Achievement(gameId = id, title = "Free Vah Ruta").unlock(at = 1L))
        achievementDao.insert(Achievement(gameId = id, title = "Defeat Ganon"))

        val details = gameDao.observeDetails(id).first()!!

        assertEquals(1, details.sessions.size)
        assertEquals(2, details.achievements.size)
        assertEquals(1, details.unlockedAchievements)
    }

    @Test
    fun deletingAGame_cascadesToItsSessionsAndAchievements() = runTest {
        val id = gameDao.insert(zelda)
        sessionDao.insert(PlaySession(gameId = id, durationHours = 1.5))
        achievementDao.insert(Achievement(gameId = id, title = "Defeat Ganon"))

        gameDao.delete(gameDao.getById(id)!!)

        assertNull(gameDao.getById(id))
        assertTrue(sessionDao.observeForGame(id).first().isEmpty())
        assertTrue(achievementDao.observeForGame(id).first().isEmpty())
    }

    @Test(expected = Exception::class)
    fun sessionForMissingGame_violatesForeignKey() = runTest {
        sessionDao.insert(PlaySession(gameId = 999, durationHours = 1.0))
    }

    @Test
    fun hoursBetween_onlyCountsSessionsInsideTheRange() = runTest {
        val id = gameDao.insert(zelda)
        sessionDao.insert(PlaySession(gameId = id, playedOn = 100L, durationHours = 1.0))
        sessionDao.insert(PlaySession(gameId = id, playedOn = 200L, durationHours = 2.5))
        sessionDao.insert(PlaySession(gameId = id, playedOn = 300L, durationHours = 4.0))

        assertEquals(3.5, sessionDao.observeHoursBetween(from = 100L, to = 300L).first(), 0.0)
    }
}
