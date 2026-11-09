package com.jofielramirez.gamevault.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VaultPurple,
    secondary = VaultBlue,
    tertiary = VaultPurpleDark,
    background = VaultBackground,
    surface = VaultSurface,
    surfaceVariant = VaultSurfaceVariant,
    onBackground = VaultOnBackground,
    onSurface = VaultOnBackground,
    onSurfaceVariant = VaultOnSurfaceMuted
)

private val LightColorScheme = lightColorScheme(
    primary = VaultPurpleDark,
    secondary = VaultBlue,
    tertiary = VaultPurple,
    background = VaultLightBackground,
    surface = VaultLightSurface,
    onBackground = VaultLightOnBackground,
    onSurface = VaultLightOnBackground
)

/**
 * GameVault uses a gaming-oriented dark theme by default.
 * The light scheme is kept so a theme switch can be added later.
 */
@Composable
fun GameVaultTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
