package com.jofielramirez.gamevault

import android.content.Context
import com.jofielramirez.gamevault.data.local.GameVaultDatabase
import com.jofielramirez.gamevault.data.repository.GameRepository
import com.jofielramirez.gamevault.data.repository.OfflineGameRepository

/** Manual dependency injection: creates the app-wide dependencies once. */
class AppContainer(context: Context) {
    val gameRepository: GameRepository by lazy {
        OfflineGameRepository(GameVaultDatabase.getInstance(context))
    }
}
