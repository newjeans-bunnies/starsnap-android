package com.sns.starsnap.main.ui.screen.main.setting

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController

@Composable
fun ReportListScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        Log.d("화면", "ReportListScreen")
    }

    SettingPageScaffold(
        title = "신고 내역",
        onBack = { navController.popBackStack() },
    ) {
        item {
            SettingEmptyState(
                title = "신고 내역이 없습니다",
                description = "접수한 게시물, 댓글, 사용자 신고가 여기에 표시됩니다.",
            )
        }
        item {
            SettingSection(title = "신고 가이드") {
                SettingRow(
                    label = "검토 중",
                    description = "운영팀이 신고 내용과 관련 기록을 확인하는 단계예요.",
                )
                SettingDivider()
                SettingRow(
                    label = "처리 완료",
                    description = "검토가 끝나면 처리 결과를 신고 내역에서 확인할 수 있어요.",
                )
            }
        }
    }
}
