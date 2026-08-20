package com.photo.starsnap.main.ui.screen.auth

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialRequest.Builder
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.photo.starsnap.designsystem.CustomColor
import com.photo.starsnap.designsystem.R
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.CustomTextStyle.SignupTitle
import com.photo.starsnap.designsystem.text.CustomTextStyle.title4
import com.photo.starsnap.designsystem.text.TextFont.pretendard
import com.photo.starsnap.main.ui.component.AppIcon
import com.photo.starsnap.main.ui.component.AppleLoginButton
import com.photo.starsnap.main.ui.component.EditText
import com.photo.starsnap.main.ui.component.GoogleLoginButton
import com.photo.starsnap.main.ui.component.MainButton
import com.photo.starsnap.main.ui.component.PasswordEditText
import com.photo.starsnap.main.ui.component.TextButton
import com.photo.starsnap.main.viewmodel.auth.LoginViewModel
import com.photo.starsnap.main.viewmodel.state.LoginState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    moveSignupNavigation: () -> Unit,
    moveMainNavigation: () -> Unit,
) {
    LaunchedEffect(Unit) {
        Log.d("화면", "LoginScreen")
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val googleLogin =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            // Once the account has been added, do sign in again.
            doGoogleSignIn(context, coroutineScope, loginViewModel, null)
        }
    // 로그인 버튼 활성화 여부
    var isClickable by remember { mutableStateOf(false) }

    // 로그인 정보
    var username by remember { mutableStateOf("") } // 아이디
    var password by remember { mutableStateOf("") } // 비밀번호

    isClickable = username.isNotBlank() && password.isNotBlank()

    val loginState by loginViewModel.loginState.observeAsState() // 로그인

    LaunchedEffect(loginState) {

        when (loginState) {
            LoginState.Idle -> {
                // 로그인 시도 전
            }
            LoginState.Loading -> {
                // 로그인 시도 중
            }
            LoginState.Success -> {
                // 로그인 성공
                moveMainNavigation()
            }
            LoginState.Failure -> {
                // 로그인 실패
            }

            else -> {}
        }

    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        StarSnapColor.infoSoft,
                        StarSnapColor.successSoft,
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .background(color = StarSnapColor.surface, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp, vertical = 30.dp),
        ) {
            Text(
                modifier = Modifier.align(Alignment.Start),
                text = "StarSnap에서 빛나는\n순간을 공유하세요.",
                style = SignupTitle,
            )
            Spacer(Modifier.height(28.dp))

            EditText(stringResource(R.string.login_edit_text_username_hint)) { username = it }
            Spacer(Modifier.height(15.dp))
            PasswordEditText(hint = stringResource(R.string.login_edit_text_password_hint)) {
                password = it
            }
            Spacer(Modifier.height(20.dp))
            MainButton(
                { loginViewModel.login(username, password) },
                isClickable,
                stringResource(R.string.login)
            )
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(
                    text = stringResource(R.string.login_find_username_button_text),
                    textStyle = title4,
                    onClick = { })
                Spacer(Modifier.width(18.dp))
                TextButton(
                    text = stringResource(R.string.login_find_password_button_text),
                    textStyle = title4,
                    onClick = { })
                Spacer(Modifier.width(18.dp))
                TextButton(
                    text = stringResource(R.string.signup),
                    textStyle = title4,
                    onClick = moveSignupNavigation
                )
            }
            Spacer(Modifier.height(26.dp))
            GoogleLoginButton(onClick = {
                doGoogleSignIn(
                    context,
                    coroutineScope,
                    loginViewModel,
                    null
                )
            })
            Spacer(Modifier.height(10.dp))
            AppleLoginButton {}
        }
    }

}

private fun doGoogleSignIn(
    context: Context,
    coroutineScope: CoroutineScope,
    loginViewModel: LoginViewModel,
    startAddAccountIntentLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>?,
) {
    val credentialManager = CredentialManager.create(context)

    val rawNonce = UUID.randomUUID().toString()
    val bytes = rawNonce.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    val hashedNonce = digest.fold("") { str, it ->
        str + "%02x".format(it)
    }

    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(context.getString(R.string.google_client_id))
        .setAutoSelectEnabled(false)
        .setNonce(hashedNonce)
        .build()

    val request: GetCredentialRequest = Builder()
        .addCredentialOption(googleIdOption)
        .build()

    coroutineScope.launch {
        try {
            val result = credentialManager.getCredential(
                // Use an activity-based context to avoid undefined system UI
                // launching behavior.
                context = context,
                request = request
            )
            loginViewModel.handleSignIn(result)
        } catch (e: NoCredentialException) {
            startAddAccountIntentLauncher?.launch(getAddGoogleAccountIntent())
            Log.d("SignupScreen", e.toString())
        }
    }


}

fun getAddGoogleAccountIntent(): Intent {
    val intent = Intent(Settings.ACTION_ADD_ACCOUNT)
    intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
    return intent
}
