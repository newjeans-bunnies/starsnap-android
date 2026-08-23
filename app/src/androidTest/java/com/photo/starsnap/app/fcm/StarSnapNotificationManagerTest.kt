package com.photo.starsnap.app.fcm

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.messaging.RemoteMessage
import com.photo.starsnap.datastore.FcmTokenStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StarSnapNotificationManagerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val notificationManager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    @Before
    fun setUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }
        notificationManager.cancelAll()
    }

    @After
    fun tearDown() {
        notificationManager.cancelAll()
    }

    @Test
    fun dataOnlyFcmMessageCreatesChannelAndVisibleNotification() {
        val message = RemoteMessage.Builder("local-instrumentation-test")
            .setMessageId("fcm-notification-test")
            .addData("title", "친구 요청")
            .addData("body", "별님 님이 친구 요청을 보냈어요.")
            .addData("type", "friend_request")
            .addData("actorUserId", "actor-1")
            .build()

        StarSnapNotificationManager.show(context, message)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(
                context.getString(com.photo.starsnap.R.string.fcm_default_channel_id),
            )
            assertNotNull(channel)
            assertEquals(NotificationManager.IMPORTANCE_DEFAULT, channel.importance)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            assertNotificationDisplayed { statusBarNotification ->
                    statusBarNotification.notification.extras
                        .getString("android.title") == "친구 요청"
            }
        }
    }

    @Test
    fun dataOnlyChatMessageCreatesVisibleNotification() {
        val message = RemoteMessage.Builder("local-chat-notification-test")
            .setMessageId("chat-notification-test")
            .addData("title", "별님")
            .addData("body", "안녕하세요")
            .addData("type", "chat_message")
            .addData("roomId", "room-1")
            .addData("messageId", "message-1")
            .addData("senderUserId", "sender-1")
            .build()

        StarSnapNotificationManager.show(context, message)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            assertNotificationDisplayed { statusBarNotification ->
                    val extras = statusBarNotification.notification.extras
                    extras.getString("android.title") == "별님" &&
                        extras.getString("android.text") == "안녕하세요"
            }
        }
    }

    @Test
    fun disabledPushSuppressesIncomingNotification() {
        val tokenStore = FcmTokenStore(context)
        val initialAuthenticated = tokenStore.isAuthenticated()
        val initialPushNotificationsEnabled = tokenStore.arePushNotificationsEnabled()
        val message = RemoteMessage.Builder("local-disabled-notification-test")
            .setMessageId("disabled-notification-test")
            .addData("title", "별님")
            .addData("body", "이 알림은 표시되면 안 됩니다")
            .addData("type", "chat_message")
            .build()

        try {
            tokenStore.setAuthenticated(true)
            tokenStore.setPushNotificationsEnabled(false)
            StarSnapFirebaseMessagingService().apply {
                fcmTokenStore = tokenStore
            }.onMessageReceived(message)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                assertTrue(notificationManager.activeNotifications.isEmpty())
            }
        } finally {
            tokenStore.setPushNotificationsEnabled(initialPushNotificationsEnabled)
            tokenStore.setAuthenticated(initialAuthenticated)
        }
    }

    private fun assertNotificationDisplayed(
        matches: (StatusBarNotification) -> Boolean,
    ) {
        val deadline = SystemClock.uptimeMillis() + 1_000L
        do {
            if (notificationManager.activeNotifications.any(matches)) return
            SystemClock.sleep(25L)
        } while (SystemClock.uptimeMillis() < deadline)

        assertTrue("Expected notification was not displayed", false)
    }
}
