package com.jofiel.gamevault.data

import kotlinx.coroutines.flow.Flow

/** Single access point to game data for every ViewModel. */
class GameRepository(private val dao: GameDao) {

    fun observeGames(): Flow<List<Game>> = dao.observeAll()

    suspend fun getGame(id: Long): Game? = dao.getById(id)

    /** Inserts a new game (id == 0) or updates an existing one. Returns the game's id. */
    suspend fun save(game: Game): Long {
        val stamped = game.copy(updatedAt = System.currentTimeMillis())
        return if (game.id == 0L) {
            dao.insert(stamped)
        } else {
            dao.update(stamped)
            game.id
        }
    }

    suspend fun delete(game: Game) = dao.delete(game)
}
