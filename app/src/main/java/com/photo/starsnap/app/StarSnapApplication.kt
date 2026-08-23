package com.photo.starsnap.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.photo.starsnap.app.fcm.StarSnapNotificationManager
import com.photo.starsnap.datastore.FcmTokenStore
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlin.concurrent.thread

@HiltAndroidApp
class StarSnapApplication: Application() {
    @Inject
    lateinit var fcmTokenStore: FcmTokenStore

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val pushNotificationsEnabled = fcmTokenStore.arePushNotificationsEnabled()
        thread(
            start = true,
            isDaemon = true,
            name = "fcm-auto-init",
        ) {
            FirebaseMessaging.getInstance().setAutoInitEnabled(pushNotificationsEnabled)
        }
        StarSnapNotificationManager.createChannel(this)
    }
}
