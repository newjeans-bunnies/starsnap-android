package com.photo.starsnap.main.ui.screen.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.photo.starsnap.designsystem.R
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.StarSnapTypography
import com.photo.starsnap.main.ui.component.EditText
import com.photo.starsnap.main.ui.component.PasswordEditText
import com.photo.starsnap.main.viewmodel.auth.LoginViewModel
import com.photo.starsnap.main.viewmodel.state.LoginState

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    moveSignupNavigation: () -> Unit,
    moveMainNavigation: () -> Unit,
) {
    LaunchedEffect(Unit) {
        Log.d("화면", "LoginScreen")
    }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by loginViewModel.loginState.observeAsState(LoginState.Idle)
    val canLogin = username.isNotBlank() && password.isNotBlank() && loginState != LoginState.Loading

    LaunchedEffect(loginState) {
        if (loginState == LoginState.Success) moveMainNavigation()
    }

    AuthCardPage(maxWidth = 400.dp) {
        AuthBrandHeader(subtitle = "좋아하는 스타의 순간을 한곳에 모아보세요")
        Spacer(Modifier.height(28.dp))

        AuthFieldLabel(text = "아이디 또는 이메일")
        EditText("아이디 또는 이메일을 입력해주세요") { username = it }
        Spacer(Modifier.height(16.dp))

        AuthFieldLabel(text = "비밀번호")
        PasswordEditText(hint = stringResource(R.string.login_edit_text_password_hint)) {
            password = it
        }

        if (loginState == LoginState.Failure) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StarSnapColor.dangerSoft, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "아이디 또는 비밀번호를 다시 확인해 주세요.",
                    style = StarSnapTypography.caption.copy(color = StarSnapColor.danger),
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        AuthPrimaryButton(
            text = if (loginState == LoginState.Loading) "로그인 중..." else stringResource(R.string.login),
            enabled = canLogin,
            onClick = { loginViewModel.login(username.trim(), password) },
        )

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                text = "아직 계정이 없으신가요?",
                style = StarSnapTypography.label.copy(color = StarSnapColor.textSubtle),
            )
            TextButton(
                onClick = moveSignupNavigation,
                modifier = Modifier.height(44.dp),
            ) {
                Text(
                    text = stringResource(R.string.signup),
                    style = StarSnapTypography.label.copy(fontWeight = FontWeight.SemiBold),
                    color = StarSnapColor.text,
                )
            }
            Spacer(Modifier.weight(1f))
        }
    }
}
