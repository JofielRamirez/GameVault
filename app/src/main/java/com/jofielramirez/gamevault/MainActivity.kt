package com.jofielramirez.gamevault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jofielramirez.gamevault.ui.navigation.GameVaultApp
import com.jofielramirez.gamevault.ui.theme.GameVaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            GameVaultTheme {
                GameVaultApp()
            }
        }
    }
}
