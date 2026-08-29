package com.maaz.xlauncher.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maaz.xlauncher.data.DeviceAnalyzer
import com.maaz.xlauncher.data.GameAccount
import com.maaz.xlauncher.data.GameVersion

/**
 * MainLandscapeScreen.kt - 3-Column Landscape Dashboard for Maaz X Launcher V4.0
 * Left Panel [20%]: Glass Sidebar with navigation
 * Center Panel [55%]: Dashboard with version selector and PLAY button
 * Right Panel [25%]: Device info, account, performance stats
 */
@Composable
fun MainLandscapeScreen(
    selectedAccount: GameAccount?,
    selectedVersion: String,
    onVersionSelected: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onPlayClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val versions = listOf("1.21.1", "1.20.1", "1.16.5", "1.19.2")
    var currentNavItem by remember { mutableStateOf("play") }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Animated blur background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A0033), // Dark purple
                            Color(0xFF0D001A)  // Darker purple
                        )
                    )
                )
                .blur(radiusX = 10.dp, radiusY = 10.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ============ LEFT PANEL [20%] - GLASS SIDEBAR ============
            LeftSidebar(
                currentNavItem = currentNavItem,
                onNavItemClicked = { item ->
                    currentNavItem = item
                    onNavigate(item)
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.2f)
            )
            
            // ============ CENTER PANEL [55%] - DASHBOARD ============
            CenterDashboard(
                selectedAccount = selectedAccount,
                selectedVersion = selectedVersion,
                availableVersions = versions,
                onVersionSelected = onVersionSelected,
                onPlayClicked = onPlayClicked,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.55f / 0.8f) // Adjust for remaining width
            )
            
            // ============ RIGHT PANEL [25%] - INFO CARDS ============
            RightInfoPanel(
                selectedAccount = selectedAccount,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * Left Sidebar - Navigation menu with glass morphism effect
 */
@Composable
fun LeftSidebar(
    currentNavItem: String,
    onNavItemClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        "play" to "Play",
        "versions" to "Versions",
        "modpacks" to "Modpacks",
        "accounts" to "Accounts",
        "skins" to "Skins",
        "settings" to "Settings",
        "crashlogs" to "Crash Logs",
        "github" to "GitHub"
    )
    
    Column(
        modifier = modifier
            .background(
                color = Color(0x80FFFFFF).copy(alpha = 0.1f), // Glass effect
                shape = RoundedCornerShape(0.dp, 16.dp, 16.dp, 0.dp)
            )
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Maaz X Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MX",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
        
        Divider(color = Color.White.copy(alpha = 0.2f))
        
        // Navigation Items
        navItems.forEach { (id, label) ->
            NavigationItem(
                label = label,
                isSelected = currentNavItem == id,
                onClick = { onNavItemClicked(id) }
            )
        }
    }
}

/**
 * Navigation Item Button
 */
@Composable
fun NavigationItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF9C27B0) else Color.Transparent,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
        )
    }
}

/**
 * Center Dashboard - Main content area with version selector and PLAY button
 */
@Composable
fun CenterDashboard(
    selectedAccount: GameAccount?,
    selectedVersion: String,
    availableVersions: List<String>,
    onVersionSelected: (String) -> Unit,
    onPlayClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedVersions by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Welcome Text
        Text(
            text = "Welcome back, ${selectedAccount?.username ?: "Gamer"}",
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        // Version Selector Dropdown
        Box(modifier = Modifier.padding(bottom = 32.dp)) {
            Button(
                onClick = { expandedVersions = !expandedVersions },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2A2A2A),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Select Version: $selectedVersion",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                )
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Dropdown",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp)
                )
            }
            
            if (expandedVersions) {
                DropdownMenu(
                    expanded = expandedVersions,
                    onDismissRequest = { expandedVersions = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    availableVersions.forEach { version ->
                        DropdownMenuItem(
                            text = { Text(version) },
                            onClick = {
                                onVersionSelected(version)
                                expandedVersions = false
                            }
                        )
                    }
                }
            }
        }
        
        // BIG PLAY BUTTON with purple glow animation
        Button(
            onClick = onPlayClicked,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(100.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color(0xFF9C27B0).copy(alpha = 0.8f),
                    spotColor = Color(0xFFE91E63)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = "PLAY MINECRAFT JAVA",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }
        
        // Latest News Cards
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Latest News",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 16.dp)
        )
        
        repeat(3) { index ->
            NewsCard(
                title = "Minecraft ${1.21 + index * 0.1} Released",
                description = "New features and improvements available",
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

/**
 * News Card Component
 */
@Composable
fun NewsCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        border = BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = description,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            )
        }
    }
}

/**
 * Right Info Panel - Device stats, account, performance
 */
@Composable
fun RightInfoPanel(
    selectedAccount: GameAccount?,
    modifier: Modifier = Modifier
) {
    // Note: In real app, would use DeviceAnalyzer context here
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 1: Device Info
        InfoCard(
            title = "Device",
            items = listOf(
                "CPU: MediaTek Helio G" to "Unknown",
                "GPU: Mali-G72" to "Unknown",
                "RAM: 2048 MB" to "Auto Detect"
            )
        )
        
        // Card 2: Active Account + Skin
        InfoCard(
            title = "Account",
            items = listOf(
                "Username:" to (selectedAccount?.username ?: "Offline"),
                "Mode:" to if (selectedAccount?.isMicrosoft == true) "Microsoft" else "Offline"
            )
        )
        
        // Card 3: Performance
        InfoCard(
            title = "Performance",
            items = listOf(
                "FPS:" to "--",
                "RAM Usage:" to "--/2048 MB"
            )
        )
        
        // Card 4: Last Crash
        InfoCard(
            title = "Last Crash",
            items = listOf(
                "Status:" to "None"
            ),
            hasButton = true,
            buttonText = "Auto Fix"
        )
    }
}

/**
 * Info Card Component
 */
@Composable
fun InfoCard(
    title: String,
    items: List<Pair<String, String>>,
    hasButton: Boolean = false,
    buttonText: String = "",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        border = BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    )
                    Text(
                        text = value,
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    )
                }
            }
            
            if (hasButton) {
                Button(
                    onClick = { /* Auto fix action */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = buttonText,
                        style = TextStyle(fontSize = 9.sp)
                    )
                }
            }
        }
    }
}
