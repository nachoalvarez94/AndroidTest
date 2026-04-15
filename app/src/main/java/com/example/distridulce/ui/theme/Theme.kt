package com.example.distridulce.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat

private val LightColorScheme = lightColorScheme(
    primary          = BrandBlue,
    onPrimary        = Color.White,
    primaryContainer = IconBgBlue,
    onPrimaryContainer = BrandBlueDark,

    secondary          = ActionPurple,
    onSecondary        = Color.White,
    secondaryContainer = IconBgPurple,
    onSecondaryContainer = ActionPurple,

    tertiary          = ActionGreen,
    onTertiary        = Color.White,
    tertiaryContainer = IconBgGreen,
    onTertiaryContainer = ActionGreen,

    background    = BackgroundLight,
    onBackground  = TextPrimary,
    surface       = Color.White,
    onSurface     = TextPrimary,
    onSurfaceVariant = TextSecondary,

    outline = Color(0xFFE5E7EB),
)

private val DarkColorScheme = darkColorScheme(
    primary   = Color(0xFF93C5FD),
    secondary = Purple80,
    tertiary  = Pink80
)

@Composable
fun DistriDulceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled to ensure consistent brand colours on all devices
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.background.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
