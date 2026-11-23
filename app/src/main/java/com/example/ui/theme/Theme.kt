package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val VedographiDarkColorScheme = darkColorScheme(
    primary = ElectricViolet,
    onPrimary = CrispWhite,
    primaryContainer = RoyalViolet,
    onPrimaryContainer = CrispWhite,
    secondary = SoftLilac,
    onSecondary = ObsidianNight,
    secondaryContainer = ObsidianCardElevated,
    onSecondaryContainer = SoftLilac,
    tertiary = NeonOrchid,
    onTertiary = ObsidianNight,
    background = ObsidianNight,
    onBackground = MistWhite,
    surface = ObsidianSurface,
    onSurface = MistWhite,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = MutedLavender,
    outline = ObsidianOutline,
    outlineVariant = ObsidianCardElevated
)

private val VedographiLightColorScheme = lightColorScheme(
    primary = DeepRoyalPurple,
    onPrimary = PureWhite,
    primaryContainer = LavenderWash,
    onPrimaryContainer = InkBlack,
    secondary = RoyalViolet,
    onSecondary = PureWhite,
    secondaryContainer = LavenderWash,
    onSecondaryContainer = DeepRoyalPurple,
    tertiary = NeonOrchid,
    onTertiary = PureWhite,
    background = AlabasterLavender,
    onBackground = InkBlack,
    surface = PureWhite,
    onSurface = InkBlack,
    surfaceVariant = LavenderWash,
    onSurfaceVariant = InkMuted,
    outline = LavenderBorder,
    outlineVariant = LavenderWash
)

val VedographiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun VedographiTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VedographiDarkColorScheme else VedographiLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VedographiTypography,
        shapes = VedographiShapes,
        content = content
    )
}
