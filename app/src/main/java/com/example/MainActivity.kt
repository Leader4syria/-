package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.navigation.MasarNavigation
import com.example.ui.theme.MasarTheme
import com.example.ui.viewmodels.MasarViewModel
import com.example.ui.viewmodels.OnboardingViewModel

class MainActivity : ComponentActivity() {
    private val mainViewModel: MasarViewModel by viewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by mainViewModel.settingsFlow.collectAsState()
            val isDark = settings?.isDarkMode ?: false

            // Enforce Arabic RTL Layout Direction for full app
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MasarTheme(darkTheme = isDark) {
                    MasarNavigation(
                        mainViewModel = mainViewModel,
                        onboardingViewModel = onboardingViewModel
                    )
                }
            }
        }
    }
}

