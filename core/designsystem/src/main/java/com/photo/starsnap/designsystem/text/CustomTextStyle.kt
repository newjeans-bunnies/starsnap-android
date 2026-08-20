package com.photo.starsnap.designsystem.text

import androidx.compose.ui.text.font.FontWeight
import com.photo.starsnap.designsystem.StarSnapColor

/**
 * Backward-compatible aliases for the existing screen API.
 * New UI should use [StarSnapTypography] and [StarSnapFontSize].
 */
object CustomTextStyle {
    val TitleLarge = StarSnapTypography.heading
    val TitleMedium = StarSnapTypography.body.copy(fontWeight = FontWeight.SemiBold)
    val TitleSmall = StarSnapTypography.label.copy(fontWeight = FontWeight.SemiBold)

    val title1 = StarSnapTypography.label.copy(fontWeight = FontWeight.Bold)
    val title2 = StarSnapTypography.label
    val title3 = StarSnapTypography.caption
    val title4 = StarSnapTypography.caption.copy(
        fontWeight = FontWeight.Normal,
        color = StarSnapColor.textMuted,
    )
    val title5 = StarSnapTypography.label.copy(fontSize = StarSnapFontSize.label)
    val title6 = StarSnapTypography.micro.copy(color = StarSnapColor.textSubtle)
    val title7 = StarSnapTypography.micro.copy(color = StarSnapColor.textSoft)
    val title8 = StarSnapTypography.micro.copy(fontWeight = FontWeight.SemiBold)
    val title9 = StarSnapTypography.body.copy(
        fontWeight = FontWeight.Medium,
        color = StarSnapColor.textSubtle,
    )

    val body1 = StarSnapTypography.bodySmall.copy(fontWeight = FontWeight.Medium)
    val hint2 = StarSnapTypography.label.copy(
        fontSize = StarSnapFontSize.label,
        color = StarSnapColor.textMuted,
    )
    val hint1 = StarSnapTypography.bodySmall.copy(
        fontWeight = FontWeight.Medium,
        color = StarSnapColor.textMuted,
    )

    val TopBarTitle = StarSnapTypography.title
    val SignupTitle = StarSnapTypography.displayLarge
}
