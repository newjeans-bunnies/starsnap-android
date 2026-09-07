package com.sns.starsnap.main.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.sns.starsnap.designsystem.R
import com.sns.starsnap.designsystem.StarSnapColor
import com.sns.starsnap.designsystem.text.StarSnapTypography
import com.sns.starsnap.main.utils.clickableSingle

private val ButtonShape = RoundedCornerShape(12.dp)
private val DefaultButtonHeight = 48.dp

// 로그인, 회원가입 등 메인 버튼으로 사용
@Composable
fun MainButton(event: () -> Unit, enabled: Boolean, buttonText: String) {
    val buttonBackground = if (enabled) StarSnapColor.brand else StarSnapColor.brandSoft
    val buttonTextColor = if (enabled) StarSnapColor.onBrand else StarSnapColor.textMuted
    Box(
        Modifier
            .clickableSingle(
                enabled = enabled,
                onClickLabel = buttonText,
                role = Role.Button,
                onClick = event
            )
            .height(DefaultButtonHeight)
            .fillMaxWidth()
            .background(buttonBackground, ButtonShape)
    ) {
        Text(
            buttonText,
            Modifier.align(Alignment.Center),
            style = StarSnapTypography.label.copy(
                color = buttonTextColor,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

// 텍스트 버튼
@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    buttonState: Boolean = true
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
            .clickableSingle(
                enabled = buttonState,
                onClickLabel = text,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = textStyle, color = StarSnapColor.text)
    }
}

// 애플 로그인 버튼(안드로이드에서는 사용할 일이 없음)
@Composable
fun AppleLoginButton(onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(DefaultButtonHeight)
            .background(StarSnapColor.text, ButtonShape)
            .clickableSingle(
                onClickLabel = "Apple로 로그인",
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Image(
            painterResource(R.drawable.apple_icon),
            null,
            Modifier
                .padding(start = 20.dp)
                .align(Alignment.CenterStart)
        )
        Text(
            stringResource(R.string.apple_login_button_text),
            Modifier.align(Alignment.Center),
            style = StarSnapTypography.label.copy(
                color = StarSnapColor.surface,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

// 구글 로그인 버튼
@Composable
fun GoogleLoginButton(onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(DefaultButtonHeight)
            .background(StarSnapColor.surface, ButtonShape)
            .border(width = 1.dp, shape = ButtonShape, color = StarSnapColor.border)
            .clickableSingle(
                onClickLabel = "Google로 로그인",
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Image(
            painterResource(R.drawable.google_icon),
            null,
            Modifier
                .padding(start = 20.dp)
                .align(Alignment.CenterStart)
        )
        Text(
            text = stringResource(R.string.google_login_button_text),
            modifier = Modifier.align(Alignment.Center),
            style = StarSnapTypography.label.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
fun SubmitButton(onClick: () -> Unit, buttonText: String, enabled: Boolean) {
    val buttonBackground = if (enabled) StarSnapColor.brand else StarSnapColor.brandSoft
    val buttonTextColor = if (enabled) StarSnapColor.onBrand else StarSnapColor.textMuted

    Box(
        Modifier
            .height(DefaultButtonHeight)
            .width(90.dp)
            .background(buttonBackground, ButtonShape)
            .clickableSingle(
                enabled = enabled,
                onClickLabel = buttonText,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp)
    ) {
        Text(
            buttonText,
            Modifier.align(Alignment.Center),
            style = StarSnapTypography.label.copy(
                color = buttonTextColor,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
fun NextButton(event: () -> Unit, enabled: Boolean, buttonText: String) {
    val targetColor = if (enabled) StarSnapColor.brand else StarSnapColor.brandSoft
    val buttonTextColor = if (enabled) StarSnapColor.onBrand else StarSnapColor.textMuted
    val buttonBackground by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 150),
        label = "next_button_background",
    )

    Box(
        Modifier
            .clickableSingle(
                enabled = enabled,
                onClickLabel = buttonText,
                role = Role.Button,
                onClick = event,
            )
            .height(DefaultButtonHeight)
            .fillMaxWidth()
            .background(buttonBackground, ButtonShape)
    ) {
        Text(
            text = buttonText,
            modifier = Modifier.align(Alignment.Center),
            style = StarSnapTypography.label.copy(
                color = buttonTextColor,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
fun LikeIconButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    IconToggleButton(
        checked = checked,
        enabled = enabled,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.size(48.dp),
    ) {
        Icon(
            imageVector = if (checked) ImageVector.vectorResource(id = R.drawable.like_true_icon) else ImageVector.vectorResource(id = R.drawable.like_false_icon),
            contentDescription = if (checked) "좋아요 취소" else "좋아요",
            modifier = Modifier.size(20.dp),
            tint = if (checked) StarSnapColor.danger else StarSnapColor.textSubtle,
        )
    }
}

@Composable
fun CommentIconButton() {
    IconButton(onClick = { /* 클릭 이벤트 */ }, modifier = Modifier.size(48.dp)) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.comment_icon),
            contentDescription = "댓글",
            modifier = Modifier.size(20.dp),
            tint = StarSnapColor.textSubtle,
        )
    }
}

@Composable
fun SaveIconButton() {
    IconButton(onClick = { /* 클릭 이벤트 */ }, modifier = Modifier.size(48.dp)) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.save_icon),
            contentDescription = "저장",
            modifier = Modifier.size(20.dp),
            tint = StarSnapColor.textSubtle,
        )
    }
}
