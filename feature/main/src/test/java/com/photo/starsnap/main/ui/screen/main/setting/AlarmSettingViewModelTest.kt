package com.photo.starsnap.main.ui.screen.main.setting

import com.photo.starsnap.network.notification.FcmTokenSyncer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmSettingViewModelTest {
    @Test
    fun `toggle updates durable push preference through syncer`() {
        val syncer = FakeFcmTokenSyncer(initiallyEnabled = true)
        val viewModel = AlarmSettingViewModel(syncer)

        assertTrue(viewModel.uiState.value.pushNotificationsEnabled)

        viewModel.setPushNotificationsEnabled(false)

        assertFalse(viewModel.uiState.value.pushNotificationsEnabled)
        assertTrue(viewModel.uiState.value.preferenceConfigured)
        assertFalse(syncer.enabled)
    }

    private class FakeFcmTokenSyncer(
        initiallyEnabled: Boolean,
    ) : FcmTokenSyncer {
        var enabled = initiallyEnabled
        var configured = true

        override fun onAuthenticated() = Unit

        override fun syncToken(token: String) = Unit

        override fun arePushNotificationsEnabled(): Boolean = enabled

        override fun hasPushNotificationsPreference(): Boolean = configured

        override fun setPushNotificationsEnabled(enabled: Boolean) {
            this.enabled = enabled
            configured = true
        }

        override fun onNotificationPermissionResult(granted: Boolean) {
            enabled = granted
            configured = true
        }

        override fun reconcileNotificationPermission(granted: Boolean) = Unit

        override fun onSignedOut() = Unit
    }
}
