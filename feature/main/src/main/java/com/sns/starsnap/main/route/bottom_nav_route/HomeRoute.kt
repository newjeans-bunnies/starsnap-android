package com.sns.starsnap.main.route.bottom_nav_route

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sns.starsnap.main.ui.screen.main.snap_list.SnapListScreen
import com.sns.starsnap.main.ui.screen.main.snap_list.SnapScreen
import com.sns.starsnap.main.utils.NavigationRoute
import com.sns.starsnap.main.viewmodel.main.SnapViewModel
import com.sns.starsnap.main.viewmodel.main.StarViewModel
import com.sns.starsnap.main.viewmodel.main.UserViewModel

@Composable
fun HomeRoute(
    snapViewModel: SnapViewModel,
    starViewModel: StarViewModel,
    userViewModel: UserViewModel,
    onNavigate: (String) -> Unit
) {
    val homeNavController = rememberNavController()
    NavHost(
        navController = homeNavController,
        startDestination = NavigationRoute.SNAP_LIST,
        route = NavigationRoute.HOME_ROUTE
    ) {
        composable(NavigationRoute.SNAP_LIST) {
            SnapListScreen(homeNavController, snapViewModel)
        }
        composable(NavigationRoute.SNAP) {
            SnapScreen(homeNavController, snapViewModel, starViewModel, userViewModel, onNavigate)
        }
    }
}