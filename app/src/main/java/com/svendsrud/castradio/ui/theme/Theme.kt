package com.svendsrud.castradio.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
    primary = BrandPurple,
    onPrimary = Color.White,
    primaryContainer = BrandPurpleDeep,
    onPrimaryContainer = Color.White,
    secondary = BrandCyan,
    onSecondary = Color.Black,
    tertiary = BrandCyan,
    onTertiary = Color.Black,
    background = MidnightBackground,
    onBackground = Color.White,
    surface = MidnightSurface,
    onSurface = Color.White,
    onSurfaceVariant = MidnightTextSecondary
)

private val LightColors = lightColorScheme(
    primary = BrandPurpleDeep,
    onPrimary = Color.White,
    primaryContainer = BrandPurple,
    onPrimaryContainer = Color.White,
    secondary = BrandCyanDeep,
    onSecondary = Color.White,
    tertiary = BrandCyanDeep,
    onTertiary = Color.White,
    background = DayBackground,
    onBackground = Color(0xFF1B1B24),
    surface = DaySurface,
    onSurface = Color(0xFF1B1B24),
    onSurfaceVariant = DayTextSecondary
)

@Composable
fun CastRadioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = CastRadioTypography,
        content = content
    )
}
