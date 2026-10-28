package com.jofiel.gamevault.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jofiel.gamevault.ui.form.GameFormScreen
import com.jofiel.gamevault.ui.form.GameFormViewModel
import com.jofiel.gamevault.ui.library.LibraryScreen

object Routes {
    const val LIBRARY = "library"
    private const val FORM = "form"
    private const val ARG = GameFormViewModel.GAME_ID_ARG

    /** Route pattern registered in the NavHost: form?gameId={gameId} */
    const val FORM_PATTERN = "$FORM?$ARG={$ARG}"

    /** No id = add a new game; an id = edit that game. */
    fun form(gameId: Long? = null): String =
        if (gameId == null) FORM else "$FORM?$ARG=$gameId"
}

@Composable
fun GameVaultNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LIBRARY) {
        composable(Routes.LIBRARY) {
            LibraryScreen(
                onAddGame = { navController.navigate(Routes.form()) },
                onOpenGame = { id -> navController.navigate(Routes.form(id)) },
            )
        }
        composable(
            route = Routes.FORM_PATTERN,
            arguments = listOf(
                navArgument(GameFormViewModel.GAME_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = GameFormViewModel.NEW_GAME_ID
                },
            ),
        ) {
            GameFormScreen(onBack = { navController.navigateUp() })
        }
    }
}
