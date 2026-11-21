package com.jofielramirez.gamevault.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jofielramirez.gamevault.BuildConfig
import com.jofielramirez.gamevault.data.preferences.ThemeMode
import com.jofielramirez.gamevault.data.preferences.UserPreferences
import com.jofielramirez.gamevault.ui.AppViewModelProvider
import com.jofielramirez.gamevault.ui.components.RawgAttribution
import com.jofielramirez.gamevault.ui.components.readableWidth
import com.jofielramirez.gamevault.ui.library.SortOrder
import com.jofielramirez.gamevault.ui.theme.GameVaultTheme

@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        message = viewModel.message,
        onNavigateUp = onNavigateUp,
        onThemeChange = viewModel::setThemeMode,
        onDefaultSortChange = viewModel::setDefaultSort,
        onCrashReportsChange = viewModel::setCrashReportsEnabled,
        onLoadSamples = viewModel::loadSampleLibrary,
        onDeleteAll = viewModel::deleteAllGames
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    message: String? = null,
    onNavigateUp: () -> Unit = {},
    onThemeChange: (ThemeMode) -> Unit = {},
    onDefaultSortChange: (SortOrder) -> Unit = {},
    onCrashReportsChange: (Boolean) -> Unit = {},
    onLoadSamples: () -> Unit = {},
    onDeleteAll: () -> Unit = {}
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val prefs = uiState.preferences

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .readableWidth(720.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsCard("Appearance") {
                    Column(Modifier.selectableGroup()) {
                        ThemeMode.entries.forEach { mode ->
                            RadioRow(mode.label, selected = prefs.themeMode == mode, onClick = { onThemeChange(mode) })
                        }
                    }
                }
            }
            item {
                SettingsCard("Library", "Order of your games when the library opens") {
                    Column(Modifier.selectableGroup()) {
                        SortOrder.entries.forEach { sort ->
                            RadioRow(sort.label, selected = prefs.defaultSort == sort, onClick = { onDefaultSortChange(sort) })
                        }
                    }
                }
            }
            item {
                SettingsCard("Your data", "${uiState.totalGames} games saved on this phone") {
                    OutlinedButton(
                        onClick = onLoadSamples,
                        enabled = uiState.totalGames == 0,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Load sample games") }
                    Text(
                        "Available when the library is empty. Useful to explore the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = { confirmDelete = true },
                        enabled = uiState.totalGames > 0,
                        modifier = Modifier.testTag("delete_all")
                    ) { Text("Delete all games", color = MaterialTheme.colorScheme.error) }
                    message?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            item {
                SettingsCard("Privacy") {
                    SwitchRow(
                        title = "Send crash reports",
                        subtitle = if (uiState.crashReportsAvailable) {
                            "Anonymous error reports help fix bugs (Firebase Crashlytics). Your games are never sent."
                        } else {
                            "Not configured in this build."
                        },
                        checked = prefs.crashReportsEnabled && uiState.crashReportsAvailable,
                        enabled = uiState.crashReportsAvailable,
                        onCheckedChange = onCrashReportsChange
                    )
                }
            }
            item {
                SettingsCard("About") {
                    InfoRow("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    InfoRow("Game search", if (uiState.searchAvailable) "On (RAWG)" else "Off (no API key)")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    InfoRow("Storage", "Only on this phone, no account needed")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        "Capstone project by Jofiel Ramírez — MDI2, San Diego Global Knowledge University.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    RawgAttribution()
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete all games?") },
            text = { Text("Every game, play session and achievement on this phone will be deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDeleteAll() }) {
                    Text("Delete all", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SettingsCard(title: String, subtitle: String? = null, content: @Composable () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { heading() }
            )
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F6F6, widthDp = 360, heightDp = 1200)
@Composable
private fun SettingsPreview() {
    GameVaultTheme {
        SettingsContent(
            SettingsUiState(UserPreferences(), totalGames = 7, searchAvailable = true, crashReportsAvailable = true, isLoading = false)
        )
    }
}
