package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.rulytopia.ui.GameScreen
import com.example.rulytopia.ui.GameViewModel
import com.example.rulytopia.ui.dialogs.SettingsDialog
import com.example.rulytopia.ui.screens.GamePlayScreen
import com.example.rulytopia.ui.screens.LevelSelectScreen
import com.example.rulytopia.ui.screens.MainMenuScreen
import com.example.ui.theme.RulytopiaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        enableEdgeToEdge()

        // Immersive sticky fullscreen mode for AAA game feel
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            RulytopiaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RulytopiaApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun RulytopiaApp(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle Android system back button
    BackHandler(enabled = uiState.currentScreen != GameScreen.MAIN_MENU) {
        when (uiState.currentScreen) {
            GameScreen.PLAYING -> viewModel.navigateTo(GameScreen.LEVEL_SELECT)
            GameScreen.LEVEL_SELECT -> viewModel.navigateTo(GameScreen.MAIN_MENU)
            GameScreen.MAIN_MENU -> {}
        }
    }

    when (uiState.currentScreen) {
        GameScreen.MAIN_MENU -> {
            MainMenuScreen(
                totalStars = uiState.totalStars,
                onPlay = {
                    val highest = viewModel.preferences.highestUnlockedLevel
                    viewModel.startLevel(highest)
                },
                onLevelSelect = {
                    viewModel.navigateTo(GameScreen.LEVEL_SELECT)
                },
                onSettings = {
                    viewModel.toggleSettingsDialog(true)
                }
            )
        }
        GameScreen.LEVEL_SELECT -> {
            LevelSelectScreen(
                preferences = viewModel.preferences,
                onLevelSelected = { levelId ->
                    viewModel.startLevel(levelId)
                },
                onBack = {
                    viewModel.navigateTo(GameScreen.MAIN_MENU)
                }
            )
        }
        GameScreen.PLAYING -> {
            GamePlayScreen(viewModel = viewModel)
        }
    }

    // Global Settings Dialog
    if (uiState.showSettingsDialog) {
        SettingsDialog(
            isSoundEnabled = uiState.isSoundEnabled,
            onToggleSound = { viewModel.toggleSound(it) },
            isMusicEnabled = uiState.isMusicEnabled,
            onToggleMusic = { viewModel.toggleMusic(it) },
            isVibrationEnabled = uiState.isVibrationEnabled,
            onToggleVibration = { viewModel.toggleVibration(it) },
            onDismiss = { viewModel.toggleSettingsDialog(false) }
        )
    }
}
