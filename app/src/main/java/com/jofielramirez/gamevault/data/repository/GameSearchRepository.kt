package com.jofielramirez.gamevault.data.repository

import com.jofielramirez.gamevault.data.remote.GameSuggestion
import com.jofielramirez.gamevault.data.remote.RawgApi
import com.jofielramirez.gamevault.data.remote.RawgMapper

/** Looks up games in an online catalog so the user does not have to type every detail. */
interface GameSearchRepository {
    /** False when the app was built without an API key; the form then works manually only. */
    val isAvailable: Boolean

    /** Throws on network or server errors; the caller shows a message and keeps the form usable. */
    suspend fun search(query: String): List<GameSuggestion>

    /** Used when no API key is configured. */
    object Unavailable : GameSearchRepository {
        override val isAvailable = false
        override suspend fun search(query: String): List<GameSuggestion> = emptyList()
    }
}

class RawgGameSearchRepository(private val api: RawgApi) : GameSearchRepository {
    override val isAvailable = true

    override suspend fun search(query: String): List<GameSuggestion> =
        api.searchGames(query.trim()).results.map(RawgMapper::toSuggestion)
}
