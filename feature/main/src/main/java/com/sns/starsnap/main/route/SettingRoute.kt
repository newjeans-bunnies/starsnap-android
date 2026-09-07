package com.sns.starsnap.main.route

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sns.starsnap.main.ui.screen.main.setting.AlarmSettingScreen
import com.sns.starsnap.main.ui.screen.main.setting.AuthSettingScreen
import com.sns.starsnap.main.ui.screen.main.setting.BlockListScreen
import com.sns.starsnap.main.ui.screen.main.setting.FixProfileScreen
import com.sns.starsnap.main.ui.screen.main.setting.ReportListScreen
import com.sns.starsnap.main.ui.screen.main.setting.SaveListScreen
import com.sns.starsnap.main.ui.screen.main.setting.SettingScreen
import com.sns.starsnap.main.utils.NavigationRoute
import com.sns.starsnap.main.viewmodel.main.UserViewModel

/** Registers the mobile settings destinations in the main navigation graph. */
fun NavGraphBuilder.SettingRoute(
    mainNavController: NavController,
    userViewModel: UserViewModel,
) {
    // "setting" is retained for compatibility with the existing profile menu.
    composable("setting") {
        SettingScreen(mainNavController, userViewModel)
    }
    composable(NavigationRoute.SETTING) {
        SettingScreen(mainNavController, userViewModel)
    }
    composable(NavigationRoute.SETTING_ROUTE) {
        SettingScreen(mainNavController, userViewModel)
    }
    composable(NavigationRoute.AUTH_SETTING) {
        AuthSettingScreen(mainNavController, userViewModel)
    }
    composable(NavigationRoute.REPORT_LIST) {
        ReportListScreen(mainNavController)
    }
    composable(NavigationRoute.BLOCK_LIST) {
        BlockListScreen(mainNavController)
    }
    composable(NavigationRoute.SAVE_LIST) {
        SaveListScreen(mainNavController)
    }
    composable(NavigationRoute.ALARM_SETTING) {
        AlarmSettingScreen(mainNavController)
    }
    composable(NavigationRoute.FIX_PROFILE) {
        FixProfileScreen(mainNavController, userViewModel)
    }
}
