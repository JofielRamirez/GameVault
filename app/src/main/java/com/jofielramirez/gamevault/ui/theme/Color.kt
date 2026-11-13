package com.jofielramirez.gamevault.ui.theme

import androidx.compose.ui.graphics.Color
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform

// GameVault palette: near-black with a cool tint and a teal-gray accent.
// (Purple was replaced as the accent: black + purple is very common in apps, and teal-gray
// gives GameVault its own identity. Purple remains only as the "Currently Playing" status.)
val VaultAccent = Color(0xFF7FB2AE)              // teal-gray: primary actions, progress, selection
val VaultOnAccent = Color(0xFF0C1E1D)
val VaultAccentContainer = Color(0xFF1F3B3A)
val VaultOnAccentContainer = Color(0xFFCFE6E3)
val VaultSecondary = Color(0xFF8FB3CC)            // muted steel blue: secondary chips
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

val VaultLightAccent = Color(0xFF2F6B66)
val VaultLightBackground = Color(0xFFF4F7F7)
val VaultLightSurface = Color(0xFFFFFFFF)
val VaultLightOnBackground = Color(0xFF161C1E)

// Semantic colors: every status and platform always uses the same color across screens
val StarGold = Color(0xFFFBBF24)

fun GameStatus.color(): Color = when (this) {
    GameStatus.WANT_TO_PLAY -> Color(0xFF60A5FA)
    GameStatus.PLAYING -> Color(0xFFB794F6)
    GameStatus.COMPLETED -> Color(0xFF34D399)
    GameStatus.ON_HOLD -> Color(0xFFFBBF24)
    GameStatus.DROPPED -> Color(0xFFF87171)
}

fun Platform.color(): Color = when (this) {
    Platform.PC -> Color(0xFF475569)
    Platform.PLAYSTATION -> Color(0xFF2563EB)
    Platform.XBOX -> Color(0xFF15803D)
    Platform.NINTENDO_SWITCH -> Color(0xFFDC2626)
    Platform.MOBILE -> Color(0xFFD97706)
    Platform.OTHER -> Color(0xFF3F7F7A)
}
