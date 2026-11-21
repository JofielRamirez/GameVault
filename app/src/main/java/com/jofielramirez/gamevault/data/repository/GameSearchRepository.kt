package com.jofielramirez.gamevault.data.repository

import android.util.Log
import com.jofielramirez.gamevault.data.remote.GameSuggestion
import com.jofielramirez.gamevault.data.remote.RawgApi
import com.jofielramirez.gamevault.data.remote.RawgMapper
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/** Looks up games in an online catalog so the user does not have to type every detail. */
interface GameSearchRepository {
    /** False when the app was built without an API key; the form then works manually only. */
    val isAvailable: Boolean

    /** Throws [GameSearchException] when the catalog cannot answer; the form stays usable. */
    suspend fun search(query: String): List<GameSuggestion>

    /** Used when no API key is configured. */
    object Unavailable : GameSearchRepository {
        override val isAvailable = false
        override suspend fun search(query: String): List<GameSuggestion> = emptyList()
    }
}

/** Why a search failed, so the form can tell the user something useful. */
enum class SearchFailure {
    /** No internet or the request timed out. */
    CONNECTION,
    /** The API key is missing, wrong or was revoked (HTTP 401 / 403). */
    KEY_REJECTED,
    /** The catalog is down or answered with something unexpected (HTTP 5xx, bad JSON). */
    SERVER
}

class GameSearchException(val failure: SearchFailure, cause: Throwable) : Exception(failure.name, cause)

class RawgGameSearchRepository(
    private val api: RawgApi,
    /** Receives failed searches, so they show up in Crashlytics as non-fatal errors. */
    private val onError: (Throwable) -> Unit = {}
) : GameSearchRepository {
    override val isAvailable = true

    override suspend fun search(query: String): List<GameSuggestion> {
        val start = System.nanoTime()
        try {
            val results = api.searchGames(query.trim()).results.map(RawgMapper::toSuggestion)
            Log.d(TAG, "search \"$query\": ${results.size} results in ${elapsedMs(start)} ms")
            return results
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val failure = when (e) {
                is HttpException -> if (e.code() == 401 || e.code() == 403) SearchFailure.KEY_REJECTED else SearchFailure.SERVER
                is IOException -> SearchFailure.CONNECTION
                is SerializationException -> SearchFailure.SERVER
                else -> SearchFailure.SERVER
            }
            Log.w(TAG, "search \"$query\" failed after ${elapsedMs(start)} ms: $failure", e)
            val error = GameSearchException(failure, e)
            onError(error)
            throw error
        }
    }

    private fun elapsedMs(start: Long) = (System.nanoTime() - start) / 1_000_000

    private companion object {
        const val TAG = "GameVaultSearch"
    }
}
