package com.example.autowash.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = GreenAccent,
    tertiary = BlueSecondary,

    background = LightBackground,
    surface = LightSurface,

    onPrimary = LightOnPrimary,
    onBackground = LightOnBackground,
    onSurface = LightOnBackground,

    error = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = BlueSecondary,
    secondary = GreenAccent,
    tertiary = BluePrimary,

    background = DarkBackground,
    surface = DarkSurface,

    onPrimary = DarkOnPrimary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnBackground,

    error = ErrorRed
)

@Composable
fun AutoWashAppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography(),
        content = content
    )
}
