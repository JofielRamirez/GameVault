package com.jofielramirez.gamevault.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jofielramirez.gamevault.data.model.Achievement
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.PlaySession

@Database(
    entities = [Game::class, PlaySession::class, Achievement::class],
    version = 1,
    exportSchema = true
)
abstract class GameVaultDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao
    abstract fun playSessionDao(): PlaySessionDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        const val DATABASE_NAME = "gamevault.db"

        @Volatile
        private var instance: GameVaultDatabase? = null

        /** One database instance per process; opening several would waste memory and break observers. */
        fun getInstance(context: Context): GameVaultDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    GameVaultDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
    }
}
