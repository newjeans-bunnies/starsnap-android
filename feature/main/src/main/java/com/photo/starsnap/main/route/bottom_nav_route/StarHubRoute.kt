package com.photo.starsnap.main.route.bottom_nav_route

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.photo.starsnap.designsystem.CustomColor
import com.photo.starsnap.designsystem.CustomColor.container
import com.photo.starsnap.designsystem.R
import com.photo.starsnap.designsystem.text.CustomTextStyle.TitleSmall
import com.photo.starsnap.designsystem.text.CustomTextStyle.title9
import com.photo.starsnap.main.ui.component.TextEditHint
import com.photo.starsnap.main.ui.screen.main.star_hub.StarGroupListScreen
import com.photo.starsnap.main.ui.screen.main.star_hub.StarListScreen
import com.photo.starsnap.main.utils.NavigationRoute
import com.photo.starsnap.main.utils.clickableSingle
import com.photo.starsnap.main.viewmodel.main.StarViewModel

@Composable
fun StarHubRoute(
    rootNavController: NavController,
    starViewModel: StarViewModel,
    onNavigate: (String) -> Unit
) {
    val searchHubNavController = rememberNavController()

    // false면 star, true면 stargroup
    var state by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Log.d("화면", "SearchHubScreen")
    }

    LaunchedEffect(state) {
        if(state)
            searchHubNavController.navigate(NavigationRoute.SEARCH_STAR_GROUP)
        else
            searchHubNavController.navigate(NavigationRoute.SEARCH_STAR)
    }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SearchTopBar(state) {
                if(state)
                    starViewModel.setSearchStarGroupName(it)
                else
                    starViewModel.setSearchStarName(it)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            SelectText {
                state = it
            }
            Spacer(modifier = Modifier.height(10.dp))
            NavHost(
                navController = searchHubNavController,
                startDestination = NavigationRoute.SEARCH_STAR_GROUP,
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
                composable(NavigationRoute.SEARCH_STAR) {
                    StarListScreen(rootNavController, starViewModel, onNavigate)
                }

                composable(NavigationRoute.SEARCH_STAR_GROUP) {
                    StarGroupListScreen(rootNavController, starViewModel, onNavigate)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SearchTopBar(state: Boolean = false, searchText: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val hintText = if (state) "StarGroup 검색" else "Star 검색"
    Column(
        modifier = Modifier
            .padding(horizontal = 22.dp)
            .height(65.dp)
            .fillMaxWidth()
            .background(color = Color.White)

    ) {
        Spacer(modifier = Modifier.height(18.dp))
        BasicTextField(
            value = text,
            textStyle = title9,
            onValueChange = { input ->
                if (100 > input.length) {
                    text = input
                    searchText(input)
                }
            },
            modifier = Modifier
                .height(55.dp)
                .background(container, shape = RoundedCornerShape(size = 8.dp)),
            singleLine = true,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .padding(start = 0.dp, end = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(13.dp))
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = ImageVector.vectorResource(R.drawable.search_icon),
                        contentDescription = "search_icon"
                    )
                    Spacer(Modifier.width(9.dp))
                    Box(
                        Modifier.weight(1F)
                    ) {
                        innerTextField()
                        if (text.isEmpty()) {
                            Crossfade(
                                targetState = hintText,
                                label = "hintChange"
                            ) { animatedHint ->
                                TextEditHint(animatedHint)
                            }
                        }
                    }
                }
            })
        Column(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
                .background(CustomColor.container)
        ) { }
        Spacer(modifier = Modifier.height(9.dp))
    }
}

@Composable
fun SelectText(changeState: (Boolean) -> Unit) {
    var tempState by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .height(30.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clickableSingle {
                    changeState(false)
                    tempState = false
                }
                .weight(1F), contentAlignment = Alignment.Center) {
            Text(
                text = "Star",
                textAlign = TextAlign.Center,
                style = TitleSmall,
                color = if (!tempState) CustomColor.light_black else CustomColor.light_gray
            )
        }
        Box(
            modifier = Modifier
                .clickableSingle {
                    changeState(true)
                    tempState = true
                }
                .weight(1F), contentAlignment = Alignment.Center) {
            Text(
                text = "StarGroup",
                textAlign = TextAlign.Center,
                style = TitleSmall,
                color = if (tempState) CustomColor.light_black else CustomColor.light_gray
            )
        }
    }
}
