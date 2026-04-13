package com.swipeout.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary          = Accent,
    onPrimary        = TextPrimary,
    primaryContainer = SurfaceHigh,
    background       = Background,
    surface          = Surface,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    outline          = Border,
    error            = Delete,
)

@Composable
fun SwipeOutTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography,
        content     = content,
    )
}
