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
import com.sns.starsnap.main.ui.component.CheckUserNameStatusMessage
import com.sns.starsnap.main.ui.screen.auth.AuthFieldLabel
import com.sns.starsnap.main.utils.EditTextType
import com.sns.starsnap.main.utils.NavigationRoute.SIGNUP_PASSWORD
import com.sns.starsnap.main.viewmodel.auth.SignupViewModel

@Composable
fun UserNameSignupScreen(
    viewModel: SignupViewModel,
    navController: NavController,
    onNavigateToLogin: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    SignupStepLayout(
        step = 1,
        label = "아이디",
        title = stringResource(R.string.signup_username_screen_title),
        description = "StarSnap에서 사용할 아이디를 입력해 주세요.",
        primaryText = "다음",
        primaryEnabled = uiState.usernameButtonState,
        onPrimary = { navController.navigate(SIGNUP_PASSWORD) },
        onBack = onNavigateToLogin,
        backText = "로그인으로 돌아가기",
    ) {
        AuthFieldLabel(text = "아이디")
        BaseEditText(
            defaultText = viewModel.username,
            hint = "아이디",
            inputText = { viewModel.username = it },
            editTextType = EditTextType.Text,
        )
        Spacer(Modifier.height(8.dp))
        CheckUserNameStatusMessage(viewModel)
    }
}
