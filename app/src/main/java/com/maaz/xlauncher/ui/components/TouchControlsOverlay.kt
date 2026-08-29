package com.maaz.xlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

/**
 * TouchControlsOverlay.kt - Virtual joystick + game buttons overlay
 * Left: WASD joystick, Right: Mouse look + game buttons (Jump, Sneak, Sprint, Attack, Use, E, T, Esc)
 * Bottom: 9-slot hotbar. Top-Right: Control editor button
 */
@Composable
fun TouchControlsOverlay(
    onJump: () -> Unit,
    onSneak: () -> Unit,
    onSprint: () -> Unit,
    onAttack: () -> Unit,
    onUse: () -> Unit,
    onInventory: () -> Unit,
    onChat: () -> Unit,
    onEscape: () -> Unit,
    onWASD: (w: Boolean, a: Boolean, s: Boolean, d: Boolean) -> Unit,
    onMouseLook: (deltaX: Float, deltaY: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditMode by remember { mutableStateOf(false) }
    var leftJoystickPos by remember { mutableStateOf(Offset.Zero) }
    var rightLookPos by remember { mutableStateOf(Offset.Zero) }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // LEFT JOYSTICK (WASD Movement)
        LeftJoystick(
            position = leftJoystickPos,
            onMove = { offset ->
                leftJoystickPos = offset
                // Calculate WASD input from joystick
                val distance = sqrt(offset.x * offset.x + offset.y * offset.y)
                if (distance > 30f) {
                    val w = offset.y < -20
                    val s = offset.y > 20
                    val a = offset.x < -20
                    val d = offset.x > 20
                    onWASD(w, a, s, d)
                }
            },
            modifier = Modifier.align(Alignment.BottomStart)
        )
        
        // RIGHT AREA - Mouse Look
        MouseLookArea(
            position = rightLookPos,
            onMove = { offset ->
                rightLookPos = offset
                onMouseLook(offset.x, offset.y)
            },
            modifier = Modifier.align(Alignment.BottomEnd)
        )
        
        // RIGHT PANEL - Game Buttons (arranged vertically)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .width(100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Jump Button
            GameButton(
                text = "JUMP",
                onClick = onJump,
                bgColor = Color(0xFF4CAF50), // Green
                modifier = Modifier.size(80.dp, 40.dp)
            )
            
            // Sneak Button
            GameButton(
                text = "SNEAK",
                onClick = onSneak,
                bgColor = Color(0xFF2196F3), // Blue
                modifier = Modifier.size(80.dp, 40.dp)
            )
            
            // Sprint Button
            GameButton(
                text = "SPRINT",
                onClick = onSprint,
                bgColor = Color(0xFFFF9800), // Orange
                modifier = Modifier.size(80.dp, 40.dp)
            )
        }
        
        // CENTER-RIGHT PANEL - Attack, Use, Inventory, Chat
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(16.dp)
                .width(100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Attack Button
            GameButton(
                text = "ATTACK",
                onClick = onAttack,
                bgColor = Color(0xFFF44336), // Red
                modifier = Modifier.size(80.dp, 40.dp)
            )
            
            // Use/Place Button
            GameButton(
                text = "USE",
                onClick = onUse,
                bgColor = Color(0xFF9C27B0), // Purple
                modifier = Modifier.size(80.dp, 40.dp)
            )
            
            // Inventory (E) Button
            GameButton(
                text = "E",
                onClick = onInventory,
                bgColor = Color(0xFFFFEB3B), // Yellow
                modifier = Modifier.size(80.dp, 40.dp)
            )
            
            // Chat (T) Button
            GameButton(
                text = "T",
                onClick = onChat,
                bgColor = Color(0xFF00BCD4), // Cyan
                modifier = Modifier.size(80.dp, 40.dp)
            )
        }
        
        // TOP-RIGHT - Control Editor Button
        Button(
            onClick = { isEditMode = !isEditMode },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Controls",
                modifier = Modifier.size(24.dp)
            )
        }
        
        // BOTTOM - 9-SLOT HOTBAR
        Hotbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
        
        // ESC Button (Top-Right corner, small)
        GameButton(
            text = "ESC",
            onClick = onEscape,
            bgColor = Color(0xFF424242), // Dark Gray
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp + 60.dp)
                .size(50.dp)
        )
    }
}

/**
 * Left Joystick - WASD Movement Control
 */
@Composable
fun LeftJoystick(
    position: Offset,
    onMove: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var currentPos by remember { mutableStateOf(Offset.Zero) }
    
    Box(
        modifier = modifier
            .padding(16.dp)
            .size(120.dp)
            .clip(CircleShape)
            .background(Color(0x40FFFFFF)) // Transparent white
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentPos += dragAmount
                        // Clamp to circle boundary
                        val distance = sqrt(currentPos.x * currentPos.x + currentPos.y * currentPos.y)
                        if (distance > 40f) {
                            currentPos = (currentPos / distance) * 40f
                        }
                        onMove(currentPos)
                    },
                    onDragEnd = {
                        isDragging = false
                        currentPos = Offset.Zero
                        onMove(Offset.Zero)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            // Inner stick
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF9C27B0))
                    .offset(x = currentPos.x.dp / 4, y = currentPos.y.dp / 4)
            )
        }
    }
}

/**
 * Mouse Look Area - Right side drag for camera control
 */
@Composable
fun MouseLookArea(
    position: Offset,
    onMove: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentDelta by remember { mutableStateOf(Offset.Zero) }
    
    Box(
        modifier = modifier
            .fillMaxHeight(0.7f)
            .fillMaxWidth(0.3f)
            .padding(end = 16.dp, bottom = 16.dp)
            .background(Color(0x20FFFFFF))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentDelta = dragAmount
                        onMove(currentDelta)
                    }
                )
            }
    )
}

/**
 * Game Control Button
 */
@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .shadow(8.dp, CircleShape),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPressed) bgColor.copy(alpha = 0.7f) else bgColor,
            contentColor = Color.White
        ),
        shape = CircleShape
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

/**
 * Bottom Hotbar - 9 inventory slots
 */
@Composable
fun Hotbar(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(60.dp)
            .background(Color(0xAA000000), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(9) { index ->
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                    .background(
                        if (index == 0) Color(0xFF9C27B0) else Color(0xFF424242)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}
