package com.jofielramirez.gamevault.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jofielramirez.gamevault.GameVaultApplication
import com.jofielramirez.gamevault.ui.details.GameDetailsViewModel
import com.jofielramirez.gamevault.ui.form.GameFormViewModel
import com.jofielramirez.gamevault.ui.library.LibraryViewModel

/** Builds every ViewModel with the repository from the [com.jofielramirez.gamevault.AppContainer]. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { LibraryViewModel(gameVaultApplication().container.gameRepository) }
        initializer {
            GameFormViewModel(createSavedStateHandle(), gameVaultApplication().container.gameRepository)
        }
        initializer {
            GameDetailsViewModel(createSavedStateHandle(), gameVaultApplication().container.gameRepository)
        }
    }
}

fun CreationExtras.gameVaultApplication(): GameVaultApplication =
    this[APPLICATION_KEY] as GameVaultApplication
