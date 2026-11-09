package com.jofielramirez.gamevault.data.repository

import com.jofielramirez.gamevault.data.model.Achievement
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameDetails
import com.jofielramirez.gamevault.data.model.LibrarySummary
import com.jofielramirez.gamevault.data.model.PlatformCount
import com.jofielramirez.gamevault.data.model.PlaySession
import com.jofielramirez.gamevault.data.model.StatusCount
import kotlinx.coroutines.flow.Flow

/**
 * Single entry point the ViewModels use to read and write GameVault data.
 * The UI never talks to Room directly, so the storage can change (e.g. adding
 * Firebase sync) without touching the screens.
 */
interface GameRepository {
    fun observeGames(): Flow<List<Game>>
    fun searchGames(query: String): Flow<List<Game>>
    fun observeGameDetails(gameId: Long): Flow<GameDetails?>
    fun observeRecentlyUpdated(limit: Int = 5): Flow<List<Game>>
    fun observeMostPlayed(limit: Int = 5): Flow<List<Game>>
    fun observeSummary(): Flow<LibrarySummary>
    fun observeCountByPlatform(): Flow<List<PlatformCount>>
    fun observeCountByStatus(): Flow<List<StatusCount>>

    suspend fun getGame(gameId: Long): Game?
    suspend fun addGame(game: Game): Long
    suspend fun updateGame(game: Game)
    suspend fun deleteGame(game: Game)

    /**
     * Records a play session and updates the game's total hours, completion and
     * last-updated date in a single transaction.
     */
    suspend fun logPlaySession(session: PlaySession)

    suspend fun addAchievement(achievement: Achievement): Long
    suspend fun setAchievementUnlocked(achievement: Achievement, unlocked: Boolean)
}
