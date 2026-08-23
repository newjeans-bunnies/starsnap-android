package com.photo.starsnap.main.ui.screen.main.setting

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.photo.starsnap.main.utils.NavigationRoute
import com.photo.starsnap.main.viewmodel.main.UserViewModel

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
    val loginType = when {
        userData.authority.contains("GOOGLE", ignoreCase = true) -> "Google"
        userData.authority.contains("APPLE", ignoreCase = true) -> "Apple"
        else -> "이메일"
    }

    SettingPageScaffold(
        title = "계정",
        onBack = { navController.popBackStack() },
    ) {
        item {
            SettingSection(title = "계정 정보") {
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
