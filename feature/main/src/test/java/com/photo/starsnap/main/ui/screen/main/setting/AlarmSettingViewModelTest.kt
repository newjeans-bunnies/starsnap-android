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
        assertFalse(syncer.enabled)
    }

    private class FakeFcmTokenSyncer(
        initiallyEnabled: Boolean,
    ) : FcmTokenSyncer {
        var enabled = initiallyEnabled

        override fun onAuthenticated() = Unit

        override fun syncToken(token: String) = Unit

        override fun arePushNotificationsEnabled(): Boolean = enabled

        override fun setPushNotificationsEnabled(enabled: Boolean) {
            this.enabled = enabled
        }

        override fun onSignedOut() = Unit
    }
}
