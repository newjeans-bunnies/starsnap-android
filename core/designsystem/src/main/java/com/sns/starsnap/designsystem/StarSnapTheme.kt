package com.sns.starsnap.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme = lightColorScheme(
    primary = StarSnapLightColors.brand,
    onPrimary = StarSnapLightColors.onBrand,
    primaryContainer = StarSnapLightColors.brandSoft,
    onPrimaryContainer = StarSnapLightColors.text,
    secondary = StarSnapLightColors.info,
    onSecondary = StarSnapLightColors.onDanger,
    secondaryContainer = StarSnapLightColors.infoSoft,
    onSecondaryContainer = StarSnapLightColors.text,
    tertiary = StarSnapLightColors.purple,
    onTertiary = StarSnapLightColors.onDanger,
    tertiaryContainer = StarSnapLightColors.purpleSoft,
    onTertiaryContainer = StarSnapLightColors.text,
    background = StarSnapLightColors.canvas,
    onBackground = StarSnapLightColors.text,
    surface = StarSnapLightColors.surface,
    onSurface = StarSnapLightColors.text,
    surfaceVariant = StarSnapLightColors.surfaceSubtle,
    onSurfaceVariant = StarSnapLightColors.textSoft,
    surfaceTint = StarSnapLightColors.brand,
    inverseSurface = StarSnapLightColors.emphasis,
    inverseOnSurface = StarSnapLightColors.onEmphasis,
    inversePrimary = StarSnapLightColors.brandSoft,
    error = StarSnapLightColors.danger,
    onError = StarSnapLightColors.onDanger,
    errorContainer = StarSnapLightColors.dangerSoft,
    onErrorContainer = StarSnapLightColors.dangerStrong,
    outline = StarSnapLightColors.borderStrong,
    outlineVariant = StarSnapLightColors.border,
    surfaceDim = StarSnapLightColors.surfaceSubtle,
    surfaceBright = StarSnapLightColors.surface,
    surfaceContainerLowest = StarSnapLightColors.surface,
    surfaceContainerLow = StarSnapLightColors.surfaceHover,
    surfaceContainer = StarSnapLightColors.canvas,
    surfaceContainerHigh = StarSnapLightColors.surfaceSubtle,
    surfaceContainerHighest = StarSnapLightColors.border,
    scrim = StarSnapLightColors.mediaBackdrop,
)

private val DarkColorScheme = darkColorScheme(
    primary = StarSnapDarkColors.brand,
    onPrimary = StarSnapDarkColors.onBrand,
    primaryContainer = StarSnapDarkColors.brandSoft,
    onPrimaryContainer = StarSnapDarkColors.text,
    secondary = StarSnapDarkColors.info,
    onSecondary = StarSnapDarkColors.onDanger,
    secondaryContainer = StarSnapDarkColors.infoSoft,
    onSecondaryContainer = StarSnapDarkColors.text,
    tertiary = StarSnapDarkColors.purple,
    onTertiary = StarSnapDarkColors.onDanger,
    tertiaryContainer = StarSnapDarkColors.purpleSoft,
    onTertiaryContainer = StarSnapDarkColors.text,
    background = StarSnapDarkColors.canvas,
    onBackground = StarSnapDarkColors.text,
    surface = StarSnapDarkColors.surface,
    onSurface = StarSnapDarkColors.text,
    surfaceVariant = StarSnapDarkColors.surfaceSubtle,
    onSurfaceVariant = StarSnapDarkColors.textSoft,
    surfaceTint = StarSnapDarkColors.brand,
    inverseSurface = StarSnapDarkColors.emphasis,
    inverseOnSurface = StarSnapDarkColors.onEmphasis,
    inversePrimary = StarSnapDarkColors.brandSoft,
    error = StarSnapDarkColors.danger,
    onError = StarSnapDarkColors.onDanger,
    errorContainer = StarSnapDarkColors.dangerSoft,
    onErrorContainer = StarSnapDarkColors.dangerStrong,
    outline = StarSnapDarkColors.borderStrong,
    outlineVariant = StarSnapDarkColors.border,
    surfaceDim = StarSnapDarkColors.canvas,
    surfaceBright = StarSnapDarkColors.surfaceHover,
    surfaceContainerLowest = StarSnapDarkColors.canvas,
    surfaceContainerLow = StarSnapDarkColors.surface,
    surfaceContainer = StarSnapDarkColors.surfaceSubtle,
    surfaceContainerHigh = StarSnapDarkColors.surfaceHover,
    surfaceContainerHighest = StarSnapDarkColors.border,
    scrim = StarSnapDarkColors.mediaBackdrop,
)

@Composable
fun StarSnapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) StarSnapDarkColors else StarSnapLightColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalStarSnapColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
        ) {
            CompositionLocalProvider(LocalContentColor provides colors.text) {
                content()
            }
        }
    }
}
