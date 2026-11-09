package com.jofielramirez.gamevault.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameDetails
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.LibrarySummary
import com.jofielramirez.gamevault.data.model.PlatformCount
import com.jofielramirez.gamevault.data.model.StatusCount
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Insert
    suspend fun insert(game: Game): Long

    @Update
    suspend fun update(game: Game)

    @Delete
    suspend fun delete(game: Game)

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getById(id: Long): Game?

    @Query("SELECT * FROM games WHERE id = :id")
    fun observeById(id: Long): Flow<Game?>

    @Transaction
    @Query("SELECT * FROM games WHERE id = :id")
    fun observeDetails(id: Long): Flow<GameDetails?>

    @Query("SELECT * FROM games ORDER BY title COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE title LIKE '%' || :query || '%' ORDER BY title COLLATE NOCASE ASC")
    fun search(query: String): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE status = :status ORDER BY last_updated DESC")
    fun observeByStatus(status: GameStatus): Flow<List<Game>>

    @Query("SELECT * FROM games ORDER BY last_updated DESC LIMIT :limit")
    fun observeRecentlyUpdated(limit: Int): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE hours_played > 0 ORDER BY hours_played DESC LIMIT :limit")
    fun observeMostPlayed(limit: Int): Flow<List<Game>>

    @Query(
        """
        SELECT COUNT(*) AS total_games,
               COALESCE(SUM(status = 'PLAYING'), 0) AS playing_games,
               COALESCE(SUM(status = 'COMPLETED'), 0) AS completed_games,
               COALESCE(SUM(hours_played), 0.0) AS total_hours,
               COALESCE(AVG(completion_percent), 0.0) AS average_completion
        FROM games
        """
    )
    fun observeSummary(): Flow<LibrarySummary>

    @Query("SELECT platform, COUNT(*) AS count FROM games GROUP BY platform ORDER BY count DESC")
    fun observeCountByPlatform(): Flow<List<PlatformCount>>

    @Query("SELECT status, COUNT(*) AS count FROM games GROUP BY status ORDER BY count DESC")
    fun observeCountByStatus(): Flow<List<StatusCount>>
}
