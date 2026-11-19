package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class GlassThemePreset(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val accent: Color,
    val accentSecondary: Color,
    val orbPrimary: Color,
    val orbSecondary: Color,
    val orbTertiary: Color
) {
    OCEAN_BLUE(
        id = "1",
        displayName = "Ocean Blue",
        subtitle = "Sapphire & Glacier Refraction",
        accent = Color(0xFF4DA6FF),
        accentSecondary = Color(0xFF7B61FF),
        orbPrimary = Color(0xFF1D6CE0),
        orbSecondary = Color(0xFF00B4D8),
        orbTertiary = Color(0xFF4338CA)
    ),
    LIME(
        id = "2",
        displayName = "Electric Lime",
        subtitle = "Prism Lime & Mint Glass",
        accent = Color(0xFFA3FF47),
        accentSecondary = Color(0xFF2EE59D),
        orbPrimary = Color(0xFF5CCB1A),
        orbSecondary = Color(0xFF0EA571),
        orbTertiary = Color(0xFF22C55E)
    ),
    EMERALD_GREEN(
        id = "3",
        displayName = "Emerald Glass",
        subtitle = "Signature Liquid Emerald",
        accent = Color(0xFF2EF2A1),
        accentSecondary = Color(0xFF38BDF8),
        orbPrimary = Color(0xFF0E9F6E),
        orbSecondary = Color(0xFF0284C7),
        orbTertiary = Color(0xFF14B8A6)
    ),
    CRIMSON_RED(
        id = "4",
        displayName = "Crimson Ruby",
        subtitle = "Ruby & Coral Prism",
        accent = Color(0xFFFF5C7A),
        accentSecondary = Color(0xFFFF9B54),
        orbPrimary = Color(0xFFD92652),
        orbSecondary = Color(0xFFEA580C),
        orbTertiary = Color(0xFFBE123C)
    ),
    SOLAR_YELLOW(
        id = "5",
        displayName = "Solar Amber",
        subtitle = "Liquid Gold & Warm Quartz",
        accent = Color(0xFFFFD15C),
        accentSecondary = Color(0xFFFF8F3F),
        orbPrimary = Color(0xFFD97706),
        orbSecondary = Color(0xFFF59E0B),
        orbTertiary = Color(0xFFB45309)
    ),
    ARCTIC_CYAN(
        id = "6",
        displayName = "Arctic Cyan",
        subtitle = "Glacial Teal & Frost",
        accent = Color(0xFF38F2FF),
        accentSecondary = Color(0xFF60A5FA),
        orbPrimary = Color(0xFF0891B2),
        orbSecondary = Color(0xFF2563EB),
        orbTertiary = Color(0xFF06B6D4)
    );

    companion object {
        fun fromId(id: String?): GlassThemePreset =
            entries.firstOrNull { it.id == id } ?: EMERALD_GREEN
    }
}

@Immutable
data class LiquidGlassPalette(
    val preset: GlassThemePreset,
    val backdropDeep: Color = Color(0xFF050812),
    val backdropMid: Color = Color(0xFF0B1224),
    val backdropElevated: Color = Color(0xFF111B33),
    val glassSurfaceTop: Color = Color(0x28FFFFFF),
    val glassSurfaceBottom: Color = Color(0x0DFFFFFF),
    val glassSurfaceStrongTop: Color = Color(0x38FFFFFF),
    val glassSurfaceStrongBottom: Color = Color(0x16FFFFFF),
    val glassSpecularBright: Color = Color(0x88FFFFFF),
    val glassSpecularDim: Color = Color(0x1EFFFFFF),
    val textPrimary: Color = Color(0xFFF8FAFC),
    val textSecondary: Color = Color(0xB8E2E8F0),
    val textMuted: Color = Color(0x8094A3B8),
    val success: Color = Color(0xFF34D399),
    val warning: Color = Color(0xFFFBBF24),
    val error: Color = Color(0xFFFF5C7A),
    val accent: Color = preset.accent,
    val accentSecondary: Color = preset.accentSecondary
)
