package com.jofielramirez.gamevault.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform

// ---------------------------------------------------------------- Light theme (default)
// Clean, minimal look: very light cool-gray background, white cards, dark text and a
// teal-gray accent (darker than in the dark theme so it reads well on white).
val LightAccent = Color(0xFF3E7C77)
val LightOnAccent = Color(0xFFFFFFFF)
val LightAccentContainer = Color(0xFFD7EBE8)
val LightOnAccentContainer = Color(0xFF103B37)
val LightSecondary = Color(0xFF4A7290)
val LightSecondaryContainer = Color(0xFFDDE9F2)
val LightOnSecondaryContainer = Color(0xFF15334A)

val LightBackground = Color(0xFFF4F6F6)
val LightSurfaceContainer = Color(0xFFFFFFFF)       // cards, bars and search field are white
val LightSurfaceContainerHigh = Color(0xFFEDF1F1)
val LightSurfaceContainerHighest = Color(0xFFE2E8E8) // progress and chart tracks
val LightOnBackground = Color(0xFF1A2224)
val LightOnSurfaceMuted = Color(0xFF5B6A6D)
val LightOutline = Color(0xFF8F9EA0)
val LightOutlineVariant = Color(0xFFDCE3E3)
val LightError = Color(0xFFC93838)

// ---------------------------------------------------------------- Dark theme (kept for a future theme switch)
val VaultAccent = Color(0xFF7FB2AE)
val VaultOnAccent = Color(0xFF0C1E1D)
val VaultAccentContainer = Color(0xFF1F3B3A)
val VaultOnAccentContainer = Color(0xFFCFE6E3)
val VaultSecondary = Color(0xFF8FB3CC)
val VaultSecondaryContainer = Color(0xFF233645)
val VaultOnSecondaryContainer = Color(0xFFD6E6F2)

val VaultBackground = Color(0xFF0E1214)
val VaultSurface = Color(0xFF13181A)
val VaultSurfaceContainerLow = Color(0xFF171D1F)
val VaultSurfaceContainer = Color(0xFF1C2326)
val VaultSurfaceContainerHigh = Color(0xFF232B2E)
val VaultSurfaceContainerHighest = Color(0xFF2B3437)
val VaultOutline = Color(0xFF5A6A6E)
val VaultOutlineVariant = Color(0xFF313C3F)
val VaultOnBackground = Color(0xFFE6ECEC)
val VaultOnSurfaceMuted = Color(0xFF9DAEB0)
val VaultError = Color(0xFFF87171)

// ---------------------------------------------------------------- Semantic colors
val StarGold = Color(0xFFF2A91D)

/**
 * Every status keeps the same hue everywhere in the app. The light theme uses deeper
 * shades so colored text (e.g. "On Hold" in amber) stays readable on white.
 */
@Composable
@ReadOnlyComposable
fun GameStatus.color(): Color = if (LocalIsDarkTheme.current) {
    when (this) {
        GameStatus.WANT_TO_PLAY -> Color(0xFF60A5FA)
        GameStatus.PLAYING -> Color(0xFFB794F6)
        GameStatus.COMPLETED -> Color(0xFF34D399)
        GameStatus.ON_HOLD -> Color(0xFFFBBF24)
        GameStatus.DROPPED -> Color(0xFFF87171)
    }
} else {
    when (this) {
        GameStatus.WANT_TO_PLAY -> Color(0xFF2F6FD1)
        GameStatus.PLAYING -> Color(0xFF7653C9)
        GameStatus.COMPLETED -> Color(0xFF14895F)
        GameStatus.ON_HOLD -> Color(0xFFB0700A)
        GameStatus.DROPPED -> Color(0xFFC93838)
    }
}

fun Platform.color(): Color = when (this) {
    Platform.PC -> Color(0xFF475569)
    Platform.PLAYSTATION -> Color(0xFF2563EB)
    Platform.XBOX -> Color(0xFF15803D)
    Platform.NINTENDO_SWITCH -> Color(0xFFDC2626)
    Platform.MOBILE -> Color(0xFFD97706)
    Platform.OTHER -> Color(0xFF3F7F7A)
}
