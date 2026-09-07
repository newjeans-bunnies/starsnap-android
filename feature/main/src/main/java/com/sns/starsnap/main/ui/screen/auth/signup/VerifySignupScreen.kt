package com.sns.starsnap.main.ui.screen.auth.signup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sns.starsnap.designsystem.R
import com.sns.starsnap.designsystem.StarSnapColor
import com.sns.starsnap.designsystem.text.StarSnapTypography
import com.sns.starsnap.main.ui.component.CheckVerifyCodeStatusMessage
import com.sns.starsnap.main.ui.component.VerifyCodeEditText
import com.sns.starsnap.main.ui.component.VerifyCodeTimer
import com.sns.starsnap.main.ui.screen.auth.AuthFieldLabel
import com.sns.starsnap.main.utils.NavigationRoute.SIGNUP_CONSENT
import com.sns.starsnap.main.viewmodel.auth.SignupViewModel
import com.sns.starsnap.main.viewmodel.auth.VerifyCodeState

@Composable
fun VerifySignupScreen(viewModel: SignupViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()
    val timerUiState by viewModel.timerUiState.collectAsState()
    val resendButtonText by remember {
        derivedStateOf {
            if (timerUiState.resendTime == 0L) {
                "인증번호 재전송"
            } else {
                "${timerUiState.resendTime}초 후 재전송 가능"
            }
        }
    }

    LaunchedEffect(uiState.verifyCodeState) {
        if (uiState.verifyCodeState == VerifyCodeState.SUCCESS) {
            navController.navigate(SIGNUP_CONSENT)
            viewModel.resetVerifyCodeState()
        }
    }

    SignupStepLayout(
        step = 4,
        label = "인증",
        title = stringResource(R.string.signup_verify_screen_title),
        description = "이메일로 전송된 4자리 인증번호를 입력해 주세요.",
        primaryText = "인증하기",
        primaryEnabled = uiState.verifyButtonState,
        onPrimary = { viewModel.checkVerifyCode(viewModel.verifyCode) },
        onBack = { navController.popBackStack() },
    ) {
        AuthFieldLabel(text = "인증번호")
        VerifyCodeEditText(viewModel)
        Spacer(Modifier.height(8.dp))
        CheckVerifyCodeStatusMessage(viewModel)
        Spacer(Modifier.height(12.dp))
        VerifyCodeTimer(timerUiState.timerValue)
        TextButton(
            onClick = { viewModel.sendEmail() },
            enabled = timerUiState.resendTime == 0L,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
        ) {
            Text(
                text = resendButtonText,
                style = StarSnapTypography.caption,
                color = if (timerUiState.resendTime == 0L) {
                    StarSnapColor.text
                } else {
                    StarSnapColor.textMuted
                },
            )
        }
    }
}
