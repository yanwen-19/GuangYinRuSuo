package com.guangyinrusuo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val L = lightColorScheme(primary = Color(0xFFC62828), onPrimary = Color.White, primaryContainer = Color(0xFFFFDAD6), secondary = Color(0xFF625B71), tertiary = Color(0xFF2E7D32), background = Color(0xFFF8F9FA), surface = Color.White, onBackground = Color(0xFF1A1A1A), onSurface = Color(0xFF1A1A1A), error = Color(0xFFBA1A1A))

@Composable
fun GuangYinRuSuoTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) darkColorScheme(primary = Color(0xFFFFB4AB), onPrimary = Color(0xFF690005), background = Color(0xFF201A1A), surface = Color(0xFF201A1A)) else L, content = content)
}
