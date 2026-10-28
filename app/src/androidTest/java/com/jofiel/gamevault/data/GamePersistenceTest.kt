package com.jofiel.gamevault.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simulates closing and relaunching the app: data written to the on-disk database
 * must still be there after the database is closed and opened again.
 */
@RunWith(AndroidJUnit4::class)
class GamePersistenceTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val dbName = "gamevault-persistence-test.db"

    private fun openDb(): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, dbName).build()

    @After
    fun cleanUp() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun gamesSurviveClosingAndReopeningTheDatabase() = runBlocking {
        val firstLaunch = openDb()
        firstLaunch.gameDao().insert(
            Game(title = "Stardew Valley", platform = Platform.NINTENDO_SWITCH, hoursPlayed = 64, progress = 51),
        )
        firstLaunch.close()

        val secondLaunch = openDb()
        val games = secondLaunch.gameDao().observeAll().first()
        secondLaunch.close()

        assertEquals(1, games.size)
        assertEquals("Stardew Valley", games.first().title)
        assertEquals(64, games.first().hoursPlayed)
        assertEquals(51, games.first().progress)
    }
}
