package com.chinombe.restaurants

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.chinombe.restaurants.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme =
                    lightColorScheme(
                        primary = Color(0xFF176B52),
                        onPrimary = Color.White,
                        background = Color(0xFFF7F7F0),
                        surface = Color.White,
                        onSurface = Color(0xFF202D27),
                        secondaryContainer = Color(0xFFE5EFE5),
                        tertiaryContainer = Color(0xFFFFE9C2),
                    )
            ) {
                AppNavHost()
            }
        }
    }
}
