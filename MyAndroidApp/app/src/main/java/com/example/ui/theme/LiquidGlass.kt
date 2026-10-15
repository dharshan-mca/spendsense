package com.example.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Apple Liquid Glass Specular & Translucency Tokens
object LiquidGlassTokens {
    // Dark mode glass palette (deep obsidian, optical crystal, teal refractions)
    val DarkBase = Color(0xFF090D16)
    val DarkGlassSurfaceTop = Color(0x28FFFFFF)     // 16% white specular sheen
    val DarkGlassSurfaceBottom = Color(0x0EFFFFFF)  // 5% white translucent floor
    val DarkGlassBorderTop = Color(0x55FFFFFF)      // 33% white top specular rim
    val DarkGlassBorderBottom = Color(0x12FFFFFF)   // 7% white bottom edge

    // Light mode glass palette (off-white pearl, milk glass translucency)
    val LightBase = Color(0xFFF8F7F4)
    val LightGlassSurfaceTop = Color(0xE6FFFFFF)    // 90% white frosted top
    val LightGlassSurfaceBottom = Color(0xB3F0F4F8) // 70% soft pale glass floor
    val LightGlassBorderTop = Color(0x99FFFFFF)     // 60% white specular rim
    val LightGlassBorderBottom = Color(0x3394A3B8)  // 20% soft slate bottom edge

    // Luminous Chromatic Refractions (Apple visionOS vibe)
    val RefractTeal = Color(0xFF0F5F53)
    val RefractEmerald = Color(0xFF10B981)
    val RefractCyan = Color(0xFF06B6D4)
    val RefractElectricLime = Color(0xFF84CC16)
    val RefractAmber = Color(0xFFF59E0B)
    val RefractIndigo = Color(0xFF6366F1)
}

/**
 * Creates an Apple Liquid Glass brush with a subtle top-down specular gradient.
 */
@Composable
fun liquidGlassSurfaceBrush(isDark: Boolean = isSystemInDarkTheme()): Brush {
    return if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                LiquidGlassTokens.DarkGlassSurfaceTop,
                LiquidGlassTokens.DarkGlassSurfaceBottom
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                LiquidGlassTokens.LightGlassSurfaceTop,
                LiquidGlassTokens.LightGlassSurfaceBottom
            )
        )
    }
}

/**
 * Creates an Apple-style specular hairline border brush with directional light refraction.
 */
@Composable
fun liquidGlassBorderBrush(
    isDark: Boolean = isSystemInDarkTheme(),
    accentColor: Color? = null
): Brush {
    return if (isDark) {
        if (accentColor != null) {
            Brush.linearGradient(
                colors = listOf(
                    LiquidGlassTokens.DarkGlassBorderTop,
                    accentColor.copy(alpha = 0.6f),
                    LiquidGlassTokens.DarkGlassBorderBottom
                ),
                start = Offset(0f, 0f),
                end = Offset(400f, 600f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    LiquidGlassTokens.DarkGlassBorderTop,
                    LiquidGlassTokens.DarkGlassBorderBottom
                ),
                start = Offset(0f, 0f),
                end = Offset(0f, 600f)
            )
        }
    } else {
        if (accentColor != null) {
            Brush.linearGradient(
                colors = listOf(
                    LiquidGlassTokens.LightGlassBorderTop,
                    accentColor.copy(alpha = 0.45f),
                    LiquidGlassTokens.LightGlassBorderBottom
                ),
                start = Offset(0f, 0f),
                end = Offset(400f, 600f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    LiquidGlassTokens.LightGlassBorderTop,
                    LiquidGlassTokens.LightGlassBorderBottom
                ),
                start = Offset(0f, 0f),
                end = Offset(0f, 600f)
            )
        }
    }
}

/**
 * Modifier extension to apply the complete Liquid Glass effect:
 * - Rounded squircle clipping
 * - Frosted translucent fill
 * - Double-specular hairline border
 * - Top-edge specular inner gleam
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    surfaceBrush: Brush,
    borderBrush: Brush,
    elevation: Dp = 6.dp
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = Color.Black.copy(alpha = 0.25f),
        spotColor = Color.Black.copy(alpha = 0.25f)
    )
    .clip(shape)
    .background(brush = surfaceBrush)
    .border(
        width = borderWidth,
        brush = borderBrush,
        shape = shape
    )
    .drawBehind {
        // Draw delicate top-edge specular highlight line
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.25f),
                    Color.Transparent
                )
            ),
            start = Offset(20f, 1f),
            end = Offset(size.width - 20f, 1f),
            strokeWidth = 1.5f
        )
    }

/**
 * High-fidelity Liquid Glass Card container component.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 6.dp,
    accentBorder: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val surfaceBrush = liquidGlassSurfaceBrush(isDark)
    val borderBrush = liquidGlassBorderBrush(isDark, accentBorder)

    Box(
        modifier = modifier
            .liquidGlass(
                shape = shape,
                borderWidth = borderWidth,
                surfaceBrush = surfaceBrush,
                borderBrush = borderBrush,
                elevation = elevation
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            content = content
        )
    }
}

/**
 * Liquid Glass Pill capsule for buttons, selectors, chips, and small controls.
 */
@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(50),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 4.dp,
    accentBorder: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val surfaceBrush = liquidGlassSurfaceBrush(isDark)
    val borderBrush = liquidGlassBorderBrush(isDark, accentBorder)

    Box(
        modifier = modifier
            .liquidGlass(
                shape = shape,
                borderWidth = borderWidth,
                surfaceBrush = surfaceBrush,
                borderBrush = borderBrush,
                elevation = elevation
            ),
        content = content
    )
}

/**
 * Ambient Liquid Animated Canvas:
 * Provides fluid chromatic orbs floating subtly behind the glass cards,
 * simulating physical refraction and light scattering.
 */
@Composable
fun AmbientLiquidBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // Smooth continuous floating animations for chromatic orbs
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_liquid_orbs")

    val orb1X by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1_x"
    )

    val orb1Y by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1_y"
    )

    val orb2X by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2_x"
    )

    val orb2Y by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(13000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2_y"
    )

    val orb3Scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb3_scale"
    )

    val baseBgColor = if (isDark) LiquidGlassTokens.DarkBase else LiquidGlassTokens.LightBase
    val orbTeal = if (isDark) LiquidGlassTokens.RefractTeal.copy(alpha = 0.45f) else LiquidGlassTokens.RefractTeal.copy(alpha = 0.15f)
    val orbEmerald = if (isDark) LiquidGlassTokens.RefractEmerald.copy(alpha = 0.35f) else LiquidGlassTokens.RefractEmerald.copy(alpha = 0.12f)
    val orbCyan = if (isDark) LiquidGlassTokens.RefractCyan.copy(alpha = 0.25f) else LiquidGlassTokens.RefractCyan.copy(alpha = 0.10f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBgColor)
            .drawBehind {
                val w = size.width
                val h = size.height

                // Draw Top-Right Fluid Teal Orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orbTeal, Color.Transparent),
                        center = Offset(w * orb1X, h * orb1Y),
                        radius = w * 0.75f
                    ),
                    center = Offset(w * orb1X, h * orb1Y),
                    radius = w * 0.75f
                )

                // Draw Bottom-Left Fluid Emerald/Cyan Orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orbEmerald, Color.Transparent),
                        center = Offset(w * orb2X, h * orb2Y),
                        radius = w * 0.85f
                    ),
                    center = Offset(w * orb2X, h * orb2Y),
                    radius = w * 0.85f
                )

                // Draw Center-Right Fluid Cyan Sheen
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orbCyan, Color.Transparent),
                        center = Offset(w * 0.65f, h * 0.55f),
                        radius = w * 0.6f * orb3Scale
                    ),
                    center = Offset(w * 0.65f, h * 0.55f),
                    radius = w * 0.6f * orb3Scale
                )
            }
    ) {
        content()
    }
}
