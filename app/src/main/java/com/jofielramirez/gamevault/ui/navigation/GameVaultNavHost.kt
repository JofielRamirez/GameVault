package com.jofielramirez.gamevault.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jofielramirez.gamevault.ui.components.BarChartIcon
import com.jofielramirez.gamevault.ui.details.GameDetailsScreen
import com.jofielramirez.gamevault.ui.form.GameFormScreen
import com.jofielramirez.gamevault.ui.home.HomeScreen
import com.jofielramirez.gamevault.ui.library.LibraryScreen
import com.jofielramirez.gamevault.ui.stats.StatsScreen

object GameVaultDestinations {
    const val GAME_ID_ARG = "gameId"
    const val HOME = "home"
    const val LIBRARY = "library"
    const val STATS = "stats"
    const val ADD_GAME = "game/add"
    const val GAME_DETAILS = "game/{$GAME_ID_ARG}"
    const val EDIT_GAME = "game/{$GAME_ID_ARG}/edit"

    fun gameDetails(id: Long) = "game/$id"
    fun editGame(id: Long) = "game/$id/edit"
}

private data class TopLevelDestination(val route: String, val label: String, val icon: ImageVector)

private val topLevelDestinations = listOf(
    TopLevelDestination(GameVaultDestinations.HOME, "Home", Icons.Filled.Home),
    TopLevelDestination(GameVaultDestinations.LIBRARY, "Library", Icons.AutoMirrored.Filled.List),
    TopLevelDestination(GameVaultDestinations.STATS, "Statistics", BarChartIcon)
)

/** App shell: bottom navigation on the three main screens, hidden on details and forms. */
@Composable
fun GameVaultApp(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = topLevelDestinations.any { it.route == currentDestination?.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                    topLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                            onClick = { navController.navigateToTopLevel(destination.route) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        GameVaultNavHost(
            navController = navController,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        )
    }
}

/** Switches tabs keeping one copy of each tab and its scroll state. */
private fun NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * A screen only reacts to navigation taps while it is fully visible (RESUMED). Without this,
 * a quick double tap on a game opened its details twice, and a double tap on Back or Save
 * closed two screens at once.
 */
private val NavBackStackEntry.isResumed: Boolean
    get() = lifecycle.currentState == Lifecycle.State.RESUMED

private fun NavHostController.navigateFrom(entry: NavBackStackEntry, route: String) {
    if (entry.isResumed) navigate(route)
}

private fun NavHostController.navigateUpFrom(entry: NavBackStackEntry) {
    if (entry.isResumed) navigateUp()
}

@Composable
fun GameVaultNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val gameIdArgument = listOf(navArgument(GameVaultDestinations.GAME_ID_ARG) { type = NavType.LongType })

    NavHost(
        navController = navController,
        startDestination = GameVaultDestinations.HOME,
        modifier = modifier
    ) {
        composable(GameVaultDestinations.HOME) { entry ->
            HomeScreen(
                onGameClick = { navController.navigateFrom(entry, GameVaultDestinations.gameDetails(it)) },
                onAddGame = { navController.navigateFrom(entry, GameVaultDestinations.ADD_GAME) },
                onSeeLibrary = { navController.navigateToTopLevel(GameVaultDestinations.LIBRARY) }
            )
        }
        composable(GameVaultDestinations.LIBRARY) { entry ->
            LibraryScreen(
                onAddGame = { navController.navigateFrom(entry, GameVaultDestinations.ADD_GAME) },
                onGameClick = { navController.navigateFrom(entry, GameVaultDestinations.gameDetails(it)) }
            )
        }
        composable(GameVaultDestinations.STATS) {
            StatsScreen()
        }
        composable(GameVaultDestinations.ADD_GAME) { entry ->
            GameFormScreen(onNavigateUp = { navController.navigateUpFrom(entry) })
        }
        composable(GameVaultDestinations.EDIT_GAME, arguments = gameIdArgument) { entry ->
            GameFormScreen(onNavigateUp = { navController.navigateUpFrom(entry) })
        }
        composable(GameVaultDestinations.GAME_DETAILS, arguments = gameIdArgument) { entry ->
            GameDetailsScreen(
                onNavigateUp = { navController.navigateUpFrom(entry) },
                onEditGame = { navController.navigateFrom(entry, GameVaultDestinations.editGame(it)) }
            )
        }
    }
}
