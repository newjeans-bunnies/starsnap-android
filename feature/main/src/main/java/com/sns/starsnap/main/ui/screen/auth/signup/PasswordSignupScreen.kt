package com.sns.starsnap.main.ui.screen.auth.signup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sns.starsnap.designsystem.R
import com.sns.starsnap.main.ui.component.BaseEditText
import com.sns.starsnap.main.ui.component.CheckPasswordStatusMessage
import com.sns.starsnap.main.ui.screen.auth.AuthFieldLabel
import com.sns.starsnap.main.utils.EditTextType
import com.sns.starsnap.main.utils.NavigationRoute.SIGNUP_EMAIL
import com.sns.starsnap.main.viewmodel.auth.SignupViewModel

@Composable
fun PasswordSignupScreen(viewModel: SignupViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()

    SignupStepLayout(
        step = 2,
        label = "비밀번호",
        title = stringResource(R.string.signup_password_screen_title),
        description = "안전한 비밀번호를 만들고 한 번 더 확인해 주세요.",
        primaryText = "다음",
        primaryEnabled = uiState.passwordButtonState,
        onPrimary = { navController.navigate(SIGNUP_EMAIL) },
        onBack = { navController.popBackStack() },
    ) {
        AuthFieldLabel(text = "비밀번호")
        BaseEditText(
            defaultText = viewModel.password,
            hint = "비밀번호",
            inputText = { viewModel.password = it },
            editTextType = EditTextType.Password,
        )
        Spacer(Modifier.height(14.dp))
        AuthFieldLabel(text = "비밀번호 확인")
        BaseEditText(
            defaultText = viewModel.confirmPassword,
            hint = "비밀번호 확인",
            inputText = { viewModel.confirmPassword = it },
            editTextType = EditTextType.Password,
        )
        Spacer(Modifier.height(8.dp))
        CheckPasswordStatusMessage(viewModel)
    }
}
