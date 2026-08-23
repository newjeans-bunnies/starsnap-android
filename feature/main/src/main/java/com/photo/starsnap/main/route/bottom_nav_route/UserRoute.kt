package com.photo.starsnap.main.route.bottom_nav_route

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.photo.starsnap.main.ui.screen.main.profile.ProfileScreen
import com.photo.starsnap.main.ui.screen.main.setting.SettingScreen
import com.photo.starsnap.main.utils.NavigationRoute
import com.photo.starsnap.main.viewmodel.main.UserViewModel

@Composable
fun UserRoute(navController: NavController, userViewModel: UserViewModel) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = NavigationRoute.PROFILE,
        route = NavigationRoute.USER_ROUTE
    ) {
        composable(NavigationRoute.PROFILE) {
            ProfileScreen(navController, userViewModel)
        }
        composable(NavigationRoute.SETTING_ROUTE) {
            SettingScreen(navController, userViewModel)
        }
    }
}