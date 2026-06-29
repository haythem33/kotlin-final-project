package com.example.finalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.finalproject.navigation.AppNavHost
import com.example.finalproject.ui.theme.FinalProjectTheme

/**
 * The single Activity that hosts the whole Compose UI.
 *
 * It does almost nothing on purpose: it sets the theme and hands control to
 * AppNavHost, which decides what screen to show. No UI state lives here.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalProjectTheme {
                AppNavHost()
            }
        }
    }
}
