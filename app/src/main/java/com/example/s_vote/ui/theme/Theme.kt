package com.example.s_vote.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

// We prioritize a Deep Professional Theme as per user request
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Primary.copy(alpha = 0.2f),
    onPrimaryContainer = Secondary,
    
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = Secondary.copy(alpha = 0.15f),
    onSecondaryContainer = Accent,

    background = BackgroundLight,
    onBackground = TextPrimary,
    
    surface = SurfaceLight,
    onSurface = TextPrimary,
    
    error = Error,
    onError = Color.White,
    
    outline = OutlineColor,
    surfaceVariant = SurfaceVariant, 
    onSurfaceVariant = TextSecondary
)

// Fallback Dark (kept for reference, but SvoteTheme now forces light)
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = SurfaceLight,
    background = TextPrimary,
    onBackground = SurfaceLight,
    surface = Color(0xFF0F172A), // Slate 900
    onSurface = SurfaceLight
)

@Composable
fun EvoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic color to enforce our branding
    content: @Composable () -> Unit
) {
    // Force light theme as per user requirement (ignore system dark mode)
    val colorScheme = LightColorScheme 

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundLight.toArgb() 
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true 
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}