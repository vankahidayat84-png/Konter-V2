package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Shinfox Store Dark Color Scheme
private val ShinfoxColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = DarkBackground,
    secondary = GoldSecondary,
    onSecondary = DarkBackground,
    tertiary = ProfitGreen,
    onTertiary = TextWhite,
    background = DarkBackground,
    onBackground = TextWhite,
    surface = RichSurface,
    onSurface = TextWhite,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextWhite,
    error = WarningRed,
    onError = TextWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force darktheme by default as specified by "Background hitam"
    content: @Composable () -> Unit
) {
    // Shinfox specifies Background Hitam, Gold Accent, White Text.
    // So we use our brand color scheme
    MaterialTheme(
        colorScheme = ShinfoxColorScheme,
        typography = Typography,
        content = content
    )
}
