package com.jofielramirez.gamevault.ui.library

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.ui.AppViewModelProvider
import com.jofielramirez.gamevault.ui.components.DropdownFilterChip
import com.jofielramirez.gamevault.ui.components.EmptyMessage
import com.jofielramirez.gamevault.ui.components.EmptyVault
import com.jofielramirez.gamevault.ui.components.GameCover
import com.jofielramirez.gamevault.ui.components.GameProgressBar
import com.jofielramirez.gamevault.ui.components.SectionHeader
import com.jofielramirez.gamevault.ui.components.StatusChip
import com.jofielramirez.gamevault.ui.components.formatHours
import com.jofielramirez.gamevault.ui.components.metadataLine
import com.jofielramirez.gamevault.ui.theme.GameVaultTheme

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
        onPlatformFilterChange = viewModel::onPlatformFilterChange,
        onGenreFilterChange = viewModel::onGenreFilterChange,
        onSortChange = viewModel::onSortChange,
        onClearFilters = viewModel::clearFilters,
        onGameClick = onGameClick,
        onAddGame = onAddGame,
        onLoadSampleLibrary = viewModel::loadSampleLibrary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryContent(
    uiState: LibraryUiState,
    onGameClick: (Long) -> Unit,
    onAddGame: () -> Unit,
    onQueryChange: (String) -> Unit = {},
    onStatusFilterChange: (GameStatus?) -> Unit = {},
    onPlatformFilterChange: (Platform?) -> Unit = {},
    onGenreFilterChange: (String?) -> Unit = {},
    onSortChange: (SortOrder) -> Unit = {},
    onClearFilters: () -> Unit = {},
    onLoadSampleLibrary: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My library", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "${uiState.totalGames} games in your vault",
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
            EmptyVault(onAddGame, onLoadSampleLibrary, Modifier.padding(innerPadding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("library_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SearchField(query = uiState.query, onQueryChange = onQueryChange) }
            item {
                SortAndFilterRow(
                    uiState = uiState,
                    onSortChange = onSortChange,
                    onPlatformFilterChange = onPlatformFilterChange,
                    onGenreFilterChange = onGenreFilterChange
                )
            }
            item { StatusFilterRow(selected = uiState.filter.status, onSelect = onStatusFilterChange) }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    SectionHeader(
                        title = uiState.filter.sort.label,
                        trailing = if (uiState.filter.hasActiveFilters) null else "${uiState.games.size}",
                        modifier = Modifier.weight(1f)
                    )
                    if (uiState.filter.hasActiveFilters) {
                        TextButton(onClick = onClearFilters) { Text("Clear filters (${uiState.games.size})") }
                    }
                }
            }
            if (uiState.games.isEmpty()) {
                item {
                    EmptyMessage(
                        title = "No games match",
                        body = "Try another search or clear the filters.",
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

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
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
private fun SortAndFilterRow(
    uiState: LibraryUiState,
    onSortChange: (SortOrder) -> Unit,
    onPlatformFilterChange: (Platform?) -> Unit,
    onGenreFilterChange: (String?) -> Unit
) {
    val filter = uiState.filter
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            DropdownFilterChip(
                label = "Sort: ${filter.sort.label}",
                selected = filter.sort != SortOrder.TITLE,
                options = SortOrder.entries.map { it.label to { onSortChange(it) } }
            )
        }
        item {
            DropdownFilterChip(
                label = filter.platform?.displayName ?: "Platform",
                selected = filter.platform != null,
                options = listOf("All platforms" to { onPlatformFilterChange(null) }) +
                    uiState.availablePlatforms.map { it.displayName to { onPlatformFilterChange(it) } }
            )
        }
        if (uiState.availableGenres.isNotEmpty()) {
            item {
                DropdownFilterChip(
                    label = filter.genre ?: "Genre",
                    selected = filter.genre != null,
                    options = listOf("All genres" to { onGenreFilterChange(null) }) +
                        uiState.availableGenres.map { it to { onGenreFilterChange(it) } }
                )
            }
        }
    }
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

@Preview(showBackground = true, backgroundColor = 0xFFF4F6F6, widthDp = 360, heightDp = 760)
@Composable
private fun LibraryPreview() {
    GameVaultTheme {
        LibraryContent(
            uiState = LibraryUiState(
                games = listOf(
                    Game(id = 1, title = "Elden Ring", platform = Platform.PLAYSTATION, genre = "Action RPG", hoursPlayed = 82.0, completionPercent = 48, status = GameStatus.PLAYING),
                    Game(id = 2, title = "Hades", platform = Platform.PC, genre = "Roguelike", hoursPlayed = 60.0, completionPercent = 100, status = GameStatus.COMPLETED)
                ),
                totalGames = 2,
                availablePlatforms = listOf(Platform.PC, Platform.PLAYSTATION),
                availableGenres = listOf("Action RPG", "Roguelike"),
                isLoading = false
            ),
            onGameClick = {}, onAddGame = {}
        )
    }
}
