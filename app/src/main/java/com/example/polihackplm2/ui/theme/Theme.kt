package com.example.polihackplm2.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = PhishBlueDarkTheme,
    onPrimary = Color.Black,
    primaryContainer = PhishBlueDarkThemeDark,
    onPrimaryContainer = Color.White,
    secondary = PhishOrangeDarkTheme,
    onSecondary = Color.Black,
    tertiary = PhishGreenDarkTheme,
    onTertiary = Color.Black,
    tertiaryContainer = PhishGreenDarkThemeDark,
    error = PhishRedDarkTheme,
    onError = Color.Black,
    errorContainer = PhishRedDarkThemeDark,
    background = PhishBackgroundDark,
    onBackground = Color.White,
    surface = PhishSurfaceDark,
    onSurface = Color.White,
    onSurfaceVariant = Color.LightGray
)

private val LightColorScheme = lightColorScheme(
    primary = PhishBlue,
    onPrimary = Color.White,
    primaryContainer = PhishBlueDark,
    onPrimaryContainer = Color.White,
    secondary = PhishOrange,
    onSecondary = Color.White,
    tertiary = PhishGreen,
    onTertiary = Color.White,
    tertiaryContainer = PhishGreenLight,
    error = PhishRed,
    onError = Color.White,
    errorContainer = PhishRedLight,
    background = PhishBackground,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    onSurfaceVariant = Color.Gray
)

@Composable
fun Polihackplm2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}