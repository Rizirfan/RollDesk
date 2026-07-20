package com.example.crattendance.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = TealOnPrimary,
    primaryContainer = TealLight.copy(alpha = 0.12f),
    onPrimaryContainer = TealDark,
    secondary = TealLight,
    onSecondary = TealOnPrimary,
    secondaryContainer = SurfaceVariantLight,
    onSecondaryContainer = OnSurfacePrimary,
    tertiary = TealDark,
    onTertiary = TealOnPrimary,
    background = BackgroundLight,
    onBackground = OnSurfacePrimary,
    surface = SurfaceLight,
    onSurface = OnSurfacePrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceSecondary,
    outline = BorderLight,
    outlineVariant = BorderLight.copy(alpha = 0.5f),
    error = ErrorRed,
    onError = TealOnPrimary,
    errorContainer = ErrorRed.copy(alpha = 0.1f),
    onErrorContainer = ErrorRed,
)

private val DarkColorScheme = darkColorScheme(
    primary = TealDarkTheme,
    onPrimary = OnSurfacePrimary,
    primaryContainer = TealDark,
    onPrimaryContainer = TealDarkTheme,
    secondary = TealDarkTheme.copy(alpha = 0.8f),
    onSecondary = OnSurfacePrimary,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = OnSurfacePrimaryDark,
    tertiary = TealDarkTheme,
    onTertiary = OnSurfacePrimary,
    background = BackgroundDark,
    onBackground = OnSurfacePrimaryDark,
    surface = SurfaceDark,
    onSurface = OnSurfacePrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceSecondaryDark,
    outline = SurfaceVariantDark,
    outlineVariant = SurfaceVariantDark.copy(alpha = 0.5f),
    error = ErrorRed.copy(alpha = 0.8f),
    onError = OnSurfacePrimary,
    errorContainer = ErrorRed.copy(alpha = 0.15f),
    onErrorContainer = ErrorRed.copy(alpha = 0.9f),
)

@Composable
fun CRAttendanceTheme(
    themeMode: Int = 0,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
