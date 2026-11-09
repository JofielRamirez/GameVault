package com.jofielramirez.gamevault.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jofielramirez.gamevault.data.model.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Insert
    suspend fun insert(achievement: Achievement): Long

    @Update
    suspend fun update(achievement: Achievement)

    @Delete
    suspend fun delete(achievement: Achievement)

    @Query("SELECT * FROM achievements WHERE game_id = :gameId ORDER BY is_unlocked DESC, title COLLATE NOCASE ASC")
    fun observeForGame(gameId: Long): Flow<List<Achievement>>

    @Query("SELECT COUNT(*) FROM achievements WHERE is_unlocked = 1")
    fun observeUnlockedCount(): Flow<Int>
}
