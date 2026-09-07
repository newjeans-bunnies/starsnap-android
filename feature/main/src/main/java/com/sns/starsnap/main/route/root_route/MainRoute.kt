package com.sns.starsnap.main.route.root_route

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sns.starsnap.main.route.SettingRoute
import com.sns.starsnap.main.route.bottom_nav_route.UploadRoute
import com.sns.starsnap.main.ui.screen.main.MainScreen
import com.sns.starsnap.main.ui.screen.main.message.MessageChatScreen
import com.sns.starsnap.main.ui.screen.main.message.MessageListScreen
import com.sns.starsnap.main.ui.screen.main.profile.ProfileScreen
import com.sns.starsnap.main.ui.screen.main.profile.UserScreen
import com.sns.starsnap.main.ui.screen.main.snap_list.SnapScreen
import com.sns.starsnap.main.ui.screen.main.star_hub.StarGroupScreen
import com.sns.starsnap.main.ui.screen.main.star_hub.StarScreen
import com.sns.starsnap.main.utils.NavigationRoute.MAIN
import com.sns.starsnap.main.utils.NavigationRoute.MAIN_ROUTE
import com.sns.starsnap.main.utils.NavigationRoute.MESSAGE
import com.sns.starsnap.main.utils.NavigationRoute.MESSAGE_CHAT
import com.sns.starsnap.main.utils.NavigationRoute.SNAP
import com.sns.starsnap.main.utils.NavigationRoute.FIX_PROFILE
import com.sns.starsnap.main.utils.NavigationRoute.SETTING
import com.sns.starsnap.main.viewmodel.main.MessageViewModel
import com.sns.starsnap.main.viewmodel.main.SnapViewModel
import com.sns.starsnap.main.viewmodel.main.StarViewModel
import com.sns.starsnap.main.viewmodel.main.UploadViewModel
import com.sns.starsnap.main.viewmodel.main.UserViewModel
import android.net.Uri

@Composable
fun MainRoute(
    rootNavController: NavHostController,
    uploadViewModel: UploadViewModel = hiltViewModel(),
    snapViewModel: SnapViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    starViewModel: StarViewModel = hiltViewModel(),
    messageViewModel: MessageViewModel = hiltViewModel()
) {
    val mainNavController = rememberNavController()

    // 로그인 후 진입 시 채팅 공개키를 자동 등록하고 실시간 소켓을 연결한다.
    LaunchedEffect(Unit) {
        messageViewModel.start()
    }

    NavHost(
        navController = mainNavController,
        startDestination = MAIN,
        route = MAIN_ROUTE,
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
        composable(MAIN) {
            MainScreen(
                rootNavController = rootNavController,
                mainNavController = mainNavController,
                uploadViewModel = uploadViewModel,
                snapViewModel = snapViewModel,
                userViewModel = userViewModel,
                starViewModel = starViewModel
            ) {
                mainNavController.navigate(it)
            }
        }
        composable(MESSAGE) {
            MessageListScreen(
                mainNavController = mainNavController,
                messageViewModel = messageViewModel
            )
        }
        composable(MESSAGE_CHAT) {
            MessageChatScreen(
                mainNavController = mainNavController,
                messageViewModel = messageViewModel
            )
        }
        composable(SNAP) {
            SnapScreen(
                navController = mainNavController,
                viewModel = snapViewModel,
                starViewModel = starViewModel,
                userViewModel = userViewModel,
                onNavigate = { route -> mainNavController.navigate(route) }
            )
        }
        composable("star") {
            StarScreen(
                mainNavController = mainNavController,
                starViewModel = starViewModel,
                snapViewModel = snapViewModel
            )
        }
        composable("star_group") {
            StarGroupScreen(
                mainNavController = mainNavController,
                starViewModel = starViewModel,
                userViewModel = userViewModel,
                snapViewModel = snapViewModel
            )
        }
        composable("profile") {
            ProfileScreen(
                mainNavController = mainNavController,
                userViewModel = userViewModel,
                snapViewModel = snapViewModel,
                onEditProfile = { mainNavController.navigate(FIX_PROFILE) },
                onOpenSettings = { mainNavController.navigate(SETTING) },
            )
        }
        composable(
            route = "user?username={username}&imageKey={imageKey}",
            arguments = listOf(
                navArgument("username") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("imageKey") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            )
        ) { backStackEntry ->
            val username = Uri.decode(backStackEntry.arguments?.getString("username").orEmpty())
            val imageKey = Uri.decode(backStackEntry.arguments?.getString("imageKey").orEmpty())
            UserScreen(
                mainNavController = mainNavController,
                userViewModel = userViewModel,
                snapViewModel = snapViewModel,
                initialUsername = username,
                initialImageKey = imageKey,
                onOpenMessages = {
                    messageViewModel.openChatWith(it) {
                        mainNavController.navigate(MESSAGE_CHAT)
                    }
                }
            )
        }
        SettingRoute(mainNavController, userViewModel)
        composable("add_snap") {
            UploadRoute(
                mainNavController = mainNavController,
                uploadViewModel = uploadViewModel,
                starViewModel = starViewModel
            )
        }
    }
}
