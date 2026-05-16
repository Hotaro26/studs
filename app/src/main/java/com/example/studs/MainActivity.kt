package com.example.studs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.studs.ui.navigation.AppNavigation
import com.example.studs.theme.StudsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as StudsApplication).container
        enableEdgeToEdge()
        setContent {
            val themeMode by appContainer.settingsRepository.themeMode.collectAsState()
            val colorScheme by appContainer.settingsRepository.colorScheme.collectAsState()
            
            StudsTheme(
                themeMode = themeMode,
                colorSchemeType = colorScheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(appContainer)
                }
            }
        }
    }
}
