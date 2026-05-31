package com.mm.audiotool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mm.audiotool.navigation.AppNavGraph
import com.mm.audiotool.ui.theme.MM_Audio_ToolTheme
import com.mm.audiotool.viewmodel.SettingsViewModel
import com.mm.audiotool.viewmodel.SettingsViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(application)
            )
            val themeMode by settingsViewModel.themeMode.collectAsState()

            val darkTheme = when (themeMode) {
                "Dark"  -> true
                "White" -> false
                else    -> isSystemInDarkTheme() // "Auto"
            }

            MM_Audio_ToolTheme(darkTheme = darkTheme) {
                AppNavGraph(settingsViewModel = settingsViewModel)
            }
        }
    }
}
