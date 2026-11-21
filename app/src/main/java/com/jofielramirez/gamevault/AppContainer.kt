package com.jofielramirez.gamevault

import android.content.Context
import com.jofielramirez.gamevault.data.CoverStorage
import com.jofielramirez.gamevault.data.local.GameVaultDatabase
import com.jofielramirez.gamevault.data.preferences.DataStoreUserPreferencesRepository
import com.jofielramirez.gamevault.data.preferences.InMemoryUserPreferencesRepository
import com.jofielramirez.gamevault.data.preferences.UserPreferencesRepository
import com.jofielramirez.gamevault.data.remote.RawgClient
import com.jofielramirez.gamevault.data.repository.GameRepository
import com.jofielramirez.gamevault.data.repository.GameSearchRepository
import com.jofielramirez.gamevault.data.repository.OfflineGameRepository
import com.jofielramirez.gamevault.data.repository.RawgGameSearchRepository

/**
 * Manual dependency injection: creates the app-wide dependencies once, the first time they are used.
 * Integration tests build their own container with an in-memory database and a fake game catalog.
 */
class AppContainer(
    gameRepositoryFactory: () -> GameRepository,
    gameSearchRepositoryFactory: () -> GameSearchRepository,
    userPreferencesFactory: () -> UserPreferencesRepository = { InMemoryUserPreferencesRepository() },
    crashReporterFactory: () -> CrashReporter = { CrashReporter.None },
    coverStorageFactory: () -> CoverStorage? = { null }
) {
    val gameRepository: GameRepository by lazy(gameRepositoryFactory)

    /** Online game search. Without a RAWG key in local.properties the form simply works manually. */
    val gameSearchRepository: GameSearchRepository by lazy(gameSearchRepositoryFactory)

    /** Theme, default sort order and crash-report choice (DataStore). */
    val userPreferencesRepository: UserPreferencesRepository by lazy(userPreferencesFactory)

    val crashReporter: CrashReporter by lazy(crashReporterFactory)

    /** Copies of covers picked from the gallery. */
    val coverStorage: CoverStorage? by lazy(coverStorageFactory)

    companion object {
        fun create(context: Context): AppContainer {
            val crashReporter = FirebaseCrashReporter(context)
            return AppContainer(
                gameRepositoryFactory = { OfflineGameRepository(GameVaultDatabase.getInstance(context)) },
                gameSearchRepositoryFactory = {
                    val apiKey = BuildConfig.RAWG_API_KEY
                    if (apiKey.isBlank()) GameSearchRepository.Unavailable
                    else RawgGameSearchRepository(RawgClient.create(apiKey), onError = crashReporter::recordError)
                },
                userPreferencesFactory = { DataStoreUserPreferencesRepository(context) },
                crashReporterFactory = { crashReporter },
                coverStorageFactory = { CoverStorage(context) }
            )
        }
    }
}
