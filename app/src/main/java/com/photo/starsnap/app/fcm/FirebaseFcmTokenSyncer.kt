package com.photo.starsnap.app.fcm

import com.photo.starsnap.datastore.FcmTokenStore
import com.google.firebase.messaging.FirebaseMessaging
import com.photo.starsnap.network.notification.FcmTokenSyncer
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Singleton
internal class FirebaseFcmTokenSyncer @Inject constructor(
    private val tokenStore: FcmTokenStore,
    private val workScheduler: FcmTokenWorkScheduler,
) : FcmTokenSyncer {
    private val firebaseCommandExecutor: Executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "fcm-command").apply { isDaemon = true }
    }

    override fun onAuthenticated() {
        tokenStore.setAuthenticated(true)
        val enabled = tokenStore.arePushNotificationsEnabled()
        if (!enabled) {
            tokenStore.clearPendingToken()
            tokenStore.markTokenRemovalPending()
            enqueuePendingSync()
        } else {
            tokenStore.clearTokenRemovalPending()
            enqueuePendingSync()
        }

        firebaseCommandExecutor.execute {
            val messaging = FirebaseMessaging.getInstance()
            messaging.setAutoInitEnabled(enabled)
            if (enabled) messaging.register() else messaging.unregister()
        }
    }

    override fun syncToken(token: String) {
        if (!tokenStore.arePushNotificationsEnabled()) return

        val normalizedToken = token.trim()
        if (normalizedToken.isEmpty()) return

        tokenStore.savePendingToken(normalizedToken)
        enqueuePendingSync()
    }

    override fun arePushNotificationsEnabled(): Boolean =
        tokenStore.arePushNotificationsEnabled()

    override fun hasPushNotificationsPreference(): Boolean =
        tokenStore.hasPushNotificationsPreference()

    override fun setPushNotificationsEnabled(enabled: Boolean) {
        tokenStore.setPushNotificationsEnabled(enabled)
        applyPushNotificationsEnabled(enabled)
    }

    override fun onNotificationPermissionResult(granted: Boolean) {
        tokenStore.setPushNotificationsPermissionResult(granted)
        applyPushNotificationsEnabled(granted)
    }

    override fun reconcileNotificationPermission(granted: Boolean) {
        val shouldApply = if (granted) {
            tokenStore.werePushNotificationsDisabledBySystem()
        } else {
            tokenStore.arePushNotificationsEnabled()
        }
        if (shouldApply) onNotificationPermissionResult(granted)
    }

    private fun applyPushNotificationsEnabled(enabled: Boolean) {

        if (enabled) {
            tokenStore.clearTokenRemovalPending()
        } else {
            tokenStore.clearPendingToken()
            if (tokenStore.isAuthenticated()) {
                tokenStore.markTokenRemovalPending()
                enqueuePendingSync()
            }
        }

        firebaseCommandExecutor.execute {
            val messaging = FirebaseMessaging.getInstance()
            messaging.setAutoInitEnabled(enabled)
            if (enabled && tokenStore.isAuthenticated()) {
                messaging.register()
            } else if (!enabled) {
                messaging.unregister()
            }
        }
    }

    override fun onSignedOut() {
        tokenStore.setAuthenticated(false)
        workScheduler.cancel()
    }

    private fun enqueuePendingSync() {
        if (!tokenStore.isAuthenticated()) return
        if (!tokenStore.isTokenRemovalPending() && tokenStore.pendingToken() == null) return

        workScheduler.enqueue()
    }
}

internal fun firebaseInstallationTarget(installationId: String): String =
    "$FIREBASE_INSTALLATION_ID_PREFIX${installationId.trim()}"

private const val FIREBASE_INSTALLATION_ID_PREFIX = "fid:"
