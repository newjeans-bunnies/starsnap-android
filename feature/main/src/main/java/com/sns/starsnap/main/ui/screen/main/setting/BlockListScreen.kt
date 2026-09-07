package com.sns.starsnap.main.ui.screen.main.setting

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController

@Composable
fun BlockListScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        Log.d("화면", "BlockListScreen")
    }

    SettingPageScaffold(
        title = "차단 사용자 관리",
        onBack = { navController.popBackStack() },
    ) {
        item {
            SettingEmptyState(
                title = "차단한 사용자가 없습니다",
                description = "차단한 사용자는 검색과 메시지에서 서로 표시되지 않아요.",
            )
        }
        item {
            SettingInfoBanner(
                text = "사용자 프로필의 더보기 메뉴에서 차단하거나 차단을 해제할 수 있어요.",
            )
        }
    }
}
