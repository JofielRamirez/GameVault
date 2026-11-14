package com.jofielramirez.gamevault

import android.content.Context
import com.jofielramirez.gamevault.data.local.GameVaultDatabase
import com.jofielramirez.gamevault.data.remote.RawgClient
import com.jofielramirez.gamevault.data.repository.GameRepository
import com.jofielramirez.gamevault.data.repository.GameSearchRepository
import com.jofielramirez.gamevault.data.repository.OfflineGameRepository
import com.jofielramirez.gamevault.data.repository.RawgGameSearchRepository

/** Manual dependency injection: creates the app-wide dependencies once. */
class AppContainer(context: Context) {
    val gameRepository: GameRepository by lazy {
        OfflineGameRepository(GameVaultDatabase.getInstance(context))
    }

    /** Online game search. Without a RAWG key in local.properties the form simply works manually. */
    val gameSearchRepository: GameSearchRepository by lazy {
        val apiKey = BuildConfig.RAWG_API_KEY
        if (apiKey.isBlank()) GameSearchRepository.Unavailable
        else RawgGameSearchRepository(RawgClient.create(apiKey))
    }
}
