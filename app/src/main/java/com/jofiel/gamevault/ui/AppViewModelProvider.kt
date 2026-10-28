package com.jofiel.gamevault.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jofiel.gamevault.GameVaultApp
import com.jofiel.gamevault.ui.form.GameFormViewModel
import com.jofiel.gamevault.ui.library.LibraryViewModel

/** Builds every ViewModel with the shared repository from [GameVaultApp]. */
object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            LibraryViewModel(gameVaultApp().repository)
        }
        initializer {
            GameFormViewModel(
                savedStateHandle = createSavedStateHandle(),
                repository = gameVaultApp().repository,
            )
        }
    }
}

private fun CreationExtras.gameVaultApp(): GameVaultApp =
    this[APPLICATION_KEY] as GameVaultApp
