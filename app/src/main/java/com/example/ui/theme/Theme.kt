package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = TwitterBlue,
    onPrimary = Color.White,
    primaryContainer = TwitterBlueDark,
    onPrimaryContainer = Color.White,
    secondary = TwitterBlueLight,
    onSecondary = TwitterTextPrimary,
    tertiary = TwitterGreen,
    background = Color(0xFF15202B),
    surface = Color(0xFF1E2732),
    onBackground = Color.White,
    onSurface = Color.White,
    outline = Color(0xFF38444D),
    outlineVariant = Color(0xFF273340),
    error = TwitterRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = TwitterBlue,
    onPrimary = Color.White,
    primaryContainer = TwitterBlueLight,
    onPrimaryContainer = TwitterBlueDark,
    secondary = TwitterTextPrimary,
    onSecondary = Color.White,
    secondaryContainer = TwitterBlueLight,
    onSecondaryContainer = TwitterBlue,
    tertiary = TwitterGreen,
    background = TwitterBackground,
    surface = Color.White,
    onBackground = TwitterTextPrimary,
    onSurface = TwitterTextPrimary,
    surfaceVariant = TwitterBackground,
    onSurfaceVariant = TwitterTextSecondary,
    outline = TwitterBorderDarker,
    outlineVariant = TwitterBorder,
    error = TwitterRed,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Enforce consistent Twitter White & Blue identity
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

