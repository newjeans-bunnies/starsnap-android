package com.photo.starsnap.network.notification

/**
 * Synchronizes this app installation's FCM registration target with the authenticated user.
 *
 * Implementations own their retry/logging policy so authentication flows do not
 * have to depend on Firebase APIs directly.
 */
interface FcmTokenSyncer {
    /** Marks a newly established session and schedules durable token delivery. */
    fun onAuthenticated()

    /** Persists a refreshed Firebase registration target; upload is gated on authentication. */
    fun syncToken(token: String)

    /** Returns the user's durable in-app push preference. */
    fun arePushNotificationsEnabled(): Boolean

    /** Applies the preference locally and schedules server registration or removal. */
    fun setPushNotificationsEnabled(enabled: Boolean)

    /** Prevents background work from calling protected APIs after session loss. */
    fun onSignedOut()
}
