package com.photo.starsnap.main.ui.screen.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.compose.animation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.main.ui.component.BottomNavigation
import com.photo.starsnap.main.ui.component.StarSnapAppBar
import com.photo.starsnap.main.route.bottom_nav_route.HomeRoute
import com.photo.starsnap.main.route.bottom_nav_route.StarHubRoute
import com.photo.starsnap.main.ui.screen.main.profile.ProfileScreen
import com.photo.starsnap.main.ui.screen.main.search.SearchScreen
import com.photo.starsnap.main.ui.screen.main.setting.AlarmSettingViewModel
import com.photo.starsnap.main.ui.screen.main.setting.SyncNotificationPermissionOnResume
import com.photo.starsnap.main.utils.BottomNavItem
import com.photo.starsnap.main.utils.NavigationRoute.FIX_PROFILE
import com.photo.starsnap.main.utils.NavigationRoute.HOME_ROUTE
import com.photo.starsnap.main.utils.NavigationRoute.SETTING
import com.photo.starsnap.main.viewmodel.main.SnapViewModel
import com.photo.starsnap.main.viewmodel.main.StarViewModel
import com.photo.starsnap.main.viewmodel.main.UploadViewModel
import com.photo.starsnap.main.viewmodel.main.UserViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    rootNavController: NavHostController,
    mainNavController: NavHostController,
    uploadViewModel: UploadViewModel,
    snapViewModel: SnapViewModel,
    userViewModel: UserViewModel,
    starViewModel: StarViewModel,
    alarmSettingViewModel: AlarmSettingViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit,
) {
    val context = LocalContext.current
    val alarmUiState by alarmSettingViewModel.uiState.collectAsStateWithLifecycle()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = alarmSettingViewModel::onNotificationPermissionResult,
    )

    LaunchedEffect(Unit) {
        Log.d("화면", "MainScreen")
        userViewModel.getUserData()
    }

    LaunchedEffect(alarmUiState.preferenceConfigured) {
        if (!alarmUiState.preferenceConfigured) {
            val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            if (permissionGranted) {
                alarmSettingViewModel.onNotificationPermissionResult(true)
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    SyncNotificationPermissionOnResume(
        viewModel = alarmSettingViewModel,
        preferenceConfigured = alarmUiState.preferenceConfigured,
    )

    val bottomNavItems = remember {
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Search,
            BottomNavItem.AddSnap,
            BottomNavItem.Star,
            BottomNavItem.User
        )
    }


    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val userData by userViewModel.userData.collectAsState()
    val profileImageKey = userData.profileImageUrl.takeUnless {
        it.isBlank() || it.equals("null", ignoreCase = true)
    }

    LaunchedEffect(currentRoute) {
        Log.d("현재 화면", "MainScreen: $currentRoute")
    }

    Scaffold(
        containerColor = StarSnapColor.canvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            StarSnapAppBar(
                profileImageKey = profileImageKey,
                onHome = {
                    if (currentRoute != BottomNavItem.Home.route) {
                        navController.navigate(BottomNavItem.Home.route)
                    }
                },
                onNotifications = {},
                onProfile = {
                    if (currentRoute != BottomNavItem.User.route) {
                        navController.navigate(BottomNavItem.User.route)
                    }
                },
            )
        },
        bottomBar = {
            BottomNavigation(navController, bottomNavItems, onNavigate = onNavigate)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = HOME_ROUTE,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeRoute(
                    snapViewModel = snapViewModel,
                    starViewModel = starViewModel,
                    userViewModel = userViewModel,
                    onNavigate = onNavigate
                )
            }
            composable(BottomNavItem.User.route) {
                ProfileScreen(
                    mainNavController = navController,
                    userViewModel = userViewModel,
                    snapViewModel = snapViewModel,
                    onEditProfile = { mainNavController.navigate(FIX_PROFILE) },
                    onOpenSettings = { mainNavController.navigate(SETTING) },
                    onOpenSnap = { onNavigate(it) }
                )
            }
            composable(BottomNavItem.Star.route) {
                StarHubRoute(rootNavController, starViewModel, onNavigate)
            }
            composable(BottomNavItem.Search.route) {
                SearchScreen(
                    mainNavController = navController,
                    snapViewModel = snapViewModel
                )
            }
        }
    }
}
