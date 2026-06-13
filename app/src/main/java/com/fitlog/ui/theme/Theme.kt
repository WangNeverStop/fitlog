package com.fitlog.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// First version is a light, warm design only. We keep the API shape (darkTheme param)
// but default to the light scheme to match the agreed visual direction.
private val WarmLightColors = lightColorScheme(
    primary = HoneyYellow,
    onPrimary = WarmTextPrimary,
    secondary = HoneyDeep,
    onSecondary = WarmTextPrimary,
    background = IvoryBackground,
    onBackground = WarmTextPrimary,
    surface = WarmSurface,
    onSurface = WarmTextPrimary,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = WarmTextSecondary,
)

@Composable
fun FitLogTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = WarmLightColors,
        typography = FitLogTypography,
        content = content,
    )
}
