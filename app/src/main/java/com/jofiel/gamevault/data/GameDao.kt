package com.jofiel.gamevault.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    /** Emits the whole library again every time a game is inserted, updated or deleted. */
    @Query("SELECT * FROM games ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getById(id: Long): Game?

    @Insert
    suspend fun insert(game: Game): Long

    @Update
    suspend fun update(game: Game)

    @Delete
    suspend fun delete(game: Game)
}
