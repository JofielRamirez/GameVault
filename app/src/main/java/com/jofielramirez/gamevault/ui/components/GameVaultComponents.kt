package com.jofielramirez.gamevault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jofielramirez.gamevault.data.model.Game
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform
import com.jofielramirez.gamevault.ui.theme.StarGold
import com.jofielramirez.gamevault.ui.theme.color
import java.util.Locale

/** "35 h", "37.5 h" */
fun formatHours(hours: Double): String =
    if (hours % 1.0 == 0.0) "${hours.toInt()} h" else String.format(Locale.US, "%.1f h", hours)

/** Subtitle line under a game title: "Nintendo Switch · Action-adventure · 2017". */
fun Game.metadataLine(includeYear: Boolean = false): String =
    listOfNotNull(
        platform.displayName,
        genre.takeIf { it.isNotBlank() },
        releaseYear?.toString()?.takeIf { includeYear }
    ).joinToString(" · ")

/**
 * Cover placeholder until real cover art is added: the platform color as a gradient
 * with the title's initials, so every game is recognizable at a glance.
 */
@Composable
fun GameCover(
    title: String,
    platform: Platform,
    modifier: Modifier = Modifier,
    width: Dp = 56.dp,
    height: Dp = 76.dp,
    fontSize: TextUnit = 20.sp
) {
    val base = platform.color()
    val initials = title.split(" ", ":", "-")
        .filter { it.isNotBlank() && it.first().isLetterOrDigit() }
        .filterNot { it.lowercase() in setOf("the", "of", "a") }
        .take(2)
        .joinToString("") { it.first().uppercase() }
    Box(
        modifier = modifier
            .size(width, height)
            .clip(MaterialTheme.shapes.small)
            .background(Brush.linearGradient(listOf(base, lerp(base, Color.Black, 0.35f))))
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Pill with a colored dot: the same color is used for a status everywhere in the app. */
@Composable
fun StatusChip(status: GameStatus, modifier: Modifier = Modifier) {
    val color = status.color()
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = color.copy(alpha = 0.16f),
        contentColor = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(6.dp))
            Text(status.displayName, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun GameProgressBar(percent: Int, modifier: Modifier = Modifier, height: Dp = 6.dp) {
    val color = if (percent >= 100) GameStatus.COMPLETED.color() else MaterialTheme.colorScheme.primary
    LinearProgressIndicator(
        progress = { percent / 100f },
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics { contentDescription = "$percent percent complete" },
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {}
    )
}

/**
 * Five stars. Read-only when [onRatingChange] is null; otherwise each star is a
 * 48 dp touch target with its own description for TalkBack.
 */
@Composable
fun RatingStars(
    rating: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
    onRatingChange: ((Int) -> Unit)? = null
) {
    val off = MaterialTheme.colorScheme.outlineVariant
    if (onRatingChange == null) {
        Row(
            modifier = modifier.semantics {
                contentDescription = if (rating == 0) "Not rated" else "Rated $rating out of 5"
            },
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(5) { index ->
                Icon(
                    Icons.Filled.Star, contentDescription = null,
                    tint = if (index < rating) StarGold else off,
                    modifier = Modifier.size(starSize)
                )
            }
        }
    } else {
        Row(modifier = modifier) {
            repeat(5) { index ->
                val stars = index + 1
                IconButton(onClick = { onRatingChange(if (rating == stars) 0 else stars) }) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Rate $stars out of 5",
                        tint = if (index < rating) StarGold else off,
                        modifier = Modifier.size(starSize)
                    )
                }
            }
        }
    }
}

/** Small dashboard card: big number on top, label below. */
@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier, accent: Color? = null) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                color = accent ?: MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { heading() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (trailing != null) {
            Text(
                trailing,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyMessage(title: String, body: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
