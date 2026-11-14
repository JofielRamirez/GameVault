package com.jofielramirez.gamevault.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * The part of the RAWG Video Games Database API (https://rawg.io/apidocs) the app uses.
 * The API key is added to every request by [RawgClient], so it is not a parameter here.
 */
interface RawgApi {

    /** Searches games by name; results come sorted by relevance. */
    @GET("games")
    suspend fun searchGames(
        @Query("search") query: String,
        @Query("page_size") pageSize: Int = 8,
        @Query("search_precise") precise: Boolean = true
    ): RawgSearchResponse

    companion object {
        const val BASE_URL = "https://api.rawg.io/api/"
    }
}
