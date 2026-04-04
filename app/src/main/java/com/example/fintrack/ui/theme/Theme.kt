package com.example.fintrack.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FinTrackColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = TextOnDark,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = PurpleDark,

    secondary = TealAccent,
    onSecondary = TextOnDark,
    secondaryContainer = Color(0xFFE0FFF7),
    onSecondaryContainer = TealDark,

    tertiary = CoralAccent,
    onTertiary = TextOnDark,
    tertiaryContainer = Color(0xFFFFEBEB),
    onTertiaryContainer = CoralDark,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,

    error = ExpenseRed,
    onError = TextOnDark,
)

@Composable
fun FinTrackTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = FinTrackColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PurplePrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}