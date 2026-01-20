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

// We prioritize Light Theme as per user request
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = SurfaceLight,
    primaryContainer = Primary.copy(alpha = 0.1f),
    onPrimaryContainer = PrimaryDark,
    
    secondary = Secondary,
    onSecondary = SurfaceLight,
    secondaryContainer = Secondary.copy(alpha = 0.1f),
    onSecondaryContainer = Color(0xFF0369A1), // Sky 700

    background = BackgroundLight,
    onBackground = TextPrimary,
    
    surface = SurfaceLight,
    onSurface = TextPrimary,
    
    error = TerribleRed,
    onError = SurfaceLight,
    
    outline = OutlineColor,
    surfaceVariant = BackgroundLight, 
    onSurfaceVariant = TextSecondary
)

// Fallback Dark (mapped to look decent if forced, but system prefers light now)
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = SurfaceLight,
    background = TextPrimary,
    onBackground = SurfaceLight,
    surface = Color(0xFF0F172A), // Slate 900
    onSurface = SurfaceLight
)

@Composable
fun SvoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color to enforce our branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Force light theme preference if user asked for "light theme la venum" (all screens)
    // We will still inspect system setting but prioritize our designed Light Scheme.
    // For now, let's respect system but make Light Scheme default and polished.
    val colorScheme = LightColorScheme // Always use Light Scheme as requested by User

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb() // Make status bar brand color
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false // White text on dark status bar
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}