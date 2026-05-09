package com.personalbiography.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors =
    lightColorScheme(
        primary = Color(0xFF2A6FDB),
        onPrimary = Color.White,
        secondary = Color(0xFF26A69A),
        surface = Color(0xFFF5F7FB),
        background = Color(0xFFFFFFFF),
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFF8AB4F8),
        secondary = Color(0xFF80CBC4),
        surface = Color(0xFF1B1F24),
        background = Color(0xFF121417),
    )

@Composable
fun PersonalBiographyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content,
    )
}
