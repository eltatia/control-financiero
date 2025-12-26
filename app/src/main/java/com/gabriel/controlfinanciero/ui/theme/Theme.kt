package com.gabriel.controlfinanciero.ui.theme

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
    primary = FinanceBlue80,
    onPrimary = FinanceBlue20,
    primaryContainer = FinanceDarkSurfaceVariant,
    onPrimaryContainer = FinanceBlue80,
    secondary = FinanceGreen80,
    onSecondary = FinanceGreen20,
    secondaryContainer = FinanceGreen20,
    onSecondaryContainer = FinanceGreen80,
    tertiary = FinanceDarkOnSurface,
    background = FinanceDarkBackground,
    onBackground = FinanceDarkOnSurface,
    surface = FinanceDarkSurface,
    onSurface = FinanceDarkOnSurface,
    surfaceVariant = FinanceDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB6C2D2),
    outline = Color(0xFF2B3A4A),
    error = Color(0xFFF87171),
    onError = Color(0xFF3F0A0A)
)

private val LightColorScheme = lightColorScheme(
    primary = FinanceBlue40,
    onPrimary = Color.White,
    primaryContainer = FinanceBlue80,
    onPrimaryContainer = FinanceBlue20,
    secondary = FinanceGreen40,
    onSecondary = Color.White,
    secondaryContainer = FinanceGreen80,
    onSecondaryContainer = FinanceGreen20,
    tertiary = FinanceGray40,
    background = FinanceGray90,
    onBackground = FinanceGray20,
    surface = Color.White,
    onSurface = FinanceGray20,
    surfaceVariant = FinanceGray80,
    onSurfaceVariant = FinanceGray40,
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color(0xFF450A0A)
)

@Composable
fun ControlFinancieroAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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
