package com.jofielramirez.gamevault.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightOnAccent,
    primaryContainer = LightAccentContainer,
    onPrimaryContainer = LightOnAccentContainer,
    secondary = LightSecondary,
    onSecondary = LightOnAccent,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = StarGold,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightBackground,
    onSurface = LightOnBackground,
    surfaceVariant = LightSurfaceContainerHigh,
    onSurfaceVariant = LightOnSurfaceMuted,
    surfaceContainerLowest = LightSurfaceContainer,
    surfaceContainerLow = LightSurfaceContainer,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError
)

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

/** Lets semantic colors (status colors) pick the right shade for the current theme. */
internal val LocalIsDarkTheme = staticCompositionLocalOf { false }

/** Rounded corners used everywhere: chips 12 dp, cards 16 dp, covers 12 dp. */
val VaultShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * GameVault uses a clean light theme by default.
 * The dark scheme is kept for the theme switch planned in Settings.
 */
@Composable
fun GameVaultTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = VaultShapes,
            content = content
        )
    }
}
