package com.maaz.xlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.maaz.xlauncher.data.GameAccount
import com.maaz.xlauncher.data.GameVersion
import com.maaz.xlauncher.ui.screens.GameActivityScreen
import com.maaz.xlauncher.ui.theme.MaazXLauncherTheme

/**
 * GameActivity.kt - Fullscreen Minecraft Java game launcher
 * Handles process management and touch control overlay
 */
class GameActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val selectedAccount = intent.getParcelableExtra<GameAccount>("account")
        val selectedVersion = intent.getStringExtra("version") ?: "1.21.1"
        
        setContent {
            MaazXLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    GameActivityScreen(
                        account = selectedAccount,
                        gameVersion = GameVersion(
                            versionId = selectedVersion,
                            releaseType = "release",
                            installed = true,
                            ramMB = 2048
                        ),
                        onExit = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}
