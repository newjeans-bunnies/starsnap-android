package com.sns.starsnap.main.ui.screen.auth.signup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.sns.starsnap.designsystem.R
import com.sns.starsnap.designsystem.StarSnapColor
import com.sns.starsnap.designsystem.text.StarSnapTypography
import com.sns.starsnap.main.ui.screen.auth.AuthCardPage
import com.sns.starsnap.main.ui.screen.auth.AuthPrimaryButton
import com.sns.starsnap.main.ui.screen.auth.SignupStepHeader
import com.sns.starsnap.main.viewmodel.auth.SignupViewModel
import com.sns.starsnap.main.viewmodel.auth.State

private enum class SignupResultUi {
    LOADING, SUCCESS, ERROR
}

@Composable
fun LoadingSignupScreen(
    viewModel: SignupViewModel,
    @Suppress("UNUSED_PARAMETER") navController: NavController,
    onNavigateToLogin: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val loadingComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.signup_loading))
    val successComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.signup_success))
    val errorComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.signup_error))

    val targetUi = when (uiState.signupState) {
        State.LOADING -> SignupResultUi.LOADING
        State.SUCCESS -> SignupResultUi.SUCCESS
        State.ERROR, State.DEFAULT, State.INTERNET_ERROR -> SignupResultUi.ERROR
    }
    val title = when (uiState.signupState) {
        State.LOADING -> stringResource(R.string.signup_loading_screen_title)
        State.SUCCESS -> stringResource(R.string.signup_success_screen_title)
        State.INTERNET_ERROR -> stringResource(R.string.internet_error)
        State.ERROR, State.DEFAULT -> stringResource(R.string.signup_error_screen_title)
    }
    val description = when (uiState.signupState) {
        State.LOADING -> "입력한 정보를 안전하게 확인하고 있어요."
        State.SUCCESS -> "이제 로그인하고 빛나는 순간을 만나보세요."
        State.ERROR, State.DEFAULT, State.INTERNET_ERROR -> "잠시 후 다시 시도하거나 로그인 화면으로 돌아가 주세요."
    }

    AuthCardPage {
        SignupStepHeader(
            step = 5,
            label = "완료",
            title = "가입 결과를 확인해 주세요",
            description = "StarSnap 계정을 준비하는 마지막 단계예요.",
        )
        Spacer(Modifier.height(18.dp))
        Crossfade(targetState = title, label = "signupTitleTransition") { animatedTitle ->
            Text(
                text = animatedTitle,
                style = StarSnapTypography.title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = description,
            style = StarSnapTypography.bodySmall.copy(color = StarSnapColor.textSubtle),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        AnimatedContent(
            targetState = targetUi,
            transitionSpec = {
                fadeIn(tween(durationMillis = 280)) togetherWith fadeOut(tween(durationMillis = 220))
            },
            label = "signupLottieTransition",
        ) { currentUi ->
            val composition = when (currentUi) {
                SignupResultUi.LOADING -> loadingComposition
                SignupResultUi.SUCCESS -> successComposition
                SignupResultUi.ERROR -> errorComposition
            }
            val iterations = if (currentUi == SignupResultUi.LOADING) {
                LottieConstants.IterateForever
            } else {
                1
            }
            LottieAnimation(
                composition = composition,
                iterations = iterations,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            )
        }
        if (uiState.signupState != State.DEFAULT && uiState.signupState != State.LOADING) {
            AuthPrimaryButton(
                text = "로그인으로 이동",
                enabled = true,
                onClick = onNavigateToLogin,
            )
        }
    }
}
