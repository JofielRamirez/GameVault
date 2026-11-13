package com.jofielramirez.gamevault.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jofielramirez.gamevault.GameVaultApplication
import com.jofielramirez.gamevault.data.repository.GameRepository
import com.jofielramirez.gamevault.ui.details.GameDetailsViewModel
import com.jofielramirez.gamevault.ui.form.GameFormViewModel
import com.jofielramirez.gamevault.ui.home.HomeViewModel
import com.jofielramirez.gamevault.ui.library.LibraryViewModel
import com.jofielramirez.gamevault.ui.stats.StatsViewModel

/** Builds every ViewModel with the repository from the [com.jofielramirez.gamevault.AppContainer]. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { HomeViewModel(repository()) }
        initializer { LibraryViewModel(repository()) }
        initializer { StatsViewModel(repository()) }
        initializer { GameFormViewModel(createSavedStateHandle(), repository()) }
        initializer { GameDetailsViewModel(createSavedStateHandle(), repository()) }
    }
}

fun CreationExtras.gameVaultApplication(): GameVaultApplication =
    this[APPLICATION_KEY] as GameVaultApplication

private fun CreationExtras.repository(): GameRepository = gameVaultApplication().container.gameRepository
