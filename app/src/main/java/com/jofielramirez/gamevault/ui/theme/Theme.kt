package com.jofielramirez.gamevault.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = VaultAccent,
    onPrimary = VaultOnAccent,
    primaryContainer = VaultAccentContainer,
    onPrimaryContainer = VaultOnAccentContainer,
    secondary = VaultSecondary,
    onSecondary = VaultOnAccent,
    secondaryContainer = VaultSecondaryContainer,
    onSecondaryContainer = VaultOnSecondaryContainer,
    tertiary = StarGold,
    background = VaultBackground,
    onBackground = VaultOnBackground,
    surface = VaultSurface,
    onSurface = VaultOnBackground,
    surfaceVariant = VaultSurfaceContainerHigh,
    onSurfaceVariant = VaultOnSurfaceMuted,
    surfaceContainerLowest = VaultBackground,
    surfaceContainerLow = VaultSurfaceContainerLow,
    surfaceContainer = VaultSurfaceContainer,
    surfaceContainerHigh = VaultSurfaceContainerHigh,
    surfaceContainerHighest = VaultSurfaceContainerHighest,
    outline = VaultOutline,
    outlineVariant = VaultOutlineVariant,
    error = VaultError
)

private val LightColorScheme = lightColorScheme(
    primary = VaultLightAccent,
    onPrimary = Color.White,
    secondary = VaultSecondary,
    tertiary = StarGold,
    background = VaultLightBackground,
    surface = VaultLightSurface,
    onBackground = VaultLightOnBackground,
    onSurface = VaultLightOnBackground
)

/** Rounded corners used everywhere: chips 12 dp, cards 16 dp, covers 12 dp. */
val VaultShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * GameVault uses a dark theme by default.
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
        shapes = VaultShapes,
        content = content
    )
}
