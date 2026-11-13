package com.jofielramirez.gamevault.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.LibrarySummary
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.ui.AppViewModelProvider
import com.jofielramirez.gamevault.ui.components.EmptyMessage
import com.jofielramirez.gamevault.ui.components.EmptyVault
import com.jofielramirez.gamevault.ui.components.GameCover
import com.jofielramirez.gamevault.ui.components.GameProgressBar
import com.jofielramirez.gamevault.ui.components.SectionHeader
import com.jofielramirez.gamevault.ui.components.StatGrid
import com.jofielramirez.gamevault.ui.components.StatItem
import com.jofielramirez.gamevault.ui.components.formatHours
import com.jofielramirez.gamevault.ui.library.GameCard
import com.jofielramirez.gamevault.ui.theme.GameVaultTheme
import com.jofielramirez.gamevault.ui.theme.color

@Composable
fun HomeScreen(
    onGameClick: (Long) -> Unit,
    onAddGame: () -> Unit,
    onSeeLibrary: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        uiState = uiState,
        onGameClick = onGameClick,
        onAddGame = onAddGame,
        onSeeLibrary = onSeeLibrary,
        onLoadSampleLibrary = viewModel::loadSampleLibrary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiState: HomeUiState,
    onGameClick: (Long) -> Unit,
    onAddGame: () -> Unit,
    onSeeLibrary: () -> Unit = {},
    onLoadSampleLibrary: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("GameVault", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Your gaming activity at a glance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (!uiState.isLibraryEmpty) {
                ExtendedFloatingActionButton(
                    onClick = onAddGame,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Add game") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        if (uiState.isLibraryEmpty) {
            EmptyVault(onAddGame, onLoadSampleLibrary, Modifier.padding(innerPadding))
            return@Scaffold
        }
        val summary = uiState.summary
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatGrid(
                    listOf(
                        StatItem("${summary.totalGames}", "Games in library"),
                        StatItem(formatHours(summary.totalHours), "Total playtime"),
                        StatItem("${summary.playingGames}", "Currently playing", GameStatus.PLAYING.color()),
                        StatItem("${summary.completedGames}", "Completed", GameStatus.COMPLETED.color())
                    )
                )
            }
            item { SectionHeader("Continue playing", trailing = "${uiState.continuePlaying.size}") }
            item {
                if (uiState.continuePlaying.isEmpty()) {
                    EmptyMessage(
                        title = "Nothing in progress",
                        body = "Set a game to \"Currently Playing\" or log a session to see it here.",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.continuePlaying, key = { it.id }) { game ->
                            ContinuePlayingCard(game, onClick = { onGameClick(game.id) })
                        }
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader("Recently updated", modifier = Modifier.weight(1f))
                    TextButton(onClick = onSeeLibrary) { Text("See library") }
                }
            }
            items(uiState.recentlyUpdated, key = { it.id }) { game ->
                GameCard(game = game, onClick = { onGameClick(game.id) })
            }
        }
    }
}

/** Compact card for the horizontal "Continue playing" row. */
@Composable
private fun ContinuePlayingCard(game: Game, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(168.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(12.dp)) {
            GameCover(title = game.title, platform = game.platform, width = 144.dp, height = 88.dp, fontSize = MaterialTheme.typography.headlineMedium.fontSize)
            Spacer(Modifier.height(10.dp))
            Text(
                game.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${game.completionPercent}% · ${formatHours(game.hoursPlayed)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            GameProgressBar(game.completionPercent)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1214, widthDp = 360, heightDp = 760)
@Composable
private fun HomePreview() {
    val elden = Game(id = 1, title = "Elden Ring", platform = Platform.PLAYSTATION, genre = "Action RPG", hoursPlayed = 82.0, completionPercent = 48, status = GameStatus.PLAYING)
    val zelda = Game(id = 2, title = "The Legend of Zelda: Breath of the Wild", platform = Platform.NINTENDO_SWITCH, genre = "Action-adventure", hoursPlayed = 35.0, completionPercent = 65, status = GameStatus.PLAYING)
    GameVaultTheme {
        HomeContent(
            uiState = HomeUiState(
                summary = LibrarySummary(7, 2, 2, 204.5, 51.0),
                continuePlaying = listOf(zelda, elden),
                recentlyUpdated = listOf(zelda, elden),
                isLoading = false
            ),
            onGameClick = {}, onAddGame = {}
        )
    }
}
