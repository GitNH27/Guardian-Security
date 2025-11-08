package com.example.security_frontend.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import com.example.security_frontend.ui.theme.DarkBackground
import com.example.security_frontend.ui.theme.GoldAccent
import com.example.security_frontend.ui.theme.TextColor
import com.example.security_frontend.ui.theme.Typography

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,       // Buttons, primary active elements
    onPrimary = DarkBackground, // Text on primary buttons
    secondary = GoldAccent,     // Secondary elements
    onSecondary = DarkBackground,
    background = DarkBackground, // Main background color
    onBackground = TextColor,    // Text on background
    surface = DarkBackground,    // Card backgrounds, etc.
    onSurface = TextColor,
    error = Color(0xFFB00020),   // Standard error color
    onError = Color.White
)

// We'll primarily use the dark theme for this design,
// but a light theme can be defined if needed.
private val LightColorScheme = lightColorScheme(
    primary = GoldAccent,
    onPrimary = DarkBackground,
    secondary = GoldAccent,
    onSecondary = DarkBackground,
    background = Color.White, // Using white for a potential light theme background
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    error = Color(0xFFB00020),
    onError = Color.White
)

@Composable
fun SecurityfrontendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // We might want to force dark theme or allow system
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to use our custom colors consistently
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme // Changed to always use DarkColorScheme if darkTheme is true
        else -> LightColorScheme // Fallback to LightColorScheme if darkTheme is false
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            // Adjust status bar icons: true for light icons on dark backgrounds, false for dark icons on light backgrounds
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Ensure Typography is defined in its own file and imported, or defined here.
        content = content
    )
}