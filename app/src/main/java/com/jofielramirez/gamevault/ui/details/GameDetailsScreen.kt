package com.jofielramirez.gamevault.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jofielramirez.gamevault.data.model.Achievement
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameDetails
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.PlaySession
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.ui.AppViewModelProvider
import com.jofielramirez.gamevault.ui.components.GameCover
import com.jofielramirez.gamevault.ui.components.GameProgressBar
import com.jofielramirez.gamevault.ui.components.RatingStars
import com.jofielramirez.gamevault.ui.components.SectionHeader
import com.jofielramirez.gamevault.ui.components.StatusChip
import com.jofielramirez.gamevault.ui.components.formatHours
import com.jofielramirez.gamevault.ui.components.metadataLine
import com.jofielramirez.gamevault.ui.theme.GameVaultTheme
import com.jofielramirez.gamevault.ui.theme.color
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun GameDetailsScreen(
    onNavigateUp: () -> Unit,
    onEditGame: (Long) -> Unit,
    viewModel: GameDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    GameDetailsContent(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onEdit = { id -> onEditGame(id) },
        onDelete = { scope.launch { viewModel.deleteGame(); onNavigateUp() } },
        onLogSession = viewModel::logSession,
        onMarkCompleted = viewModel::markCompleted,
        onToggleAchievement = viewModel::toggleAchievement,
        onAddAchievement = viewModel::addAchievement
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailsContent(
    uiState: GameDetailsUiState,
    onNavigateUp: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: () -> Unit,
    onLogSession: (hours: Double, progressAfter: Int) -> Unit,
    onMarkCompleted: () -> Unit,
    onToggleAchievement: (Achievement) -> Unit,
    onAddAchievement: (String) -> Unit
) {
    var showLogDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val details = uiState.details

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to library")
                    }
                },
                actions = {
                    if (details != null) {
                        IconButton(onClick = { onEdit(details.game.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit game")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete game")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (details == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                if (uiState.isLoading) CircularProgressIndicator()
            }
            return@Scaffold
        }
        val game = details.game
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { DetailsHeader(game) }
            item {
                ProgressCard(
                    game = game,
                    onLogSession = { showLogDialog = true },
                    onMarkCompleted = onMarkCompleted
                )
            }
            item {
                AchievementsSection(
                    achievements = details.achievements,
                    onToggle = onToggleAchievement,
                    onAdd = onAddAchievement
                )
            }
            if (game.notes.isNotBlank()) {
                item { NotesCard(game.notes) }
            }
            item {
                SectionHeader(
                    "Play history",
                    trailing = if (details.sessions.isEmpty()) null else "${details.sessions.size} sessions"
                )
            }
            if (details.sessions.isEmpty()) {
                item {
                    Text(
                        "No sessions yet. Tap \"Log session\" after you play.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(details.sessions.sortedByDescending { it.playedOn }, key = { it.id }) { session ->
                SessionRow(session)
            }
        }
    }

    if (showLogDialog && details != null) {
        LogSessionDialog(
            currentProgress = details.game.completionPercent,
            onDismiss = { showLogDialog = false },
            onConfirm = { hours, progress ->
                onLogSession(hours, progress)
                showLogDialog = false
            }
        )
    }
    if (showDeleteDialog && details != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${details.game.title}?") },
            text = { Text("Its play history and achievements will be deleted too. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun DetailsHeader(game: Game) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        GameCover(title = game.title, platform = game.platform, width = 96.dp, height = 128.dp, fontSize = 32.sp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(game.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                game.metadataLine(includeYear = true),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StatusChip(game.status)
            RatingStars(rating = game.rating)
        }
    }
}

@Composable
private fun ProgressCard(game: Game, onLogSession: () -> Unit, onMarkCompleted: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row {
                BigStat(value = "${game.completionPercent}%", label = "Completed", modifier = Modifier.weight(1f))
                BigStat(value = formatHours(game.hoursPlayed), label = "Played", modifier = Modifier.weight(1f))
            }
            GameProgressBar(game.completionPercent, height = 10.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onLogSession,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Log session")
                }
                OutlinedButton(
                    onClick = onMarkCompleted,
                    enabled = !game.isCompleted,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("mark_completed"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GameStatus.COMPLETED.color())
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (game.isCompleted) "Completed" else "Mark completed")
                }
            }
        }
    }
}

@Composable
private fun BigStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AchievementsSection(
    achievements: List<Achievement>,
    onToggle: (Achievement) -> Unit,
    onAdd: (String) -> Unit
) {
    var newTitle by remember { mutableStateOf("") }
    val unlocked = achievements.count { it.isUnlocked }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader("Achievements", trailing = if (achievements.isEmpty()) null else "$unlocked / ${achievements.size}")
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(Modifier.padding(vertical = 4.dp)) {
                val sorted = achievements.sortedWith(compareBy<Achievement> { it.isUnlocked }.thenBy { it.title.lowercase() })
                sorted.forEachIndexed { index, achievement ->
                    AchievementRow(achievement, onToggle = { onToggle(achievement) })
                    if (index < sorted.lastIndex) {
                        HorizontalDivider(Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        placeholder = { Text("New achievement") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { onAdd(newTitle); newTitle = "" }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("achievement_field")
                    )
                    IconButton(onClick = { onAdd(newTitle); newTitle = "" }, enabled = newTitle.isNotBlank()) {
                        Icon(Icons.Filled.Add, contentDescription = "Add achievement")
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementRow(achievement: Achievement, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = achievement.isUnlocked, role = Role.Checkbox, onValueChange = { onToggle() })
            .heightIn(min = 56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = achievement.isUnlocked,
            onCheckedChange = null,
            modifier = Modifier.padding(horizontal = 12.dp),
            colors = CheckboxDefaults.colors(checkedColor = GameStatus.COMPLETED.color())
        )
        Column(Modifier.weight(1f).padding(end = 16.dp, top = 8.dp, bottom = 8.dp)) {
            Text(
                achievement.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (achievement.isUnlocked) FontWeight.Normal else FontWeight.Medium,
                color = if (achievement.isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (achievement.isUnlocked) TextDecoration.LineThrough else null
            )
            val subtitle = achievement.unlockedAt?.let { "Unlocked ${formatDate(it)}" } ?: achievement.description
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader("Your notes")
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Text(notes, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun SessionRow(session: PlaySession) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(formatDate(session.playedOn), style = MaterialTheme.typography.titleSmall)
            if (session.note.isNotBlank()) {
                Text(session.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("+${formatHours(session.durationHours)}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            session.progressAfter?.let {
                Text("→ $it%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LogSessionDialog(
    currentProgress: Int,
    onDismiss: () -> Unit,
    onConfirm: (hours: Double, progressAfter: Int) -> Unit
) {
    var hoursText by remember { mutableStateOf("") }
    var progress by remember { mutableIntStateOf(currentProgress) }
    val hours = hoursText.replace(',', '.').toDoubleOrNull()
    val hoursValid = hours != null && hours > 0 && hours <= PlaySession.MAX_SESSION_HOURS

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log a play session") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = hoursText,
                    onValueChange = { new -> hoursText = new.filter { it.isDigit() || it == '.' || it == ',' } },
                    label = { Text("Hours played") },
                    suffix = { Text("h") },
                    isError = hoursText.isNotEmpty() && !hoursValid,
                    supportingText = { Text("Between 0.1 and 24 hours") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Progress now", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Text("$progress%", style = MaterialTheme.typography.titleMedium)
                }
                Slider(
                    value = progress.toFloat(),
                    onValueChange = { progress = it.roundToInt() },
                    valueRange = 0f..100f
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(hours!!, progress) }, enabled = hoursValid) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatDate(epochMillis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(epochMillis))

@Preview(showBackground = true, backgroundColor = 0xFF0F0E17, widthDp = 360, heightDp = 1000)
@Composable
private fun GameDetailsPreview() {
    val game = Game(
        id = 1, title = "The Legend of Zelda: Breath of the Wild", platform = Platform.NINTENDO_SWITCH,
        genre = "Action-adventure", releaseYear = 2017, rating = 5, hoursPlayed = 35.0,
        completionPercent = 65, status = GameStatus.PLAYING, notes = "Gliding off the Great Plateau."
    )
    GameVaultTheme {
        GameDetailsContent(
            uiState = GameDetailsUiState(
                details = GameDetails(
                    game = game,
                    sessions = listOf(PlaySession(id = 1, gameId = 1, durationHours = 3.0, progressAfter = 65)),
                    achievements = listOf(
                        Achievement(id = 1, gameId = 1, title = "Free Vah Ruta", isUnlocked = true, unlockedAt = 0L),
                        Achievement(id = 2, gameId = 1, title = "Defeat Calamity Ganon")
                    )
                ),
                isLoading = false
            ),
            onNavigateUp = {}, onEdit = {}, onDelete = {}, onLogSession = { _, _ -> },
            onMarkCompleted = {}, onToggleAchievement = {}, onAddAchievement = {}
        )
    }
}
