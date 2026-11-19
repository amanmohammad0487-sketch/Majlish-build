package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Spatial iOS 26 Liquid Glass animated atmosphere canvas.
 * Renders soft, continuously drifting luminous refraction orbs behind glass surfaces.
 */
@Composable
fun LiquidSpatialBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalLiquidGlassPalette.current
    val infinite = rememberInfiniteTransition(label = "liquid_orbs")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2.0).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_phase"
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_pulse"
    )

    val orb1 by animateColorAsState(palette.preset.orbPrimary, tween(600), label = "orb1")
    val orb2 by animateColorAsState(palette.preset.orbSecondary, tween(600), label = "orb2")
    val orb3 by animateColorAsState(palette.preset.orbTertiary, tween(600), label = "orb3")

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        palette.backdropDeep,
                        palette.backdropMid,
                        Color(0xFF060A16)
                    )
                )
            )
            val w = size.width
            val h = size.height

            // Top-left primary liquid orb
            val c1 = Offset(
                x = w * (0.18f + 0.08f * cos(phase)),
                y = h * (0.14f + 0.05f * sin(phase))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb1.copy(alpha = 0.50f), orb1.copy(alpha = 0.14f), Color.Transparent),
                    center = c1,
                    radius = w * 0.72f * pulse
                ),
                center = c1,
                radius = w * 0.72f * pulse
            )

            // Mid-right secondary refraction orb
            val c2 = Offset(
                x = w * (0.86f + 0.06f * sin(phase * 1.2f)),
                y = h * (0.42f + 0.07f * cos(phase * 0.9f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb2.copy(alpha = 0.40f), orb2.copy(alpha = 0.10f), Color.Transparent),
                    center = c2,
                    radius = w * 0.68f
                ),
                center = c2,
                radius = w * 0.68f
            )

            // Bottom-left deep prism glow
            val c3 = Offset(
                x = w * (0.28f + 0.10f * sin(phase * 0.7f)),
                y = h * (0.86f + 0.05f * cos(phase * 1.1f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb3.copy(alpha = 0.36f), orb3.copy(alpha = 0.08f), Color.Transparent),
                    center = c3,
                    radius = w * 0.75f
                ),
                center = c3,
                radius = w * 0.75f
            )
        }
        content()
    }
}

/**
 * Translucent iOS 26 Liquid Glass container with Fresnel specular rim, top light sheen,
 * subtle accent tinting, and optional tactile spring press response.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    elevated: Boolean = false,
    accentTint: Color? = null,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalLiquidGlassPalette.current
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.975f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glass_scale"
    )

    val tint = accentTint ?: palette.accent
    val topColor = if (elevated) palette.glassSurfaceStrongTop else palette.glassSurfaceTop
    val bottomColor = if (elevated) palette.glassSurfaceStrongBottom else palette.glassSurfaceBottom

    val surfaceBrush = Brush.verticalGradient(
        colors = listOf(
            topColor,
            tint.copy(alpha = if (elevated) 0.09f else 0.05f),
            bottomColor
        )
    )

    val rimBrush = Brush.linearGradient(
        colors = listOf(
            palette.glassSpecularBright,
            tint.copy(alpha = 0.38f),
            palette.glassSpecularDim,
            Color(0x40FFFFFF)
        ),
        start = Offset(0f, 0f),
        end = Offset(900f, 900f)
    )

    val clickMod = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(color = tint.copy(alpha = 0.35f)),
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .scale(pressScale)
            .shadow(
                elevation = if (elevated) 16.dp else 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.45f),
                spotColor = tint.copy(alpha = 0.20f)
            )
            .clip(shape)
            .background(Color(0x990A1020))
            .background(surfaceBrush)
            .drawWithContent {
                drawContent()
                // Subtle top liquid specular reflection bar
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.24f
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                )
            }
            .border(width = 1.dp, brush = rimBrush, shape = shape)
            .then(clickMod)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    primary: Boolean = true,
    accentOverride: Color? = null
) {
    val palette = LocalLiquidGlassPalette.current
    val accent = accentOverride ?: palette.accent
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled && !loading) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btn_scale"
    )

    val shape = RoundedCornerShape(100.dp)
    val bgBrush = if (primary) {
        Brush.horizontalGradient(
            colors = listOf(
                accent.copy(alpha = if (enabled) 0.92f else 0.35f),
                palette.accentSecondary.copy(alpha = if (enabled) 0.88f else 0.30f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (enabled) 0.16f else 0.06f),
                Color.White.copy(alpha = if (enabled) 0.07f else 0.03f)
            )
        )
    }

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = if (primary) 0.72f else 0.42f),
            accent.copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.16f)
        )
    )

    Box(
        modifier = modifier
            .scale(scale)
            .heightIn(min = 52.dp)
            .shadow(
                elevation = if (primary && enabled) 12.dp else 4.dp,
                shape = shape,
                spotColor = accent.copy(alpha = 0.45f)
            )
            .clip(shape)
            .background(bgBrush)
            .border(1.dp, borderBrush, shape)
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                indication = ripple(color = Color.White.copy(alpha = 0.35f)),
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.2.dp,
                    color = if (primary) Color(0xFF041018) else palette.textPrimary
                )
                Spacer(Modifier.width(10.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = if (primary) Color(0xFF041018) else palette.textPrimary
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                ),
                color = if (primary) Color(0xFF041018) else palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LiquidGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    monospace: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val palette = LocalLiquidGlassPalette.current
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .clip(shape)
            .background(Color(0x99060B18))
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.04f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.36f),
                        palette.accent.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.14f)
                    )
                ),
                shape = shape
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = (if (monospace) TextStyle(fontFamily = MonoFontFamily, fontSize = 14.sp) else MaterialTheme.typography.bodyLarge),
                        color = palette.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = singleLine,
                    textStyle = (if (monospace) {
                        TextStyle(
                            fontFamily = MonoFontFamily,
                            fontSize = 14.sp,
                            color = palette.textPrimary
                        )
                    } else {
                        MaterialTheme.typography.bodyLarge.copy(color = palette.textPrimary)
                    }),
                    cursorBrush = SolidColor(palette.accent),
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (trailingContent != null) {
                Spacer(Modifier.width(8.dp))
                trailingContent()
            }
        }
    }
}

@Composable
fun LiquidGlassStatusPill(
    text: String,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "dot_pulse")
    val alpha by infinite.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "dot_alpha"
    )
    val shape = RoundedCornerShape(100.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Color(0x880B1325))
            .border(1.dp, dotColor.copy(alpha = 0.45f), shape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = alpha))
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White.copy(alpha = 0.92f),
            maxLines = 1
        )
    }
}

@Composable
fun LiquidGlassIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = LocalLiquidGlassPalette.current.accent,
    size: Dp = 44.dp
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(tint.copy(alpha = 0.28f), tint.copy(alpha = 0.08f))
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.48f), tint.copy(alpha = 0.22f))),
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.50f)
        )
    }
}

data class GlassNavDestination(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val activeIcon: ImageVector
)

/**
 * Floating iOS 26 Liquid Glass navigation dock pill with safe navigation bar insets.
 */
@Composable
fun LiquidGlassFloatingNavBar(
    destinations: List<GlassNavDestination>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLiquidGlassPalette.current
    val shape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = 0.65f),
                    spotColor = palette.accent.copy(alpha = 0.25f)
                )
                .clip(shape)
                .background(Color(0xCC091020))
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.06f))
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.50f),
                            palette.accent.copy(alpha = 0.30f),
                            Color.White.copy(alpha = 0.16f)
                        )
                    ),
                    shape = shape
                )
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { dest ->
                val selected = dest.id == selectedId
                val pillBg by animateColorAsState(
                    targetValue = if (selected) palette.accent.copy(alpha = 0.22f) else Color.Transparent,
                    animationSpec = tween(220),
                    label = "nav_pill_bg"
                )
                val pillBorder by animateColorAsState(
                    targetValue = if (selected) palette.accent.copy(alpha = 0.55f) else Color.Transparent,
                    animationSpec = tween(220),
                    label = "nav_pill_border"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (selected) palette.accent else palette.textSecondary,
                    animationSpec = tween(220),
                    label = "nav_content"
                )
                val itemShape = RoundedCornerShape(24.dp)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp)
                        .clip(itemShape)
                        .background(pillBg)
                        .border(1.dp, pillBorder, itemShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = palette.accent.copy(alpha = 0.35f)),
                            onClick = { onSelect(dest.id) }
                        )
                        .padding(vertical = 7.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (selected) dest.activeIcon else dest.icon,
                        contentDescription = dest.label,
                        tint = contentColor,
                        modifier = Modifier.size(21.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = dest.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = BodyFontFamily,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.5.sp
                        ),
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
