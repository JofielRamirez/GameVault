package com.jofielramirez.gamevault.ui.stats

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.LibrarySummary
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.data.model.PlatformCount
import com.jofielramirez.gamevault.data.model.StatusCount
import com.jofielramirez.gamevault.ui.AppViewModelProvider
import com.jofielramirez.gamevault.ui.components.EmptyMessage
import com.jofielramirez.gamevault.ui.components.StatGrid
import com.jofielramirez.gamevault.ui.components.StatItem
import com.jofielramirez.gamevault.ui.components.formatHours
import com.jofielramirez.gamevault.ui.theme.GameVaultTheme
import com.jofielramirez.gamevault.ui.theme.color
import kotlin.math.roundToInt

@Composable
fun StatsScreen(viewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StatsContent(uiState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsContent(uiState: StatsUiState) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Statistics", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Calculated from your library",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (uiState.isEmpty) {
            Box(Modifier.fillMaxSize().padding(innerPadding).padding(32.dp), contentAlignment = Alignment.Center) {
                EmptyMessage("No statistics yet", "Add some games and your charts will appear here.")
            }
            return@Scaffold
        }
        val summary = uiState.summary
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatGrid(
                    listOf(
                        StatItem(formatHours(summary.totalHours), "Total playtime"),
                        StatItem("${summary.averageCompletion.roundToInt()}%", "Average completion"),
                        StatItem("${uiState.completionRate}%", "Library completed", GameStatus.COMPLETED.color()),
                        StatItem("${summary.totalGames}", "Games")
                    )
                )
            }
            item {
                ChartCard("Games by platform") {
                    val max = uiState.byPlatform.maxOfOrNull { it.count } ?: 1
                    uiState.byPlatform.forEach { item ->
                        BarRow(
                            label = item.platform.displayName,
                            valueText = "${item.count}",
                            fraction = item.count / max.toFloat(),
                            color = item.platform.color()
                        )
                    }
                }
            }
            item { StatusDistributionCard(uiState.byStatus, total = summary.totalGames) }
            if (uiState.mostPlayed.isNotEmpty()) {
                item {
                    ChartCard("Most played") {
                        val max = uiState.mostPlayed.maxOf { it.hoursPlayed }.coerceAtLeast(1.0)
                        uiState.mostPlayed.forEach { game ->
                            BarRow(
                                label = game.title,
                                valueText = formatHours(game.hoursPlayed),
                                fraction = (game.hoursPlayed / max).toFloat(),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

/** One horizontal bar: label on top, bar and value below. Read by TalkBack as "label: value". */
@Composable
private fun BarRow(label: String, valueText: String, fraction: Float, color: Color) {
    Column(Modifier.semantics(mergeDescendants = true) { contentDescription = "$label: $valueText" }) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .weight(1f)
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction.coerceIn(0.02f, 1f))
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
            Text(
                valueText,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(64.dp).padding(start = 12.dp)
            )
        }
    }
}

/** One stacked bar split by status, with a legend underneath. */
@Composable
private fun StatusDistributionCard(byStatus: List<StatusCount>, total: Int) {
    val ordered = GameStatus.entries.mapNotNull { status -> byStatus.firstOrNull { it.status == status } }
        .filter { it.count > 0 }
    ChartCard("Games by status") {
        Row(
            Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(CircleShape)
                .clearAndSetSemantics { }
        ) {
            ordered.forEach { Box(Modifier.weight(it.count.toFloat()).height(16.dp).background(it.status.color())) }
        }
        ordered.forEach { item ->
            val percent = if (total == 0) 0 else (item.count * 100.0 / total).roundToInt()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(item.status.color()))
                Spacer(Modifier.width(10.dp))
                Text(item.status.displayName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(
                    "${item.count} · $percent%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F6F6, widthDp = 360, heightDp = 1100)
@Composable
private fun StatsPreview() {
    GameVaultTheme {
        StatsContent(
            StatsUiState(
                summary = LibrarySummary(7, 2, 2, 204.5, 51.1),
                byPlatform = listOf(
                    PlatformCount(Platform.PC, 2), PlatformCount(Platform.NINTENDO_SWITCH, 2),
                    PlatformCount(Platform.PLAYSTATION, 2), PlatformCount(Platform.XBOX, 1)
                ),
                byStatus = listOf(
                    StatusCount(GameStatus.PLAYING, 2), StatusCount(GameStatus.COMPLETED, 2),
                    StatusCount(GameStatus.WANT_TO_PLAY, 1), StatusCount(GameStatus.ON_HOLD, 1),
                    StatusCount(GameStatus.DROPPED, 1)
                ),
                mostPlayed = listOf(
                    Game(id = 1, title = "Elden Ring", platform = Platform.PLAYSTATION, hoursPlayed = 82.0),
                    Game(id = 2, title = "Hades", platform = Platform.PC, hoursPlayed = 60.0)
                ),
                isLoading = false
            )
        )
    }
}
