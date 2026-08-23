package com.photo.starsnap.app.fcm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.RemoteMessage
import com.photo.starsnap.R
import com.photo.starsnap.main.MainActivity

object StarSnapNotificationManager {
    private const val EXTRA_NOTIFICATION_TYPE = "notification_type"
    private const val EXTRA_ACTOR_USER_ID = "notification_actor_user_id"
    private const val EXTRA_CHAT_ROOM_ID = "notification_chat_room_id"
    private const val EXTRA_CHAT_MESSAGE_ID = "notification_chat_message_id"
    private const val EXTRA_CHAT_SENDER_USER_ID = "notification_chat_sender_user_id"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            channelId(context),
            context.getString(R.string.fcm_social_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.fcm_social_channel_description)
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun show(context: Context, message: RemoteMessage) {
        val payload = FcmPayloadParser.parse(
            notificationTitle = message.notification?.title,
            notificationBody = message.notification?.body,
            data = message.data,
        ) ?: return

        if (!canPostNotifications(context)) return

        createChannel(context)

        val requestCode = message.messageId?.hashCode() ?: payload.hashCode()
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, payload.type)
            payload.actorUserId?.let { putExtra(EXTRA_ACTOR_USER_ID, it) }
            payload.roomId?.let { putExtra(EXTRA_CHAT_ROOM_ID, it) }
            payload.messageId?.let { putExtra(EXTRA_CHAT_MESSAGE_ID, it) }
            payload.senderUserId?.let { putExtra(EXTRA_CHAT_SENDER_USER_ID, it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, channelId(context))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(payload.title)
            .setContentText(payload.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(payload.body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(requestCode, notification)
        } catch (_: SecurityException) {
            // Permission can be revoked between the check above and this call.
            return
        }
    }

    private fun canPostNotifications(context: Context): Boolean {
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

        return permissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun channelId(context: Context): String =
        context.getString(R.string.fcm_default_channel_id)
}
