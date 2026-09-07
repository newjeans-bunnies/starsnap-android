package com.sns.starsnap.main.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sns.starsnap.designsystem.StarSnapColor
import com.sns.starsnap.designsystem.text.StarSnapTypography
import com.sns.starsnap.main.utils.BottomNavItem
import com.sns.starsnap.main.utils.NavigationRoute.UPLOAD_ROUTE

@Composable
fun BottomNavigation(
    navHostController: NavHostController,
    bottomNavItems: List<BottomNavItem>,
    onNavigate: (String) -> Unit
) {
    val navBackStackEntry by navHostController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        color = StarSnapColor.surface,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            HorizontalDivider(color = StarSnapColor.border)
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                containerColor = StarSnapColor.surface,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == item.route
                    } == true
                    val isPrimary = item == BottomNavItem.AddSnap
                    val label = when (item) {
                        BottomNavItem.Home -> "홈"
                        BottomNavItem.Search -> "탐색"
                        BottomNavItem.AddSnap -> "업로드"
                        BottomNavItem.Star -> "스타"
                        BottomNavItem.User -> "프로필"
                    }

                    NavigationBarItem(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isPrimary) StarSnapColor.brand else Color.Transparent,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    modifier = Modifier.size(if (isPrimary) 22.dp else 21.dp),
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = null,
                                )
                                if (selected && !isPrimary) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .size(4.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(StarSnapColor.brand),
                                    )
                                }
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                style = StarSnapTypography.caption.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                maxLines = 1,
                            )
                        },
                        alwaysShowLabel = true,
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                if (item.route == UPLOAD_ROUTE) {
                                    onNavigate("add_snap")
                                } else {
                                    navHostController.navigate(item.route)
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isPrimary) {
                                StarSnapColor.onBrand
                            } else {
                                StarSnapColor.text
                            },
                            selectedTextColor = StarSnapColor.text,
                            unselectedIconColor = if (isPrimary) {
                                StarSnapColor.onBrand
                            } else {
                                StarSnapColor.textMuted
                            },
                            unselectedTextColor = StarSnapColor.textMuted,
                            indicatorColor = Color.Transparent,
                        ),
                    )
                }
            }
        }
    }
}
