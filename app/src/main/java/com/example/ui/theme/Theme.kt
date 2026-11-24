package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val VedographiDarkColorScheme = darkColorScheme(
    primary = ElectricViolet,
    onPrimary = ObsidianNight,
    primaryContainer = DeepPlumContainer,
    onPrimaryContainer = SoftLilac,
    secondary = SoftLilac,
    onSecondary = ObsidianNight,
    secondaryContainer = ObsidianCardElevated,
    onSecondaryContainer = SoftLilac,
    tertiary = NeonOrchid,
    onTertiary = ObsidianNight,
    tertiaryContainer = DeepPlumContainer,
    onTertiaryContainer = SoftLilac,
    background = ObsidianNight,
    onBackground = MistWhite,
    surface = ObsidianSurface,
    onSurface = MistWhite,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = MutedLavender,
    surfaceTint = ElectricViolet,
    surfaceContainerLowest = ObsidianNight,
    surfaceContainerLow = ObsidianSurfaceLow,
    surfaceContainer = ObsidianCard,
    surfaceContainerHigh = ObsidianCardElevated,
    surfaceContainerHighest = ObsidianSurfaceHighest,
    inverseSurface = MistWhite,
    inverseOnSurface = InkBlack,
    inversePrimary = DeepRoyalPurple,
    outline = ObsidianOutline,
    outlineVariant = ObsidianOutlineSubtle,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

private val VedographiLightColorScheme = lightColorScheme(
    primary = DeepRoyalPurple,
    onPrimary = PureWhite,
    primaryContainer = LavenderWash,
    onPrimaryContainer = LightPlumText,
    secondary = RoyalViolet,
    onSecondary = PureWhite,
    secondaryContainer = LavenderSurfaceHigh,
    onSecondaryContainer = LightPlumText,
    tertiary = NeonOrchid,
    onTertiary = PureWhite,
    tertiaryContainer = LavenderWash,
    onTertiaryContainer = LightPlumText,
    background = AlabasterLavender,
    onBackground = InkBlack,
    surface = PureWhite,
    onSurface = InkBlack,
    surfaceVariant = LavenderWash,
    onSurfaceVariant = InkMuted,
    surfaceTint = DeepRoyalPurple,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = LavenderSurfaceLow,
    surfaceContainer = LavenderWash,
    surfaceContainerHigh = LavenderSurfaceHigh,
    surfaceContainerHighest = LavenderSurfaceHighest,
    inverseSurface = ObsidianSurface,
    inverseOnSurface = MistWhite,
    inversePrimary = ElectricViolet,
    outline = LavenderBorder,
    outlineVariant = LavenderOutlineSubtle,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer
)

val VedographiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
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
