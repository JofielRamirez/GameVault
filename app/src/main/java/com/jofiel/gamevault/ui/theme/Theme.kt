package com.jofiel.gamevault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = VaultPurpleLight,
    onPrimary = Color(0xFF1E0F5C),
    primaryContainer = VaultPurple,
    onPrimaryContainer = Color.White,
    secondary = VaultBlue,
    background = VaultBackgroundDark,
    surface = VaultSurfaceDark,
    surfaceVariant = VaultSurfaceVariantDark,
    surfaceContainer = VaultSurfaceDark,
)

private val LightColors = lightColorScheme(
    primary = VaultPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E2FB),
    onPrimaryContainer = Color(0xFF22125F),
    secondary = VaultBlue,
    background = VaultBackgroundLight,
    surfaceVariant = VaultSurfaceVariantLight,
)

@Composable
fun GameVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = GameVaultTypography,
        content = content,
    )
}
