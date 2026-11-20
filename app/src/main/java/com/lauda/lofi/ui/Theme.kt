package com.lauda.lofi.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LofiColors = lightColorScheme(
    primary = Color(0xFF8B5E34),
    onPrimary = Color.White,
    secondary = Color(0xFF78634F),
    background = Color(0xFFFFFBF5),
    surface = Color.White,
    surfaceVariant = Color(0xFFF4EEE5),
    onBackground = Color(0xFF29251F),
    onSurface = Color(0xFF29251F),
    outline = Color(0xFFD8CFC3)
)

@Composable
fun LofiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LofiColors, content = content)
}
