package com.sns.starsnap.main.ui.screen.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sns.starsnap.designsystem.StarSnapColor
import com.sns.starsnap.designsystem.text.StarSnapTypography
import com.sns.starsnap.main.viewmodel.auth.PasswordResetStep
import com.sns.starsnap.main.viewmodel.auth.PasswordResetViewModel

@Composable
fun PasswordResetScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: PasswordResetViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    BackHandler(enabled = state.step != PasswordResetStep.EMAIL) {
        if (!state.isLoading) {
            if (state.step == PasswordResetStep.SUCCESS) onNavigateToLogin() else viewModel.goBack()
        }
    }

    AuthCardPage(maxWidth = 420.dp) {
        AuthBrandHeader(subtitle = "이메일 인증으로 비밀번호를 다시 설정하세요")
        Spacer(Modifier.height(24.dp))

        when (state.step) {
            PasswordResetStep.EMAIL -> {
                ResetHeading("비밀번호 찾기", "가입한 이메일로 인증번호를 보내드립니다.")
                ResetField(
                    label = "이메일",
                    value = state.email,
                    onValueChange = viewModel::updateEmail,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                )
            }

            PasswordResetStep.CODE -> {
                ResetHeading("인증번호 입력", "${state.email}로 보낸 6자리 번호를 입력해 주세요.")
                ResetField(
                    label = "인증번호",
                    value = state.code,
                    onValueChange = viewModel::updateCode,
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done,
                )
            }

            PasswordResetStep.PASSWORD -> {
                ResetHeading("새 비밀번호", "8~50자의 영문 대·소문자, 숫자, 특수문자를 포함해 주세요.")
                ResetField(
                    label = "새 비밀번호",
                    value = state.newPassword,
                    onValueChange = viewModel::updateNewPassword,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation(),
                )
                Spacer(Modifier.height(12.dp))
                ResetField(
                    label = "새 비밀번호 확인",
                    value = state.confirmPassword,
                    onValueChange = viewModel::updateConfirmPassword,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    visualTransformation = PasswordVisualTransformation(),
                )
            }

            PasswordResetStep.SUCCESS -> {
                Text(
                    text = "비밀번호가 변경되었습니다.",
                    style = StarSnapTypography.headingLarge,
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = "비밀번호 변경 완료"
                    },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "새 비밀번호로 다시 로그인해 주세요.",
                    style = StarSnapTypography.bodySmall.copy(color = StarSnapColor.textSubtle),
                )
            }
        }

        state.errorMessage?.let { message ->
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StarSnapColor.dangerSoft, RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Assertive
                        contentDescription = message
                    },
            ) {
                Text(message, style = StarSnapTypography.caption, color = StarSnapColor.danger)
            }
        }

        state.statusMessage?.takeIf { state.errorMessage == null }?.let { message ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = StarSnapTypography.caption,
                color = StarSnapColor.success,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }

        Spacer(Modifier.height(20.dp))
        when (state.step) {
            PasswordResetStep.EMAIL -> AuthPrimaryButton(
                text = when {
                    state.isLoading -> "전송 중..."
                    state.resendSecondsRemaining > 0 ->
                        "인증번호 받기 (${state.resendSecondsRemaining}초)"
                    else -> "인증번호 받기"
                },
                enabled = !state.isLoading && state.resendSecondsRemaining == 0L,
                onClick = viewModel::requestCode,
            )
            PasswordResetStep.CODE -> {
                AuthPrimaryButton(
                    text = if (state.isLoading) "확인 중..." else "인증번호 확인",
                    enabled = state.code.length == 6 && !state.isLoading,
                    onClick = viewModel::verifyCode,
                )
                AuthSecondaryButton(
                    text = if (state.resendSecondsRemaining > 0) {
                        "인증번호 다시 받기 (${state.resendSecondsRemaining}초)"
                    } else {
                        "인증번호 다시 받기"
                    },
                    enabled = state.resendSecondsRemaining == 0L && !state.isLoading,
                    onClick = viewModel::requestCode,
                )
            }
            PasswordResetStep.PASSWORD -> AuthPrimaryButton(
                text = if (state.isLoading) "변경 중..." else "비밀번호 변경",
                enabled = !state.isLoading,
                onClick = viewModel::confirmPassword,
            )
            PasswordResetStep.SUCCESS -> AuthPrimaryButton(
                text = "로그인으로 돌아가기",
                enabled = true,
                onClick = onNavigateToLogin,
            )
        }

        if (state.step != PasswordResetStep.SUCCESS) {
            AuthSecondaryButton(
                text = when (state.step) {
                    PasswordResetStep.EMAIL -> "로그인으로 돌아가기"
                    PasswordResetStep.PASSWORD -> "이메일 인증 다시 시작"
                    else -> "이전"
                },
                enabled = !state.isLoading,
                onClick = if (state.step == PasswordResetStep.EMAIL) onNavigateToLogin else viewModel::goBack,
            )
        }
    }
}

@Composable
private fun ResetHeading(title: String, description: String) {
    Text(title, style = StarSnapTypography.headingLarge)
    Spacer(Modifier.height(8.dp))
    Text(
        description,
        style = StarSnapTypography.bodySmall.copy(color = StarSnapColor.textSubtle),
    )
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun ResetField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    imeAction: ImeAction = ImeAction.Next,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = label },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = StarSnapColor.brandActive,
            unfocusedBorderColor = StarSnapColor.border,
            focusedContainerColor = StarSnapColor.surface,
            unfocusedContainerColor = StarSnapColor.surface,
        ),
    )
}
