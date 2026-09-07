package com.sns.starsnap.designsystem.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.sns.starsnap.designsystem.StarSnapColor

/**
 * Backward-compatible aliases for the existing screen API.
 * New UI should use [StarSnapTypography] and [StarSnapFontSize].
 */
object CustomTextStyle {
    val TitleLarge: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.heading
    val TitleMedium: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.body.copy(fontWeight = FontWeight.SemiBold)
    val TitleSmall: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.label.copy(fontWeight = FontWeight.SemiBold)

    val title1: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.label.copy(fontWeight = FontWeight.Bold)
    val title2: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.label
    val title3: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.caption
    val title4: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.caption.copy(
            fontWeight = FontWeight.Normal,
            color = StarSnapColor.textMuted,
        )
    val title5: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.label.copy(fontSize = StarSnapFontSize.label)
    val title6: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.micro.copy(color = StarSnapColor.textSubtle)
    val title7: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.micro.copy(color = StarSnapColor.textSoft)
    val title8: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.micro.copy(fontWeight = FontWeight.SemiBold)
    val title9: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.body.copy(
            fontWeight = FontWeight.Medium,
            color = StarSnapColor.textSubtle,
        )

    val body1: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.bodySmall.copy(fontWeight = FontWeight.Medium)
    val hint2: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.label.copy(
            fontSize = StarSnapFontSize.label,
            color = StarSnapColor.textMuted,
        )
    val hint1: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.bodySmall.copy(
            fontWeight = FontWeight.Medium,
            color = StarSnapColor.textMuted,
        )

    val TopBarTitle: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.title
    val SignupTitle: TextStyle
        @Composable @ReadOnlyComposable get() = StarSnapTypography.displayLarge
}
