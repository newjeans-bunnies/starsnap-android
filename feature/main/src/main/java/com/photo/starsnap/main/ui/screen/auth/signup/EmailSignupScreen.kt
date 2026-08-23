package com.photo.starsnap.main.ui.screen.auth.signup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.photo.starsnap.designsystem.R
import com.photo.starsnap.main.ui.component.BaseEditText
import com.photo.starsnap.main.ui.component.CheckEmailStatusMessage
import com.photo.starsnap.main.ui.screen.auth.AuthFieldLabel
import com.photo.starsnap.main.utils.EditTextType
import com.photo.starsnap.main.utils.NavigationRoute.SIGNUP_VERIFY
import com.photo.starsnap.main.viewmodel.auth.SignupViewModel

@Composable
fun EmailSignupScreen(viewModel: SignupViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()

    SignupStepLayout(
        step = 3,
        label = "이메일",
        title = stringResource(R.string.signup_email_screen_title),
        description = "계정 확인과 복구에 사용할 이메일을 입력해 주세요.",
        primaryText = "인증번호 전송",
        primaryEnabled = uiState.emailSendButtonState,
        onPrimary = {
            navController.navigate(SIGNUP_VERIFY)
            viewModel.sendEmail()
        },
        onBack = { navController.popBackStack() },
    ) {
        AuthFieldLabel(text = "이메일")
        BaseEditText(
            defaultText = viewModel.email,
            hint = "이메일",
            inputText = { viewModel.email = it },
            editTextType = EditTextType.Email,
        )
        Spacer(Modifier.height(8.dp))
        CheckEmailStatusMessage(viewModel)
    }
}
