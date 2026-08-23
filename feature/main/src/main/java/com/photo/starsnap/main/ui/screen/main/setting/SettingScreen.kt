package com.photo.starsnap.main.ui.screen.main.setting

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.StarSnapTypography
import com.photo.starsnap.main.utils.NavigationRoute
import com.photo.starsnap.main.viewmodel.main.ThemeViewModel
import com.photo.starsnap.main.viewmodel.main.UserViewModel
import com.photo.starsnap.main.viewmodel.auth.LoginViewModel

private enum class SettingCategory(val label: String) {
    Account("계정"),
    Notification("알림"),
    Privacy("개인정보 보호"),
    Display("화면"),
    Report("신고 내역"),
    Support("고객센터"),
}

@Composable
fun SettingScreen(
    mainNavController: NavController,
    userViewModel: UserViewModel,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel(),
    alarmSettingViewModel: AlarmSettingViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        Log.d("화면", "SettingScreen")
        userViewModel.getUserData()
    }

    val userData by userViewModel.userData.collectAsStateWithLifecycle()
    val themeUiState by themeViewModel.uiState.collectAsStateWithLifecycle()
    val alarmUiState by alarmSettingViewModel.uiState.collectAsStateWithLifecycle()
    val darkModeEnabled = themeUiState.darkModePreference ?: isSystemInDarkTheme()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = alarmSettingViewModel::onNotificationPermissionResult,
    )

    SyncNotificationPermissionOnResume(
        viewModel = alarmSettingViewModel,
        preferenceConfigured = alarmUiState.preferenceConfigured,
    )

    fun updatePushNotifications(enabled: Boolean) {
        if (!enabled) {
            alarmSettingViewModel.setPushNotificationsEnabled(false)
            return
        }

        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        if (permissionGranted) {
            alarmSettingViewModel.setPushNotificationsEnabled(true)
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var activeLabel by rememberSaveable { mutableStateOf(SettingCategory.Account.label) }
    val activeCategory = SettingCategory.entries.firstOrNull { it.label == activeLabel }
        ?: SettingCategory.Account

    var searchExposureEnabled by rememberSaveable { mutableStateOf(true) }
    var commentEnabled by rememberSaveable { mutableStateOf(true) }
    var reduceMotionEnabled by rememberSaveable { mutableStateOf(false) }
    var autoPlayEnabled by rememberSaveable { mutableStateOf(true) }
    var faqExpanded by rememberSaveable { mutableStateOf(false) }
    var inquiryExpanded by rememberSaveable { mutableStateOf(false) }

    SettingPageScaffold(
        title = "설정",
        onBack = { mainNavController.popBackStack() },
    ) {
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    count = SettingCategory.entries.size,
                    key = { SettingCategory.entries[it].label },
                ) { index ->
                    val category = SettingCategory.entries[index]
                    val isSelected = category == activeCategory
                    Surface(
                        onClick = { activeLabel = category.label },
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .semantics {
                                selected = isSelected
                            },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        color = if (isSelected) StarSnapColor.brandSoft else StarSnapColor.surface,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) StarSnapColor.brandActive else StarSnapColor.border,
                        ),
                    ) {
                        Text(
                            text = category.label,
                            style = StarSnapTypography.label.copy(
                                color = StarSnapColor.text,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }
            }
        }

        when (activeCategory) {
            SettingCategory.Account -> {
                item {
                    SettingSection(title = "계정") {
                        SettingRow(
                            label = "프로필 정보",
                            value = userData.username.ifBlank { "사용자" },
                            onClick = { mainNavController.navigate(NavigationRoute.FIX_PROFILE) },
                        )
                        SettingDivider()
                        SettingRow(
                            label = "이메일",
                            value = userData.email.ifBlank { "-" },
                        )
                        SettingDivider()
                        SettingRow(
                            label = "소셜 계정",
                            onClick = { mainNavController.navigate(NavigationRoute.AUTH_SETTING) },
                        )
                        SettingDivider()
                        SettingRow(
                            label = "로그아웃",
                            onClick = loginViewModel::logout,
                        )
                    }
                }
                item {
                    SettingSection(title = "환경설정") {
                        SettingRow(
                            label = "푸시 알림",
                            value = if (alarmUiState.pushNotificationsEnabled) "켜짐" else "꺼짐",
                            onClick = { activeLabel = SettingCategory.Notification.label },
                        )
                        SettingDivider()
                        SettingSwitchRow(
                            label = "다크 모드",
                            checked = darkModeEnabled,
                            onCheckedChange = themeViewModel::setDarkMode,
                        )
                        SettingDivider()
                        SettingRow(label = "언어", value = "한국어")
                    }
                }
                item {
                    SettingSection(title = "보관함") {
                        SettingRow(
                            label = "저장한 스냅",
                            onClick = { mainNavController.navigate(NavigationRoute.SAVE_LIST) },
                        )
                    }
                }
            }

            SettingCategory.Notification -> {
                item {
                    SettingSection(title = "푸시 알림") {
                        SettingSwitchRow(
                            label = "푸시 알림 받기",
                            description = "친구 요청·수락과 새 메시지 알림을 받습니다.",
                            checked = alarmUiState.pushNotificationsEnabled,
                            onCheckedChange = ::updatePushNotifications,
                        )
                        if (!alarmUiState.pushNotificationsEnabled) {
                            SettingDivider()
                            SettingInfoBanner(
                                text = "푸시 알림이 꺼져 있어 이 기기로 알림을 보내지 않습니다.",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
                item {
                    SettingSection(title = "기기 설정") {
                        SettingRow(
                            label = "시스템 알림 설정",
                            description = "소리, 배너와 잠금 화면 표시를 설정합니다.",
                            onClick = {
                                context.startActivity(
                                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    },
                                )
                            },
                        )
                    }
                }
            }

            SettingCategory.Privacy -> {
                item {
                    SettingSection(title = "공개 범위") {
                        SettingSwitchRow(
                            label = "비공개 계정",
                            description = "친구가 아닌 사용자는 내 스냅을 볼 수 없어요.",
                            checked = userData.isPrivate,
                            onCheckedChange = userViewModel::changeAccountPrivacy,
                            enabled = userData.username.isNotBlank(),
                        )
                        SettingDivider()
                        SettingSwitchRow(
                            label = "검색 노출 허용",
                            checked = searchExposureEnabled,
                            onCheckedChange = { searchExposureEnabled = it },
                        )
                        SettingDivider()
                        SettingSwitchRow(
                            label = "댓글 허용",
                            checked = commentEnabled,
                            onCheckedChange = { commentEnabled = it },
                        )
                    }
                }
                item {
                    SettingSection(title = "데이터 및 보안") {
                        SettingRow(
                            label = "차단 사용자 관리",
                            onClick = { mainNavController.navigate(NavigationRoute.BLOCK_LIST) },
                        )
                        SettingDivider()
                        SettingRow(label = "활동 상태 표시", value = "친구에게만")
                        SettingDivider()
                        SettingRow(
                            label = "개인정보 다운로드",
                            description = "데이터 요청은 웹 고객센터에서 진행할 수 있어요.",
                        )
                    }
                }
            }

            SettingCategory.Display -> {
                item {
                    SettingSection(title = "표시 옵션") {
                        SettingSwitchRow(
                            label = "다크 모드",
                            checked = darkModeEnabled,
                            onCheckedChange = themeViewModel::setDarkMode,
                        )
                        SettingDivider()
                        SettingSwitchRow(
                            label = "동작 줄이기",
                            checked = reduceMotionEnabled,
                            onCheckedChange = { reduceMotionEnabled = it },
                        )
                        SettingDivider()
                        SettingRow(label = "글자 크기", value = "보통")
                    }
                }
                item {
                    SettingSection(title = "재생 설정") {
                        SettingSwitchRow(
                            label = "영상 자동 재생",
                            checked = autoPlayEnabled,
                            onCheckedChange = { autoPlayEnabled = it },
                        )
                        SettingDivider()
                        SettingRow(label = "이미지 품질", value = "고화질")
                    }
                }
            }

            SettingCategory.Report -> {
                item {
                    SettingEmptyState(
                        title = "신고 내역이 없습니다",
                        description = "접수한 신고의 처리 상태가 여기에 표시됩니다.",
                    )
                }
                item {
                    SettingSection(title = "관리") {
                        SettingRow(label = "총 신고 건수", value = "0건")
                        SettingDivider()
                        SettingRow(
                            label = "신고 가이드",
                            onClick = { mainNavController.navigate(NavigationRoute.REPORT_LIST) },
                        )
                    }
                }
            }

            SettingCategory.Support -> {
                item {
                    SettingSection(title = "도움말") {
                        SettingRow(
                            label = "자주 묻는 질문",
                            value = if (faqExpanded) "닫기" else "보기",
                            onClick = { faqExpanded = !faqExpanded },
                        )
                        if (faqExpanded) {
                            SettingInfoBanner(
                                text = "계정, 업로드, 친구 기능에서 문제가 생기면 앱을 최신 상태로 확인한 뒤 다시 시도해주세요.",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                backgroundColor = StarSnapColor.surfaceSubtle,
                            )
                        }
                        SettingDivider()
                        SettingRow(
                            label = "문의하기",
                            value = if (inquiryExpanded) "닫기" else "안내",
                            onClick = { inquiryExpanded = !inquiryExpanded },
                        )
                        if (inquiryExpanded) {
                            SettingInfoBanner(
                                text = "문의 접수와 답변 내역 확인은 StarSnap 웹 고객센터에서 이용할 수 있어요.",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        SettingDivider()
                        SettingRow(label = "공지사항", value = "새 소식 없음")
                    }
                }
                item {
                    SettingSection(title = "정책") {
                        SettingRow(label = "이용약관", value = "웹에서 확인")
                        SettingDivider()
                        SettingRow(label = "개인정보 처리방침", value = "웹에서 확인")
                        SettingDivider()
                        SettingRow(label = "오픈소스 라이선스", value = "앱 정보")
                    }
                }
            }
        }
    }
}
