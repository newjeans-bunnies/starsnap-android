package com.sns.starsnap.main.utils

import com.sns.starsnap.designsystem.R
import com.sns.starsnap.main.utils.NavigationRoute.HOME_ROUTE
import com.sns.starsnap.main.utils.NavigationRoute.SEARCH_ROUTE
import com.sns.starsnap.main.utils.NavigationRoute.STAR_HUB_ROUTE
import com.sns.starsnap.main.utils.NavigationRoute.USER_ROUTE
import com.sns.starsnap.main.utils.NavigationRoute.UPLOAD_ROUTE

sealed class BottomNavItem(
    val label: String,
    val iconRes: Int,
    val route: String,
) {
//
//    companion object {
//        private const val HOME_ROUTE = "HOME"
//        private const val USER_ROUTE = "USER"
//        private const val ADD_SNAP_ROUTE = "ADD_SNAP"
//        private const val STAR_HUB_ROUTE = "STAR_HUB"
//    }

    data object Home : BottomNavItem(
        label = "Home",
        iconRes = R.drawable.home_icon,
        route = HOME_ROUTE,
    )

    data object AddSnap : BottomNavItem(
        label = "AddSnap",
        iconRes = R.drawable.add_icon,
        route = UPLOAD_ROUTE
    )

    data object User : BottomNavItem(
        label = "User",
        iconRes = R.drawable.user_icon,
        route = USER_ROUTE,
    )

    data object Star : BottomNavItem(
        label = "StarHub",
        iconRes = R.drawable.star_icon,
        route = STAR_HUB_ROUTE,
    )

    data object Search : BottomNavItem(
        label = "Search",
        iconRes = R.drawable.search_icon,
        route = SEARCH_ROUTE,
    )
}