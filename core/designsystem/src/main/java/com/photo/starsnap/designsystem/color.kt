package com.photo.starsnap.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Backward-compatible aliases for screens that still use the original token names.
 * New UI should use [StarSnapColor] directly.
 */
object CustomColor {
    val ink: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.text
    val inkSoft: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.textSoft
    val sub: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.textSubtle
    val muted: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.textMuted
    val line: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.border
    val placeholder: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.surfaceSubtle
    val brand: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.brand
    val brandSoft: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.brandSoft
    val danger: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.danger

    val yellow_50 = StarSnapColor.yellow50
    val yellow_100 = StarSnapColor.yellow100
    val yellow_200 = StarSnapColor.yellow200
    val yellow_300 = StarSnapColor.yellow300
    val yellow_400 = StarSnapColor.yellow400
    val yellow_500 = StarSnapColor.yellow500
    val yellow_600 = StarSnapColor.yellow600
    val yellow_700 = StarSnapColor.yellow700
    val yellow_800 = StarSnapColor.yellow800
    val yellow_900 = StarSnapColor.yellow800

    val light_black: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.text
    val title: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.textSoft
    val sub_title: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.textSubtle
    val gray: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.textMuted
    val light_gray: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.borderStrong
    val button: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.borderStrong
    val container: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.surfaceSubtle

    val error: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.danger
    val success: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.success

    val background: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.canvas
    val surface: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.surface
    val border: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.border
    val primary: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.brand
    val primarySoft: Color
        @Composable @ReadOnlyComposable get() = StarSnapColor.brandSoft
}
