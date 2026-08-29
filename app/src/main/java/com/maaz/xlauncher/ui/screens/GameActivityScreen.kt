package com.maaz.xlauncher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maaz.xlauncher.data.GameAccount
import com.maaz.xlauncher.data.GameVersion
import com.maaz.xlauncher.engine.LaunchHelper
import com.maaz.xlauncher.ui.components.TouchControlsOverlay
import kotlinx.coroutines.launch

/**
 * GameActivityScreen.kt - Fullscreen game launcher with Minecraft display
 * Shows black screen during startup, then overlays touch controls
 */
@Composable
fun GameActivityScreen(
    account: GameAccount?,
    gameVersion: GameVersion,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isGameRunning by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Ready to launch") }
    var minecraftProcess by remember { mutableStateOf<Process?>(null) }
    val scope = rememberCoroutineScope()
    
    // Fullscreen black background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (!isGameRunning) {
            // Pre-launch screen: Button to start Minecraft
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Maaz X Launcher V4.0",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9C27B0) // Purple
                    ),
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                Text(
                    text = statusMessage,
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                if (account != null) {
                    Text(
                        text = "Account: ${account.username}",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.Gray
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "Version: ${gameVersion.versionId}",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.Gray
                        ),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
                
                // PLAY button with purple glow
                Button(
                    onClick = {
                        scope.launch {
                            launchMinecraft(
                                account,
                                gameVersion,
                                onGameLaunched = {
                                    isGameRunning = true
                                    statusMessage = "Minecraft is running"
                                },
                                onStatusChanged = { newStatus ->
                                    statusMessage = newStatus
                                },
                                onProcessStarted = { process ->
                                    minecraftProcess = process
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .size(width = 300.dp, height = 80.dp)
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0), // Purple
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "PLAY MINECRAFT JAVA",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onExit,
                    modifier = Modifier
                        .size(width = 200.dp, height = 50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Back to Launcher")
                }
            }
        } else {
            // Game is running: Show black screen with touch controls overlay
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Main Minecraft window would be rendered here by the process
                // For now, show black screen as placeholder
                Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                
                // Overlay touch controls on top
                TouchControlsOverlay(
                    onJump = { /* Jump input */ },
                    onSneak = { /* Sneak input */ },
                    onSprint = { /* Sprint input */ },
                    onAttack = { /* Attack input */ },
                    onUse = { /* Use/Place block input */ },
                    onInventory = { /* Open inventory */ },
                    onChat = { /* Open chat */ },
                    onEscape = {
                        // Exit game
                        LaunchHelper.stopMinecraft(minecraftProcess)
                        isGameRunning = false
                        statusMessage = "Game stopped"
                        minecraftProcess = null
                        onExit()
                    },
                    onWASD = { w, a, s, d ->
                        // Movement input to Minecraft process
                    },
                    onMouseLook = { deltaX, deltaY ->
                        // Mouse look input to Minecraft process
                    }
                )
            }
        }
    }
}

/**
 * Launches Minecraft using LaunchHelper
 */
private suspend fun launchMinecraft(
    account: GameAccount?,
    gameVersion: GameVersion,
    onGameLaunched: () -> Unit,
    onStatusChanged: (String) -> Unit,
    onProcessStarted: (Process?) -> Unit
) {
    if (account == null) {
        onStatusChanged("No account selected")
        return
    }
    
    onStatusChanged("Starting Minecraft...")
    
    val process = LaunchHelper.launchMinecraft(
        context = android.content.ContextCompat.getApplicationContext() as android.content.Context,
        gameVersion = gameVersion,
        username = account.username,
        uuid = account.uuid
    )
    
    if (process != null) {
        onProcessStarted(process)
        onStatusChanged("Minecraft is starting...")
        
        // Monitor process
        kotlinx.coroutines.Dispatchers.IO.let { dispatcher ->
            kotlinx.coroutines.withContext(dispatcher) {
                try {
                    process.waitFor()
                    onStatusChanged("Game stopped")
                } catch (e: Exception) {
                    onStatusChanged("Error: ${e.message}")
                }
            }
        }
        onGameLaunched()
    } else {
        onStatusChanged("Failed to launch Minecraft")
    }
}
