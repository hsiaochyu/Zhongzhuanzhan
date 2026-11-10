package com.zhongzhuanzhan.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF17181A)
val SubInk = Color(0xFF9AA0A6)
val Accent = Color(0xFF3B7DE0)

val PageTop = Color(0xFFF1EDFB)
val PageMid = Color(0xFFF7F5FC)

val IconBg = Color(0xFFF5F6F8)
val Divider = Color(0xFFE7E7EC)

private val lightColors = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    background = Color(0xFFF7F6FB),
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
)

@Composable
fun HubTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColors, content = content)
}
