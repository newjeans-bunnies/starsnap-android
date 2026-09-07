package com.sns.starsnap.main.ui.screen.main.setting

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController

@Composable
fun SaveListScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        Log.d("화면", "SaveListScreen")
    }

    SettingPageScaffold(
        title = "저장한 스냅",
        onBack = { navController.popBackStack() },
    ) {
        item {
            SettingEmptyState(
                title = "저장한 스냅을 프로필에서 확인하세요",
                description = "프로필의 ‘저장됨’ 탭에서 저장한 스냅을 한눈에 볼 수 있어요.",
            )
        }
        item {
            SettingPrimaryButton(
                text = "프로필로 이동",
                onClick = { navController.navigate("profile") },
            )
        }
    }
}
