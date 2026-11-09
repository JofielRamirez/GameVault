package com.jofielramirez.gamevault.ui.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.ui.AppViewModelProvider
import com.jofielramirez.gamevault.ui.components.RatingStars
import com.jofielramirez.gamevault.ui.theme.GameVaultTheme
import com.jofielramirez.gamevault.ui.theme.color
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun GameFormScreen(
    onNavigateUp: () -> Unit,
    viewModel: GameFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scope = rememberCoroutineScope()
    GameFormContent(
        state = viewModel.formState,
        isEditing = viewModel.isEditing,
        onStateChange = viewModel::updateForm,
        onSave = { scope.launch { if (viewModel.save()) onNavigateUp() } },
        onClose = onNavigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GameFormContent(
    state: GameFormState,
    isEditing: Boolean,
    onStateChange: (GameFormState) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit game" else "Add game", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Close without saving") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FormSection("Game") {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { onStateChange(state.copy(title = it)) },
                    label = { Text("Title *") },
                    isError = state.showErrors && state.titleError != null,
                    supportingText = { if (state.showErrors) state.titleError?.let { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("title_field")
                )
                PlatformDropdown(selected = state.platform, onSelect = { onStateChange(state.copy(platform = it)) })
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.genre,
                        onValueChange = { onStateChange(state.copy(genre = it)) },
                        label = { Text("Genre") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.weight(1.4f)
                    )
                    OutlinedTextField(
                        value = state.releaseYear,
                        onValueChange = { new -> onStateChange(state.copy(releaseYear = new.filter { it.isDigit() }.take(4))) },
                        label = { Text("Year") },
                        isError = state.releaseYearError != null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                state.releaseYearError?.let { ErrorText(it) }
            }

            FormSection("Progress") {
                Text("Status", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameStatus.entries.forEach { status ->
                        FilterChip(
                            selected = state.status == status,
                            onClick = { onStateChange(state.copy(status = status)) },
                            label = { Text(status.displayName) },
                            shape = MaterialTheme.shapes.small,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = status.color().copy(alpha = 0.22f),
                                selectedLabelColor = status.color()
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = state.hoursPlayed,
                    onValueChange = { new -> onStateChange(state.copy(hoursPlayed = new.filter { it.isDigit() || it == '.' || it == ',' })) },
                    label = { Text("Hours played") },
                    suffix = { Text("h") },
                    isError = state.hoursError != null,
                    supportingText = { state.hoursError?.let { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Completion", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    Text("${state.completionPercent}%", style = MaterialTheme.typography.titleMedium)
                }
                Slider(
                    value = state.completionPercent.toFloat(),
                    onValueChange = { onStateChange(state.copy(completionPercent = it.roundToInt())) },
                    valueRange = 0f..100f,
                    steps = 19,
                    modifier = Modifier.semantics { stateDescription = "${state.completionPercent} percent" }
                )
            }

            FormSection("Your review") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Rating", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    Text(
                        if (state.rating == 0) "Not rated" else "${state.rating} / 5",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                RatingStars(rating = state.rating, starSize = 30.dp, onRatingChange = { onStateChange(state.copy(rating = it)) })
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { onStateChange(state.copy(notes = it)) },
                    label = { Text("Notes") },
                    placeholder = { Text("Favorite moments, what you enjoyed, why you stopped…") },
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_button")
            ) {
                Text(if (isEditing) "Save changes" else "Add to library", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
}

@Composable
private fun PlatformDropdown(selected: Platform, onSelect: (Platform) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Platform") },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        // Transparent layer so a tap anywhere on the field opens the menu
        Box(
            Modifier
                .matchParentSize()
                .clickable(role = Role.DropdownList, onClickLabel = "Choose platform") { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Platform.entries.forEach { platform ->
                DropdownMenuItem(
                    text = { Text(platform.displayName) },
                    onClick = {
                        onSelect(platform)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0E17, widthDp = 360, heightDp = 1100)
@Composable
private fun GameFormPreview() {
    GameVaultTheme {
        GameFormContent(
            state = GameFormState(
                title = "The Legend of Zelda: Breath of the Wild", platform = Platform.NINTENDO_SWITCH,
                genre = "Action-adventure", releaseYear = "2017", status = GameStatus.PLAYING,
                hoursPlayed = "35", completionPercent = 65, rating = 5
            ),
            isEditing = false, onStateChange = {}, onSave = {}, onClose = {}
        )
    }
}
