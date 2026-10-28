package com.jofiel.gamevault.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jofiel.gamevault.data.GameStatus
import com.jofiel.gamevault.data.Platform
import com.jofiel.gamevault.ui.AppViewModelProvider
import com.jofiel.gamevault.ui.components.DropdownField
import kotlin.math.roundToInt

object GameFormTestTags {
    const val TITLE = "form_title"
    const val GENRE = "form_genre"
    const val HOURS = "form_hours"
    const val NOTES = "form_notes"
    const val SAVE = "form_save_button"
    const val DELETE = "form_delete_button"
    const val CONFIRM_DELETE = "form_confirm_delete"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameFormScreen(
    onBack: () -> Unit,
    viewModel: GameFormViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state = viewModel.state
    val errors = viewModel.errors
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit game" else "Add game") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag(GameFormTestTags.DELETE),
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete game")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title*") },
                singleLine = true,
                isError = errors.title != null,
                supportingText = if (errors.title != null) {
                    { Text(errors.title.orEmpty()) }
                } else {
                    null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(GameFormTestTags.TITLE),
            )

            DropdownField(
                label = "Platform*",
                options = Platform.entries,
                selected = state.platform,
                optionLabel = { it.label },
                onSelected = viewModel::onPlatformChange,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.genre,
                onValueChange = viewModel::onGenreChange,
                label = { Text("Genre") },
                placeholder = { Text("e.g. Action RPG") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(GameFormTestTags.GENRE),
            )

            DropdownField(
                label = "Status",
                options = GameStatus.entries,
                selected = state.status,
                optionLabel = { it.label },
                onSelected = viewModel::onStatusChange,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.hoursPlayed,
                onValueChange = viewModel::onHoursChange,
                label = { Text("Hours played") },
                placeholder = { Text("0") },
                suffix = { Text("h") },
                singleLine = true,
                isError = errors.hours != null,
                supportingText = if (errors.hours != null) {
                    { Text(errors.hours.orEmpty()) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(GameFormTestTags.HOURS),
            )

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Completion", style = MaterialTheme.typography.labelLarge)
                    Box(Modifier.weight(1f))
                    Text(
                        text = "${state.progress}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Slider(
                    value = state.progress.toFloat(),
                    onValueChange = { viewModel.onProgressChange(it.roundToInt()) },
                    valueRange = 0f..100f,
                    steps = 19, // 5% increments
                )
            }

            Column {
                Text("Your rating", style = MaterialTheme.typography.labelLarge)
                Row {
                    for (star in 1..5) {
                        IconButton(
                            onClick = {
                                // Tapping the current rating again clears it.
                                viewModel.onRatingChange(if (state.rating == star) 0 else star)
                            },
                        ) {
                            Icon(
                                imageVector = if (star <= state.rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Rate $star stars",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes") },
                minLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(GameFormTestTags.NOTES),
            )

            Button(
                onClick = { viewModel.save(onSaved = onBack) },
                enabled = !viewModel.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag(GameFormTestTags.SAVE),
            ) {
                Text(if (state.isEditing) "Save changes" else "Save game")
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete game?") },
            text = { Text("\"${state.title}\" will be removed from your library. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete(onDeleted = onBack)
                    },
                    modifier = Modifier.testTag(GameFormTestTags.CONFIRM_DELETE),
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }
}
