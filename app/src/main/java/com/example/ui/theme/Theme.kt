package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LocalLiquidGlassPalette = staticCompositionLocalOf {
    LiquidGlassPalette(GlassThemePreset.EMERALD_GREEN)
}

val MajlishShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun MajlishTheme(
    themePreset: GlassThemePreset = GlassThemePreset.EMERALD_GREEN,
    content: @Composable () -> Unit
) {
    val palette = remember(themePreset) { LiquidGlassPalette(themePreset) }

    val colorScheme = remember(palette) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = Color(0xFF041118),
            primaryContainer = palette.accent.copy(alpha = 0.22f),
            onPrimaryContainer = palette.textPrimary,
            secondary = palette.accentSecondary,
            onSecondary = Color(0xFF041118),
            secondaryContainer = palette.accentSecondary.copy(alpha = 0.22f),
            onSecondaryContainer = palette.textPrimary,
            tertiary = palette.preset.orbTertiary,
            background = palette.backdropDeep,
            onBackground = palette.textPrimary,
            surface = palette.backdropMid,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.backdropElevated,
            onSurfaceVariant = palette.textSecondary,
            outline = Color(0x38FFFFFF),
            outlineVariant = Color(0x1CFFFFFF),
            error = palette.error,
            onError = Color.White
        )
    }

    CompositionLocalProvider(LocalLiquidGlassPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MajlishTypography,
            shapes = MajlishShapes,
            content = content
        )
    }
}
