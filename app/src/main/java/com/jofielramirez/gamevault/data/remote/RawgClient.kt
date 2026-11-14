package com.jofielramirez.gamevault.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Builds the Retrofit client for RAWG. */
object RawgClient {

    val json = Json {
        ignoreUnknownKeys = true
        // A null where a list is expected becomes the default (empty list) instead of an error
        coerceInputValues = true
    }

    fun create(apiKey: String, baseUrl: String = RawgApi.BASE_URL): RawgApi {
        val httpClient = OkHttpClient.Builder()
            // RAWG requires the key as a "key" query parameter on every request
            .addInterceptor { chain ->
                val request = chain.request()
                val url = request.url.newBuilder().addQueryParameter("key", apiKey).build()
                chain.proceed(request.newBuilder().url(url).build())
            }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(RawgApi::class.java)
    }
}
