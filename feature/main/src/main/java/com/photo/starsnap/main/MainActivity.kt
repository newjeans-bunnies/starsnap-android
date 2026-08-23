package com.photo.starsnap.main

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.photo.starsnap.datastore.SessionManager
import com.photo.starsnap.datastore.ThemePreferences
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.StarSnapTheme
import com.photo.starsnap.main.route.root_route.AuthRoute
import com.photo.starsnap.main.route.root_route.MainRoute
import com.photo.starsnap.main.utils.NavigationRoute
import com.photo.starsnap.main.viewmodel.auth.LoginViewModel
import com.photo.starsnap.main.viewmodel.main.ThemeViewModel
import com.photo.starsnap.main.viewmodel.state.AutoLoginState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun attachBaseContext(newBase: Context) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            AppCompatDelegate.setDefaultNightMode(
                ThemePreferences.getBootstrapDarkMode(newBase).toNightMode()
            )
        }
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashscreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        val loginViewModel: LoginViewModel by viewModels()
        val themeViewModel: ThemeViewModel by viewModels()
        setContent {
            val autoLoginState by loginViewModel.autoLoginState.observeAsState()
            val themeUiState by themeViewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = themeUiState.darkModePreference ?: isSystemInDarkTheme()
            val requestedNightMode = themeUiState.darkModePreference.toNightMode()

            LaunchedEffect(Unit) {
                loginViewModel.reissueToken()
            }

            LaunchedEffect(requestedNightMode) {
                if (AppCompatDelegate.getDefaultNightMode() != requestedNightMode) {
                    AppCompatDelegate.setDefaultNightMode(requestedNightMode)
                }
            }

            splashscreen.setKeepOnScreenCondition {
                !themeUiState.isLoaded ||
                    autoLoginState == AutoLoginState.Idle ||
                    autoLoginState == AutoLoginState.Loading
            }

            val startDestination = when (autoLoginState) {
                AutoLoginState.Success -> NavigationRoute.MAIN_ROUTE
                AutoLoginState.Failure -> NavigationRoute.AUTH_ROUTE
                else -> null
            }

            if (themeUiState.isLoaded && startDestination != null) {
                StarSnapTheme(darkTheme = darkTheme) {
                    val systemBarColor = StarSnapColor.canvas
                    SideEffect {
                        updateSystemBars(darkTheme, systemBarColor)
                    }

                    val navController = rememberNavController()

                    // 토큰 재발급(refresh) 시도가 실패하면 AuthAuthenticator가 이 이벤트를 발생시킨다.
                    // 세션이 끊어졌으므로 어느 화면에 있든 로그인 화면으로 강제 이동(자동 로그아웃)시킨다.
                    LaunchedEffect(navController) {
                        sessionManager.sessionExpiredEvents.collect {
                            navController.navigate(NavigationRoute.AUTH_ROUTE) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable(NavigationRoute.AUTH_ROUTE) {
                            AuthRoute(navController)
                        }
                        composable(NavigationRoute.MAIN_ROUTE) {
                            MainRoute(navController)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun updateSystemBars(darkTheme: Boolean, color: Color) {
        window.statusBarColor = color.toArgb()
        window.navigationBarColor = color.toArgb()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

private fun Boolean?.toNightMode(): Int = when (this) {
    true -> AppCompatDelegate.MODE_NIGHT_YES
    false -> AppCompatDelegate.MODE_NIGHT_NO
    null -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
}
