package com.jofielramirez.gamevault.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jofielramirez.gamevault.ui.details.GameDetailsScreen
import com.jofielramirez.gamevault.ui.form.GameFormScreen
import com.jofielramirez.gamevault.ui.library.LibraryScreen

object GameVaultDestinations {
    const val GAME_ID_ARG = "gameId"
    const val LIBRARY = "library"
    const val ADD_GAME = "game/add"
    const val GAME_DETAILS = "game/{$GAME_ID_ARG}"
    const val EDIT_GAME = "game/{$GAME_ID_ARG}/edit"

    fun gameDetails(id: Long) = "game/$id"
    fun editGame(id: Long) = "game/$id/edit"
}

@Composable
fun GameVaultNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val gameIdArgument = listOf(navArgument(GameVaultDestinations.GAME_ID_ARG) { type = NavType.LongType })

    NavHost(
        navController = navController,
        startDestination = GameVaultDestinations.LIBRARY,
        modifier = modifier
    ) {
        composable(GameVaultDestinations.LIBRARY) {
            LibraryScreen(
                onAddGame = { navController.navigate(GameVaultDestinations.ADD_GAME) },
                onGameClick = { navController.navigate(GameVaultDestinations.gameDetails(it)) }
            )
        }
        composable(GameVaultDestinations.ADD_GAME) {
            GameFormScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(GameVaultDestinations.EDIT_GAME, arguments = gameIdArgument) {
            GameFormScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(GameVaultDestinations.GAME_DETAILS, arguments = gameIdArgument) {
            GameDetailsScreen(
                onNavigateUp = { navController.navigateUp() },
                onEditGame = { navController.navigate(GameVaultDestinations.editGame(it)) }
            )
        }
    }
}
