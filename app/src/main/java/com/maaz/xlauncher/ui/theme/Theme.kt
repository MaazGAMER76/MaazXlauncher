package com.maaz.xlauncher.ui.theme

import androidx.compose.foundation.isSystemInDarkMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9C27B0),      // Purple
    onPrimary = Color.White,
    primaryContainer = Color(0xFF7B1FA2),
    onPrimaryContainer = Color(0xFFE1BEE7),
    
    secondary = Color(0xFFE91E63),    // Pink
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC2185B),
    onSecondaryContainer = Color(0xFFF8BBD0),
    
    tertiary = Color(0xFF00BCD4),     // Cyan
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF0097A7),
    onTertiaryContainer = Color(0xFFB2EBF2),
    
    error = Color(0xFFCF6679),
    onError = Color.Black,
    errorContainer = Color(0xFFB1384E),
    onErrorContainer = Color(0xFFF9DEDC),
    
    background = Color(0xFF121212),   // Black
    onBackground = Color.White,
    
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC7D0)
)

@Composable
fun MaazXLauncherTheme(
    darkTheme: Boolean = isSystemInDarkMode(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
