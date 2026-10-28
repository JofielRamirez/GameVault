package com.jofiel.gamevault

import android.app.Application
import com.jofiel.gamevault.data.AppDatabase
import com.jofiel.gamevault.data.GameRepository

/**
 * Manual dependency container for the MVP (no Hilt yet).
 * The database and repository are created once and shared by every screen.
 */
class GameVaultApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val repository: GameRepository by lazy { GameRepository(database.gameDao()) }
}
