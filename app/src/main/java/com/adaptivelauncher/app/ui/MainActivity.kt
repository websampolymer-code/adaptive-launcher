package com.adaptivelauncher.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.adaptivelauncher.app.LauncherApp
import com.adaptivelauncher.app.ui.home.LauncherHomeScreen
import com.adaptivelauncher.app.ui.onboarding.OnboardingScreen
import com.adaptivelauncher.app.ui.theme.AdaptiveLauncherTheme

class MainActivity : ComponentActivity() {

    private val launcherApp by lazy { application as LauncherApp }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val onboardingCompleted by launcherApp.preferencesManager.onboardingCompleted.collectAsState(initial = false)

            AdaptiveLauncherTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (onboardingCompleted) {
                        LauncherHomeScreen(
                            onNavigateToSettings = {
                                startActivity(Intent(this, com.adaptivelauncher.app.ui.settings.SettingsActivity::class.java))
                            }
                        )
                    } else {
                        OnboardingScreen(
                            onCompleted = {
                                launcherApp.preferencesManager.setOnboardingCompleted(true)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
