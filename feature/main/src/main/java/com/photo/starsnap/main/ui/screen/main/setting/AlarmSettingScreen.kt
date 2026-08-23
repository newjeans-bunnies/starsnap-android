package com.photo.starsnap.main.ui.screen.main.setting

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun AlarmSettingScreen(
    navController: NavController,
    viewModel: AlarmSettingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = viewModel::onNotificationPermissionResult,
    )

    SyncNotificationPermissionOnResume(
        viewModel = viewModel,
        preferenceConfigured = uiState.preferenceConfigured,
    )

    fun updatePushNotifications(enabled: Boolean) {
        if (!enabled) {
            viewModel.setPushNotificationsEnabled(false)
            return
        }

        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        if (permissionGranted) {
            viewModel.setPushNotificationsEnabled(true)
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    SettingPageScaffold(
        title = "알림 설정",
        onBack = { navController.popBackStack() },
    ) {
        item {
            SettingSection(title = "푸시 알림") {
                SettingSwitchRow(
                    label = "푸시 알림 받기",
                    description = "친구 요청·수락과 새 메시지 알림을 받습니다.",
                    checked = uiState.pushNotificationsEnabled,
                    onCheckedChange = ::updatePushNotifications,
                )
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
        if (!uiState.pushNotificationsEnabled) {
            item {
                SettingInfoBanner("푸시 알림이 꺼져 있어 이 기기로 알림을 보내지 않습니다.")
            }
        }
    }
}
