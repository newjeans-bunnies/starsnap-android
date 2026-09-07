package com.sns.starsnap.network.notification

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

    /** Returns whether the initial notification permission/preference has been resolved. */
    fun hasPushNotificationsPreference(): Boolean

    /** Applies the preference locally and schedules server registration or removal. */
    fun setPushNotificationsEnabled(enabled: Boolean)

    /** Applies the result of an Android notification permission request. */
    fun onNotificationPermissionResult(granted: Boolean)

    /** Reconciles a later permission change without overriding an explicit user opt-out. */
    fun reconcileNotificationPermission(granted: Boolean)

    /** Prevents background work from calling protected APIs after session loss. */
    fun onSignedOut()
}
