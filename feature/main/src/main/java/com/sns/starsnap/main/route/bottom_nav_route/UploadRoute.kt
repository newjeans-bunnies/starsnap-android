package com.sns.starsnap.main.route.bottom_nav_route

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sns.starsnap.main.ui.screen.main.upload.PickPhotoScreen
import com.sns.starsnap.main.ui.screen.main.upload.PickStarScreen
import com.sns.starsnap.main.ui.screen.main.upload.SetSnapScreen
import com.sns.starsnap.main.ui.screen.main.upload.PickStarGroupScreen
import com.sns.starsnap.main.utils.NavigationRoute
import com.sns.starsnap.main.viewmodel.main.StarViewModel
import com.sns.starsnap.main.viewmodel.main.UploadViewModel

@Composable
fun UploadRoute(
    mainNavController: NavController,
    uploadViewModel: UploadViewModel,
    starViewModel: StarViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController =navController,
        startDestination = NavigationRoute.PICK_IMAGE,
        route = NavigationRoute.UPLOAD_ROUTE,
        enterTransition = {
            fadeIn(animationSpec = tween(200))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(200))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(200))
        }
    ) {
        composable(NavigationRoute.PICK_IMAGE) {
            PickPhotoScreen(mainNavController, navController, uploadViewModel)
        }
        composable(NavigationRoute.SET_SNAP) {
            SetSnapScreen(navController, uploadViewModel)
        }
        composable(NavigationRoute.PICK_STAR) {
            PickStarScreen(navController, uploadViewModel)
        }
        composable(NavigationRoute.PICK_STAR_GROUP) {
            PickStarGroupScreen(navController, uploadViewModel)
        }
    }
}