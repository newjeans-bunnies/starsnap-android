package com.sns.starsnap.main.ui.screen.main.setting

import androidx.lifecycle.ViewModel
import com.sns.starsnap.network.notification.FcmTokenSyncer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AlarmSettingUiState(
    val pushNotificationsEnabled: Boolean,
    val preferenceConfigured: Boolean,
)

@HiltViewModel
class AlarmSettingViewModel @Inject constructor(
    private val fcmTokenSyncer: FcmTokenSyncer,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AlarmSettingUiState(
            pushNotificationsEnabled = fcmTokenSyncer.arePushNotificationsEnabled(),
            preferenceConfigured = fcmTokenSyncer.hasPushNotificationsPreference(),
        ),
    )
    val uiState: StateFlow<AlarmSettingUiState> = _uiState.asStateFlow()

    fun setPushNotificationsEnabled(enabled: Boolean) {
        fcmTokenSyncer.setPushNotificationsEnabled(enabled)
        updateUiState()
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        fcmTokenSyncer.onNotificationPermissionResult(granted)
        updateUiState()
    }

    fun reconcileNotificationPermission(granted: Boolean) {
        fcmTokenSyncer.reconcileNotificationPermission(granted)
        updateUiState()
    }

    private fun updateUiState() {
        _uiState.value = AlarmSettingUiState(
            pushNotificationsEnabled = fcmTokenSyncer.arePushNotificationsEnabled(),
            preferenceConfigured = fcmTokenSyncer.hasPushNotificationsPreference(),
        )
    }
}
