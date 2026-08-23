package com.photo.starsnap.app.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.photo.starsnap.network.notification.FcmTokenSyncer
import com.photo.starsnap.datastore.FcmTokenStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StarSnapFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenSyncer: FcmTokenSyncer

    @Inject
    lateinit var fcmTokenStore: FcmTokenStore

    override fun onRegistered(installationId: String) {
        super.onRegistered(installationId)
        fcmTokenSyncer.syncToken(firebaseInstallationTarget(installationId))
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (
            fcmTokenStore.isAuthenticated() &&
            fcmTokenStore.arePushNotificationsEnabled()
        ) {
            StarSnapNotificationManager.show(this, message)
        }
    }

    override fun onUnregistered(installationId: String) {
        super.onUnregistered(installationId)
        if (
            fcmTokenStore.isAuthenticated() &&
            fcmTokenStore.arePushNotificationsEnabled()
        ) {
            FirebaseMessaging.getInstance().register()
        }
    }
}
