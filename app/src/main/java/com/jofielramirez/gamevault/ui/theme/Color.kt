package com.jofielramirez.gamevault.ui.theme

import androidx.compose.ui.graphics.Color
import com.jofielramirez.gamevault.data.model.GameStatus
import com.jofielramirez.gamevault.data.model.Platform

// GameVault palette: dark background with purple and blue accents
val VaultPurple = Color(0xFF9F7AEA)
val VaultPurpleDeep = Color(0xFF4C1D95)
val VaultPurpleLight = Color(0xFFEDE9FE)
val VaultBlue = Color(0xFF60A5FA)
val VaultBlueDeep = Color(0xFF1E3A8A)
val VaultBlueLight = Color(0xFFDBEAFE)

val VaultBackground = Color(0xFF0F0E17)
val VaultSurface = Color(0xFF16141F)
val VaultSurfaceContainerLow = Color(0xFF1A1825)
val VaultSurfaceContainer = Color(0xFF1F1C2B)
val VaultSurfaceContainerHigh = Color(0xFF262335)
val VaultSurfaceContainerHighest = Color(0xFF2E2A40)
val VaultOutline = Color(0xFF5B5672)
val VaultOutlineVariant = Color(0xFF34304A)
val VaultOnBackground = Color(0xFFEDEBF5)
val VaultOnSurfaceMuted = Color(0xFFA9A5BD)
val VaultError = Color(0xFFF87171)

val VaultLightBackground = Color(0xFFF7F5FC)
val VaultLightSurface = Color(0xFFFFFFFF)
val VaultLightOnBackground = Color(0xFF1A1825)
val VaultPurpleDark = Color(0xFF6D28D9)

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
    Platform.OTHER -> Color(0xFF7C3AED)
}
