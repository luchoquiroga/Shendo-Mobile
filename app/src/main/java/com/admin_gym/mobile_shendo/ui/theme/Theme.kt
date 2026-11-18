package com.admin_gym.mobile_shendo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

// El diseño es solo claro y con colores propios: sin modo oscuro ni colores dinámicos de Android 12.
private val OrganicColors = lightColorScheme(
    primary = Organic.accent,
    onPrimary = Organic.bg,
    primaryContainer = Organic.accent100,
    onPrimaryContainer = Organic.accent800,
    secondary = Organic.accent2,
    onSecondary = Organic.bg,
    secondaryContainer = Organic.sage100,
    onSecondaryContainer = Organic.sage800,
    tertiary = Organic.sage600,
    background = Organic.bg,
    onBackground = Organic.text,
    surface = Organic.bg,
    onSurface = Organic.text,
    surfaceVariant = Organic.surface,
    onSurfaceVariant = Organic.neutral700,
    surfaceContainerLowest = Organic.neutral100,
    surfaceContainerLow = Organic.surface,
    surfaceContainer = Organic.surface,
    surfaceContainerHigh = Organic.surface,
    surfaceContainerHighest = Organic.neutral300,
    outline = Organic.neutral500,
    outlineVariant = Organic.divider,
    error = Organic.error,
    errorContainer = Organic.errorContainer,
    onErrorContainer = Organic.onErrorContainer,
)

private val OrganicShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun MobileshendoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OrganicColors,
        typography = Typography,
        shapes = OrganicShapes,
        content = content,
    )
}
