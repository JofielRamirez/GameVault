package com.jofielramirez.gamevault.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.jofielramirez.gamevault.data.model.PlaySession
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaySessionDao {

    @Insert
    suspend fun insert(session: PlaySession): Long

    @Delete
    suspend fun delete(session: PlaySession)

    @Query("SELECT * FROM play_sessions WHERE game_id = :gameId ORDER BY played_on DESC")
    fun observeForGame(gameId: Long): Flow<List<PlaySession>>

    /** Hours played across the whole library in [from, to) — used for monthly goals. */
    @Query("SELECT COALESCE(SUM(duration_hours), 0.0) FROM play_sessions WHERE played_on >= :from AND played_on < :to")
    fun observeHoursBetween(from: Long, to: Long): Flow<Double>
}
