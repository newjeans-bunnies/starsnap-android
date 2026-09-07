package com.sns.starsnap.main.ui.screen.main.setting

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sns.starsnap.main.ui.component.DataLoadErrorCard
import com.sns.starsnap.main.ui.component.skeleton.loadingSemantics
import com.sns.starsnap.main.utils.NavigationRoute
import com.sns.starsnap.main.viewmodel.main.UserViewModel

@Composable
fun AuthSettingScreen(
    navController: NavController,
    userViewModel: UserViewModel,
) {
    LaunchedEffect(Unit) {
        Log.d("화면", "AuthSettingScreen")
        userViewModel.getUserData()
    }

    val userData by userViewModel.userData.collectAsStateWithLifecycle()
    val userDataLoading by userViewModel.userDataLoading.collectAsStateWithLifecycle()
    val userDataError by userViewModel.userDataError.collectAsStateWithLifecycle()
    val showUserDataSkeleton = userDataLoading && userData.username.isBlank()
    val userDataUnavailable = !userDataLoading &&
        !userDataError.isNullOrBlank() &&
        userData.username.isBlank()
    val loginType = when {
        userData.authority.contains("GOOGLE", ignoreCase = true) -> "Google"
        userData.authority.contains("APPLE", ignoreCase = true) -> "Apple"
        else -> "이메일"
    }

    SettingPageScaffold(
        title = "계정",
        onBack = { navController.popBackStack() },
    ) {
        if (!userDataError.isNullOrBlank()) {
            item {
                DataLoadErrorCard(
                    title = "계정 정보를 불러오지 못했어요.",
                    description = "네트워크 상태를 확인한 뒤 다시 시도해 주세요.",
                    onRetry = userViewModel::getUserData,
                )
            }
        }
        item {
            SettingSection(title = "계정 정보") {
                if (showUserDataSkeleton) {
                    repeat(3) { index ->
                        SettingRowSkeleton(
                            modifier = if (index == 0) {
                                Modifier.loadingSemantics("계정 정보")
                            } else {
                                Modifier
                            },
                        )
                        if (index < 2) SettingDivider()
                    }
                } else if (userDataUnavailable) {
                    SettingInfoBanner(
                        text = "계정 정보를 불러오면 프로필, 이메일과 로그인 방식을 확인할 수 있어요.",
                    )
                } else {
                    SettingRow(
                        label = "프로필 정보",
                        value = userData.username.ifBlank { "사용자" },
                        onClick = { navController.navigate(NavigationRoute.FIX_PROFILE) },
                    )
                    SettingDivider()
                    SettingRow(
                        label = "이메일",
                        value = userData.email.ifBlank { "-" },
                    )
                    SettingDivider()
                    SettingRow(label = "로그인 방식", value = loginType)
                }
            }
        }
        item {
            SettingSection(title = "보안") {
                SettingRow(
                    label = "비밀번호 변경",
                    description = "비밀번호 변경은 StarSnap 웹 계정 설정에서 이용할 수 있어요.",
                )
            }
        }
        item {
            SettingInfoBanner(
                text = "계정 이메일은 로그인과 중요 보안 안내에 사용됩니다.",
            )
        }
    }
}
