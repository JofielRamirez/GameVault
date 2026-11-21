package com.jofielramirez.gamevault

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jofielramirez.gamevault.data.preferences.ThemeMode
import com.jofielramirez.gamevault.ui.navigation.GameVaultApp
import com.jofielramirez.gamevault.ui.theme.GameVaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val preferences = (application as GameVaultApplication).container.userPreferencesRepository.preferences
        setContent {
            val prefs by preferences.collectAsStateWithLifecycle(initialValue = null)
            val darkTheme = when (prefs?.themeMode ?: ThemeMode.SYSTEM) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            // Status and navigation bar icons follow the app theme, not the phone's
            DisposableEffect(darkTheme) {
                val style = if (darkTheme) SystemBarStyle.dark(Color.TRANSPARENT)
                else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }
            GameVaultTheme(darkTheme = darkTheme) {
                GameVaultApp()
            }
        }
    }
}
