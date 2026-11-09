package com.jofielramirez.gamevault.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
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
import com.jofielramirez.gamevault.ui.components.GameCover
import com.jofielramirez.gamevault.ui.components.GameProgressBar
import com.jofielramirez.gamevault.ui.components.SectionHeader
import com.jofielramirez.gamevault.ui.components.StatCard
import com.jofielramirez.gamevault.ui.components.StatusChip
import com.jofielramirez.gamevault.ui.components.formatHours
import com.jofielramirez.gamevault.ui.components.metadataLine
import com.jofielramirez.gamevault.ui.theme.GameVaultTheme
import com.jofielramirez.gamevault.ui.theme.color

@Composable
fun LibraryScreen(
    onAddGame: () -> Unit,
    onGameClick: (Long) -> Unit,
    viewModel: LibraryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LibraryContent(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onStatusFilterChange = viewModel::onStatusFilterChange,
        onGameClick = onGameClick,
        onAddGame = onAddGame,
        onLoadSampleLibrary = viewModel::loadSampleLibrary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryContent(
    uiState: LibraryUiState,
    onQueryChange: (String) -> Unit,
    onStatusFilterChange: (GameStatus?) -> Unit,
    onGameClick: (Long) -> Unit,
    onAddGame: () -> Unit,
    onLoadSampleLibrary: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("GameVault", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Your personal game library",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                scrollBehavior = scrollBehavior
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
            EmptyLibrary(
                onAddGame = onAddGame,
                onLoadSampleLibrary = onLoadSampleLibrary,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("library_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { SummaryRow(uiState.summary) }
                item {
                    SearchField(
                        query = uiState.query,
                        onQueryChange = onQueryChange,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                item { StatusFilterRow(selected = uiState.statusFilter, onSelect = onStatusFilterChange) }
                item {
                    SectionHeader(
                        title = "My games",
                        trailing = "${uiState.games.size}",
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (uiState.games.isEmpty()) {
                    item {
                        EmptyMessage(
                            title = "No games match",
                            body = "Try another search or remove the status filter.",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                items(uiState.games, key = { it.id }) { game ->
                    GameCard(game = game, onClick = { onGameClick(game.id) })
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(summary: LibrarySummary) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("${summary.totalGames}", "Games", Modifier.weight(1f))
        StatCard("${summary.playingGames}", "Playing", Modifier.weight(1f), accent = GameStatus.PLAYING.color())
        StatCard("${summary.completedGames}", "Completed", Modifier.weight(1f), accent = GameStatus.COMPLETED.color())
        StatCard(formatHours(summary.totalHours).removeSuffix(" h"), "Hours", Modifier.weight(1f))
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search by title") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Composable
private fun StatusFilterRow(selected: GameStatus?, onSelect: (GameStatus?) -> Unit) {
    val options = listOf<GameStatus?>(null) + GameStatus.entries
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { status ->
            FilterChip(
                selected = selected == status,
                onClick = { onSelect(status) },
                label = { Text(status?.displayName ?: "All") },
                shape = MaterialTheme.shapes.small,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun GameCard(game: Game, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GameCover(title = game.title, platform = game.platform)
            Column(Modifier.weight(1f)) {
                Text(
                    game.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    game.metadataLine(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(game.status)
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${formatHours(game.hoursPlayed)} · ${game.completionPercent}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(10.dp))
                GameProgressBar(game.completionPercent)
            }
        }
    }
}

@Composable
private fun EmptyLibrary(onAddGame: () -> Unit, onLoadSampleLibrary: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GameCover(title = "Game Vault", platform = Platform.OTHER, width = 88.dp, height = 112.dp)
            Spacer(Modifier.height(24.dp))
            Text("Your vault is empty", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "Add the games you own or want to play to start tracking hours, progress and achievements.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onAddGame, modifier = Modifier.height(48.dp)) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Add your first game")
            }
            TextButton(onClick = onLoadSampleLibrary) { Text("Explore with sample games") }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0E17, widthDp = 360, heightDp = 760)
@Composable
private fun LibraryPreview() {
    GameVaultTheme {
        LibraryContent(
            uiState = LibraryUiState(
                games = listOf(
                    Game(id = 1, title = "Elden Ring", platform = Platform.PLAYSTATION, genre = "Action RPG", hoursPlayed = 82.0, completionPercent = 48, status = GameStatus.PLAYING),
                    Game(id = 2, title = "Hades", platform = Platform.PC, genre = "Roguelike", hoursPlayed = 60.0, completionPercent = 100, status = GameStatus.COMPLETED)
                ),
                summary = LibrarySummary(2, 1, 1, 142.0, 74.0),
                isLoading = false
            ),
            onQueryChange = {}, onStatusFilterChange = {}, onGameClick = {}, onAddGame = {}, onLoadSampleLibrary = {}
        )
    }
}
