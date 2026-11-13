package com.jofielramirez.gamevault.data.repository

import androidx.room.withTransaction
import com.jofielramirez.gamevault.data.local.GameVaultDatabase
import com.jofielramirez.gamevault.data.model.Achievement
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.PlaySession
import com.jofielramirez.gamevault.data.model.ProgressRules
import com.jofielramirez.gamevault.data.model.reconciledWith

/** [GameRepository] backed only by the local Room database. */
class OfflineGameRepository(
    private val database: GameVaultDatabase,
    private val clock: () -> Long = System::currentTimeMillis
) : GameRepository {

    private val gameDao = database.gameDao()
    private val sessionDao = database.playSessionDao()
    private val achievementDao = database.achievementDao()

    override fun observeGames() = gameDao.observeAll()
    override fun observeGamesByStatus(status: GameStatus) = gameDao.observeByStatus(status)
    override fun searchGames(query: String) = gameDao.search(query.trim())
    override fun observeGameDetails(gameId: Long) = gameDao.observeDetails(gameId)
    override fun observeRecentlyUpdated(limit: Int) = gameDao.observeRecentlyUpdated(limit)
    override fun observeMostPlayed(limit: Int) = gameDao.observeMostPlayed(limit)
    override fun observeSummary() = gameDao.observeSummary()
    override fun observeCountByPlatform() = gameDao.observeCountByPlatform()
    override fun observeCountByStatus() = gameDao.observeCountByStatus()

    override suspend fun getGame(gameId: Long): Game? = gameDao.getById(gameId)

    override suspend fun addGame(game: Game): Long {
        val now = clock()
        return gameDao.insert(game.reconciledWith(previous = null).copy(dateAdded = now, lastUpdated = now))
    }

    override suspend fun updateGame(game: Game) {
        val previous = gameDao.getById(game.id)
        gameDao.update(game.reconciledWith(previous).copy(lastUpdated = clock()))
    }

    override suspend fun deleteGame(game: Game) = gameDao.delete(game)

    override suspend fun logPlaySession(session: PlaySession) {
        database.withTransaction {
            val game = requireNotNull(gameDao.getById(session.gameId)) {
                "Game ${session.gameId} does not exist"
            }
            sessionDao.insert(session)

            // Playing a backlog game means the user started it, even without new progress
            val started = if (game.status == GameStatus.WANT_TO_PLAY) GameStatus.PLAYING else game.status
            val (status, progress) = ProgressRules.reconcile(
                previousStatus = game.status,
                previousProgress = game.completionPercent,
                status = started,
                progress = session.progressAfter ?: game.completionPercent
            )
            gameDao.update(
                game.copy(
                    hoursPlayed = game.hoursPlayed + session.durationHours,
                    completionPercent = progress,
                    status = status,
                    lastUpdated = clock()
                )
            )
        }
    }

    override suspend fun addAchievement(achievement: Achievement) = achievementDao.insert(achievement)

    override suspend fun setAchievementUnlocked(achievement: Achievement, unlocked: Boolean) =
        achievementDao.update(if (unlocked) achievement.unlock(clock()) else achievement.lock())
}
