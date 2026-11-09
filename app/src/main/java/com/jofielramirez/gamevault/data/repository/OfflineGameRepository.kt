package com.jofielramirez.gamevault.data.repository

import androidx.room.withTransaction
import com.jofielramirez.gamevault.data.local.GameVaultDatabase
import com.jofielramirez.gamevault.data.model.Achievement
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.PlaySession

/** [GameRepository] backed only by the local Room database. */
class OfflineGameRepository(
    private val database: GameVaultDatabase,
    private val clock: () -> Long = System::currentTimeMillis
) : GameRepository {

    private val gameDao = database.gameDao()
    private val sessionDao = database.playSessionDao()
    private val achievementDao = database.achievementDao()

    override fun observeGames() = gameDao.observeAll()
    override fun searchGames(query: String) = gameDao.search(query.trim())
    override fun observeGameDetails(gameId: Long) = gameDao.observeDetails(gameId)
    override fun observeRecentlyUpdated(limit: Int) = gameDao.observeRecentlyUpdated(limit)
    override fun observeMostPlayed(limit: Int) = gameDao.observeMostPlayed(limit)
    override fun observeSummary() = gameDao.observeSummary()
    override fun observeCountByPlatform() = gameDao.observeCountByPlatform()
    override fun observeCountByStatus() = gameDao.observeCountByStatus()

    override suspend fun addGame(game: Game): Long {
        val now = clock()
        return gameDao.insert(game.copy(dateAdded = now, lastUpdated = now))
    }

    override suspend fun updateGame(game: Game) =
        gameDao.update(game.copy(lastUpdated = clock()))

    override suspend fun deleteGame(game: Game) = gameDao.delete(game)

    override suspend fun logPlaySession(session: PlaySession) {
        database.withTransaction {
            val game = requireNotNull(gameDao.getById(session.gameId)) {
                "Game ${session.gameId} does not exist"
            }
            sessionDao.insert(session)

            val progress = session.progressAfter ?: game.completionPercent
            val status = when {
                progress == 100 -> GameStatus.COMPLETED
                game.status == GameStatus.WANT_TO_PLAY -> GameStatus.PLAYING
                else -> game.status
            }
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
