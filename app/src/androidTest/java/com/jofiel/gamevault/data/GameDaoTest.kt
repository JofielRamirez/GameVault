package com.jofiel.gamevault.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Create, edit and delete against an in-memory Room database. */
@RunWith(AndroidJUnit4::class)
class GameDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: GameDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.gameDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun sample(title: String = "Hades") = Game(
        title = title,
        platform = Platform.PC,
        genre = "Roguelike",
        status = GameStatus.PLAYING,
        hoursPlayed = 12,
        progress = 40,
    )

    @Test
    fun create_insertsGameAndEmitsIt() = runBlocking {
        val id = dao.insert(sample())

        val games = dao.observeAll().first()
        assertEquals(1, games.size)
        assertEquals("Hades", games.first().title)
        assertEquals(Platform.PC, dao.getById(id)?.platform)
        assertEquals(GameStatus.PLAYING, dao.getById(id)?.status)
    }

    @Test
    fun edit_updatesStoredFields() = runBlocking {
        val id = dao.insert(sample())
        val stored = dao.getById(id)!!

        dao.update(stored.copy(hoursPlayed = 30, progress = 100, status = GameStatus.COMPLETED))

        val updated = dao.getById(id)!!
        assertEquals(30, updated.hoursPlayed)
        assertEquals(100, updated.progress)
        assertEquals(GameStatus.COMPLETED, updated.status)
    }

    @Test
    fun delete_removesOnlyThatGame() = runBlocking {
        val keepId = dao.insert(sample("Celeste"))
        val removeId = dao.insert(sample("Hades"))

        dao.delete(dao.getById(removeId)!!)

        assertNull(dao.getById(removeId))
        val games = dao.observeAll().first()
        assertEquals(listOf(keepId), games.map { it.id })
    }

    @Test
    fun repository_saveInsertsThenUpdates() = runBlocking {
        val repository = GameRepository(dao)

        val id = repository.save(sample())
        repository.save(repository.getGame(id)!!.copy(title = "Hades II"))

        val games = repository.observeGames().first()
        assertEquals(1, games.size)
        assertEquals("Hades II", games.first().title)
        assertTrue(games.first().updatedAt >= games.first().createdAt)
    }
}
