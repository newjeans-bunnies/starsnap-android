package com.sns.starsnap.main.ui.screen.auth

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.sns.starsnap.main.ui.screen.auth.signup.ConsentSignupScreen
import com.sns.starsnap.main.ui.screen.auth.signup.EmailSignupScreen
import com.sns.starsnap.main.ui.screen.auth.signup.LoadingSignupScreen
import com.sns.starsnap.main.ui.screen.auth.signup.PasswordSignupScreen
import com.sns.starsnap.main.ui.screen.auth.signup.UserNameSignupScreen
import com.sns.starsnap.main.ui.screen.auth.signup.VerifySignupScreen
import com.sns.starsnap.main.utils.NavigationRoute
import com.sns.starsnap.main.viewmodel.auth.SignupViewModel

@Composable
fun SignupScreen(
    viewModel: SignupViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit
) {

    LaunchedEffect(Unit) {
        Log.d("화면", "SignupScreen")
    }

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = NavigationRoute.SIGNUP) {
        navigation(startDestination = NavigationRoute.SIGNUP_USERNAME, route = NavigationRoute.SIGNUP) {
            // 닉네임 입력 화면
            composable(NavigationRoute.SIGNUP_USERNAME) {
                UserNameSignupScreen(viewModel, navController, onNavigateToLogin)
            }
            // 비밀번호 입력 화면
            composable(NavigationRoute.SIGNUP_PASSWORD) {
                PasswordSignupScreen(viewModel, navController)
            }
            // 이메일 입력 화면
            composable(NavigationRoute.SIGNUP_EMAIL) {
                EmailSignupScreen(viewModel, navController)
            }
            // 이메일 인증 화면
            composable(NavigationRoute.SIGNUP_VERIFY) {
                VerifySignupScreen(viewModel, navController)
            }
            // 약관동의 화면
            composable(NavigationRoute.SIGNUP_CONSENT) {
                ConsentSignupScreen(viewModel, navController)
            }
            // 마지막 화면
            composable(NavigationRoute.SIGNUP_LOADING) {
                LoadingSignupScreen(viewModel, navController, onNavigateToLogin)
            }
        }
    }
}