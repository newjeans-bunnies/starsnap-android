package com.sns.starsnap.main.route.root_route

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sns.starsnap.main.ui.screen.auth.LoginScreen
import com.sns.starsnap.main.ui.screen.auth.PasswordResetScreen
import com.sns.starsnap.main.ui.screen.auth.SignupScreen
import com.sns.starsnap.main.utils.NavigationRoute.AUTH_ROUTE
import com.sns.starsnap.main.utils.NavigationRoute.FORGOT_PASSWORD
import com.sns.starsnap.main.utils.NavigationRoute.LOGIN
import com.sns.starsnap.main.utils.NavigationRoute.MAIN_ROUTE
import com.sns.starsnap.main.utils.NavigationRoute.SIGNUP

@Composable
fun AuthRoute(navController: NavHostController) {
    val authController = rememberNavController()
    NavHost(navController = authController, startDestination = LOGIN, route = AUTH_ROUTE) {
        /*
        * https://tomas-repcik.medium.com/jetpack-compose-and-screen-transition-animations-b361fc8164cc
        * Transition 애니메이션 참고
        * */
        composable(
            LOGIN, // 처음 페이지
            // 열때 효과
            enterTransition = {
                return@composable slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(400)
                )
            },
            // 닫을때 효과
            exitTransition = {
                return@composable slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(400)
                )
            }
        ) {
            LoginScreen(moveSignupNavigation = {
                authController.navigate(SIGNUP)  // authController 사용 (SIGNUP은 authController에 등록됨)
            }, moveForgotPasswordNavigation = {
                authController.navigate(FORGOT_PASSWORD)
            }, moveMainNavigation = {
                navController.navigate(MAIN_ROUTE) {  // 루트 이동은 navController 유지
                    popUpTo(AUTH_ROUTE) { inclusive = true }
                }
            })
        }
        composable(FORGOT_PASSWORD) {
            PasswordResetScreen(onNavigateToLogin = { authController.popBackStack() })
        }
        composable(SIGNUP) {
            SignupScreen(onNavigateToLogin = {
                Log.d("signup", "뒤로가기")
                authController.navigate(LOGIN) {  // authController 사용 (LOGIN도 authController에 등록됨)
                    popUpTo(LOGIN) { inclusive = true }
                }
            })
        }
    }
}
