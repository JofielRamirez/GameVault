package com.jofielramirez.gamevault.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jofielramirez.gamevault.data.model.GameStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

/**
 * Checks that a library saved by version 1 of the database survives the update to version 2.
 *
 * The version 1 database is built from the exported schema (app/schemas/.../1.json), exactly as
 * the old app created it. Opening it with the current Room database runs the generated
 * AutoMigration, and Room then validates the result against version 2 (it throws if any table
 * or column does not match).
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class GameVaultMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val dbFile get() = context.getDatabasePath(TEST_DB)

    @Before
    fun setUp() {
        context.deleteDatabase(TEST_DB)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(TEST_DB)
    }

    @Test
    fun migrate1To2_keepsTheLibraryAndAddsAnEmptyRawgId() = runTest {
        createVersion1Database { db ->
            db.execSQL(
                """INSERT INTO games (id, title, platform, genre, release_year, rating, hours_played,
                   completion_percent, status, cover_uri, notes, date_added, last_updated)
                   VALUES (1, 'Celeste', 'PC', 'Platformer', 2018, 4, 9.5, 100, 'COMPLETED', NULL, 'Great', 10, 20)"""
            )
            db.execSQL("INSERT INTO play_sessions (game_id, played_on, duration_hours, progress_after, note) VALUES (1, 15, 2.0, 100, '')")
        }

        val room = Room.databaseBuilder(context, GameVaultDatabase::class.java, TEST_DB)
            .allowMainThreadQueries()
            .build()
        try {
            val details = room.gameDao().observeDetails(1).first()!!
            assertEquals("Celeste", details.game.title)
            assertEquals(9.5, details.game.hoursPlayed, 0.0)
            assertEquals(GameStatus.COMPLETED, details.game.status)
            assertEquals("Great", details.game.notes)
            assertNull(details.game.rawgId)
            assertEquals(1, details.sessions.size)
        } finally {
            room.close()
        }
    }

    /** Creates the tables, indices and Room metadata of schema version 1, then lets [fill] add rows. */
    private fun createVersion1Database(fill: (SQLiteDatabase) -> Unit) {
        val schema = Json.parseToJsonElement(File(SCHEMA_DIR, "1.json").readText()).jsonObject["database"]!!.jsonObject
        dbFile.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { db ->
            db.execSQL("PRAGMA foreign_keys = ON")
            schema["entities"]!!.jsonArray.forEach { entityElement ->
                val entity = entityElement.jsonObject
                val table = entity["tableName"]!!.jsonPrimitive.content
                db.execSQL(entity["createSql"]!!.jsonPrimitive.content.replace("\${TABLE_NAME}", table))
                entity["indices"]?.jsonArray?.forEach { indexElement ->
                    val index = indexElement.jsonObject
                    db.execSQL(
                        index["createSql"]!!.jsonPrimitive.content
                            .replace("\${TABLE_NAME}", table)
                            .replace("\${INDEX_NAME}", index["name"]!!.jsonPrimitive.content)
                    )
                }
            }
            schema["setupQueries"]!!.jsonArray.forEach { db.execSQL(it.jsonPrimitive.content) }
            db.version = 1
            fill(db)
        }
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
        // Robolectric runs the tests from the app module folder
        val SCHEMA_DIR = File("schemas/com.jofielramirez.gamevault.data.local.GameVaultDatabase")
    }
}
