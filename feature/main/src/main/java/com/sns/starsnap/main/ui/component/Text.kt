package com.sns.starsnap.main.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.sns.starsnap.designsystem.R
import com.sns.starsnap.designsystem.StarSnapColor
import com.sns.starsnap.designsystem.text.StarSnapTypography
import com.sns.starsnap.main.viewmodel.auth.PasswordState
import com.sns.starsnap.main.viewmodel.auth.SignupViewModel
import com.sns.starsnap.main.viewmodel.auth.ValidState
import com.sns.starsnap.main.viewmodel.auth.VerifyCodeState

@Composable
fun TextEditHint(text: String) {
    Text(
        text = text,
        style = StarSnapTypography.label.copy(color = StarSnapColor.textMuted),
    )
}

// 닉네임 유효성 검사
@Composable
fun CheckUserNameStatusMessage(viewModel: SignupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val message = when (uiState.usernameValidState) {
        ValidState.ERROR -> MessageState(
            stringResource(R.string.signup_username_screen_error_message),
            StarSnapColor.danger
        )

        ValidState.EXIST -> MessageState(
            stringResource(R.string.signup_username_screen_exist_message),
            StarSnapColor.danger
        )

        ValidState.SUCCESS -> MessageState(
            stringResource(R.string.signup_username_screen_success_message),
            StarSnapColor.success
        )

        ValidState.LOADING -> MessageState(
            stringResource(R.string.signup_username_screen_loading_message),
            StarSnapColor.textMuted
        )

        ValidState.DEFAULT -> MessageState(
            stringResource(R.string.signup_username_screen_default_message),
            StarSnapColor.textMuted
        )

        ValidState.INTERNET_ERROR -> MessageState(
            stringResource(R.string.internet_error),
            StarSnapColor.danger
        )
    }

    // 색상을 자연스럽게 전환하기 위한 애니메이션
    val animatedColor by animateColorAsState(
        targetValue = message.color,
        animationSpec = tween(durationMillis = 300), label = "check_username_message_text"
    )

    // 글자 변경도 자연스럽게 전환하기 위해 Crossfade 사용
    Crossfade(
        targetState = message.text, animationSpec = tween(durationMillis = 300), label = ""
    ) { animatedText ->
        SignupStateMessage(animatedText, animatedColor)
    }
}

// 비밀번호 유효성 검사
@Composable
fun CheckPasswordStatusMessage(viewModel: SignupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val message = when (uiState.passwordValidState) {
        PasswordState.ERROR -> MessageState(
            stringResource(R.string.signup_password_screen_error_message),
            StarSnapColor.danger
        )

        PasswordState.SUCCESS -> MessageState(
            stringResource(R.string.signup_password_screen_success_message),
            StarSnapColor.success
        )

        PasswordState.DEFAULT -> MessageState(
            stringResource(R.string.signup_password_screen_default_message),
            StarSnapColor.textMuted
        )

        PasswordState.INVALID_CONFIRM -> MessageState(
            stringResource(R.string.signup_password_screen_mismatch_message),
            StarSnapColor.danger
        )
        PasswordState.CONFIRM_EMPTY -> MessageState(
            stringResource(R.string.signup_password_screen_confirm_empty_message),
            StarSnapColor.danger
        )
    }

    // 색상을 자연스럽게 전환하기 위한 애니메이션
    val animatedColor by animateColorAsState(
        targetValue = message.color,
        animationSpec = tween(durationMillis = 300), label = "check_password_message_text"
    )

    // 글자 변경도 자연스럽게 전환하기 위해 Crossfade 사용
    Crossfade(
        targetState = message.text, animationSpec = tween(durationMillis = 300), label = ""
    ) { animatedText ->
        SignupStateMessage(animatedText, animatedColor, minLines = 2)
    }
}

// 이메일 유효성 검사
@Composable
fun CheckEmailStatusMessage(viewModel: SignupViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val message = when (uiState.emailValidState) {
        ValidState.ERROR -> MessageState(
            stringResource(R.string.signup_email_screen_error_message),
            StarSnapColor.danger
        )

        ValidState.EXIST -> MessageState(
            stringResource(R.string.signup_email_screen_exist_message),
            StarSnapColor.danger
        )

        ValidState.SUCCESS -> MessageState(
            stringResource(R.string.signup_email_screen_success_message),
            StarSnapColor.success
        )

        ValidState.LOADING -> MessageState(
            stringResource(R.string.signup_email_screen_loading_message),
            StarSnapColor.textMuted
        )

        ValidState.DEFAULT -> MessageState(
            stringResource(R.string.signup_email_screen_default_message),
            StarSnapColor.textMuted
        )

        ValidState.INTERNET_ERROR -> MessageState(
            stringResource(R.string.internet_error),
            StarSnapColor.danger
        )
    }

    // 색상을 자연스럽게 전환하기 위한 애니메이션
    val animatedColor by animateColorAsState(
        targetValue = message.color,
        animationSpec = tween(durationMillis = 300), label = "check_email_message_text"
    )

    // 글자 변경도 자연스럽게 전환하기 위해 Crossfade 사용
    Crossfade(
        targetState = message.text, animationSpec = tween(durationMillis = 300), label = ""
    ) { animatedText ->
        SignupStateMessage(animatedText, animatedColor)
    }
}

// 인증코드 유효성 검사
@Composable
fun CheckVerifyCodeStatusMessage(viewModel: SignupViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val message = when (uiState.verifyCodeState) {
        VerifyCodeState.ERROR -> MessageState(
            stringResource(R.string.signup_verify_screen_error_message),
            StarSnapColor.danger
        )

        VerifyCodeState.SUCCESS -> MessageState(
            stringResource(R.string.signup_verify_screen_success_message),
            StarSnapColor.success
        )

        VerifyCodeState.LOADING -> MessageState(
            stringResource(R.string.signup_verify_screen_loading_message),
            StarSnapColor.textMuted
        )

        VerifyCodeState.DEFAULT -> MessageState(
            stringResource(R.string.signup_verify_screen_default_message),
            StarSnapColor.textMuted
        )

        VerifyCodeState.RESEND -> MessageState(
            stringResource(R.string.signup_verify_screen_resend_message),
            StarSnapColor.textMuted
        )

        VerifyCodeState.INTERNET_ERROR -> MessageState(
            stringResource(R.string.internet_error),
            StarSnapColor.danger
        )
    }

    // 색상을 자연스럽게 전환하기 위한 애니메이션
    val animatedColor by animateColorAsState(
        targetValue = message.color,
        animationSpec = tween(durationMillis = 300), label = "check_verify_code_message_text"
    )

    // 글자 변경도 자연스럽게 전환하기 위해 Crossfade 사용
    Crossfade(
        targetState = message.text, animationSpec = tween(durationMillis = 300), label = ""
    ) { animatedText ->
        SignupStateMessage(animatedText, animatedColor)
    }
}

@Composable
fun SignupStateMessage(text: String, color: Color, minLines: Int = 1) {
    Text(
        text = text,
        style = StarSnapTypography.label,
        color = color,
        minLines = minLines,
    )
}


data class MessageState(val text: String, val color: Color)
