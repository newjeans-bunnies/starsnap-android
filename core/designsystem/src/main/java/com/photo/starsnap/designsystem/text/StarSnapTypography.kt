// AUTO-GENERATED from /starsnap-main/starsnap-web/design-system/tokens.json. Do not edit directly.
package com.photo.starsnap.designsystem.text

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.photo.starsnap.designsystem.StarSnapColor

object StarSnapFontSize {
    val micro = 10.sp
    val xs = 12.sp
    val label = 13.sp
    val sm = 14.sp
    val bodySm = 15.sp
    val base = 16.sp
    val lg = 18.sp
    val xl = 20.sp
    val twoXl = 24.sp
    val threeXl = 30.sp
    val fourXl = 36.sp
}

object StarSnapTypography {
    val displayLarge = TextStyle(
        fontFamily = TextFont.pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = StarSnapFontSize.threeXl,
        lineHeight = 36.sp,
        color = StarSnapColor.text,
    )

    val headingLarge = TextStyle(
        fontFamily = TextFont.pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = StarSnapFontSize.twoXl,
        lineHeight = 31.sp,
        color = StarSnapColor.text,
    )

    val heading = TextStyle(
        fontFamily = TextFont.pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = StarSnapFontSize.xl,
        lineHeight = 28.sp,
        color = StarSnapColor.text,
    )

    val title = TextStyle(
        fontFamily = TextFont.pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = StarSnapFontSize.lg,
        lineHeight = 25.sp,
        color = StarSnapColor.text,
    )

    val body = TextStyle(
        fontFamily = TextFont.pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = StarSnapFontSize.base,
        lineHeight = 24.sp,
        color = StarSnapColor.text,
    )

    val bodySmall = TextStyle(
        fontFamily = TextFont.pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = StarSnapFontSize.bodySm,
        lineHeight = 22.sp,
        color = StarSnapColor.text,
    )

    val label = TextStyle(
        fontFamily = TextFont.pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = StarSnapFontSize.sm,
        lineHeight = 20.sp,
        color = StarSnapColor.text,
    )

    val caption = TextStyle(
        fontFamily = TextFont.pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = StarSnapFontSize.xs,
        lineHeight = 18.sp,
        color = StarSnapColor.text,
    )

    val micro = TextStyle(
        fontFamily = TextFont.pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = StarSnapFontSize.micro,
        lineHeight = 14.sp,
        color = StarSnapColor.text,
    )
}
