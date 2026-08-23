package com.photo.starsnap.datastore

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-durable state used to keep Firebase callbacks separate from protected APIs.
 * The token lives only in this app's private preferences and is never written to logs.
 */
@Singleton
class FcmTokenStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun savePendingToken(token: String) {
        preferences.edit().putString(PENDING_TOKEN, token).commit()
    }

    fun pendingToken(): String? = preferences.getString(PENDING_TOKEN, null)

    fun isAuthenticated(): Boolean = preferences.getBoolean(AUTHENTICATED, false)

    fun hasPushNotificationsPreference(): Boolean =
        preferences.contains(PUSH_NOTIFICATIONS_ENABLED)

    fun arePushNotificationsEnabled(): Boolean =
        preferences.getBoolean(PUSH_NOTIFICATIONS_ENABLED, false)

    fun werePushNotificationsDisabledBySystem(): Boolean =
        preferences.getBoolean(PUSH_NOTIFICATIONS_DISABLED_BY_SYSTEM, false)

    fun isTokenRemovalPending(): Boolean =
        preferences.getBoolean(TOKEN_REMOVAL_PENDING, false)

    @Synchronized
    fun setAuthenticated(authenticated: Boolean) {
        preferences.edit().putBoolean(AUTHENTICATED, authenticated).commit()
    }

    @Synchronized
    fun setPushNotificationsEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(PUSH_NOTIFICATIONS_ENABLED, enabled)
            .remove(PUSH_NOTIFICATIONS_DISABLED_BY_SYSTEM)
            .commit()
    }

    @Synchronized
    fun setPushNotificationsPermissionResult(granted: Boolean) {
        val editor = preferences.edit().putBoolean(PUSH_NOTIFICATIONS_ENABLED, granted)
        if (granted) {
            editor.remove(PUSH_NOTIFICATIONS_DISABLED_BY_SYSTEM)
        } else {
            editor.putBoolean(PUSH_NOTIFICATIONS_DISABLED_BY_SYSTEM, true)
        }
        editor.commit()
    }

    @Synchronized
    fun markTokenRemovalPending() {
        preferences.edit().putBoolean(TOKEN_REMOVAL_PENDING, true).commit()
    }

    @Synchronized
    fun clearTokenRemovalPending() {
        preferences.edit().remove(TOKEN_REMOVAL_PENDING).commit()
    }

    @Synchronized
    fun clearPendingToken() {
        preferences.edit().remove(PENDING_TOKEN).commit()
    }

    @Synchronized
    fun clearPendingTokenIfMatches(token: String) {
        if (pendingToken() == token) {
            preferences.edit().remove(PENDING_TOKEN).commit()
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "starsnap_fcm_state"
        const val PENDING_TOKEN = "pending_token"
        const val AUTHENTICATED = "authenticated"
        const val PUSH_NOTIFICATIONS_ENABLED = "push_notifications_enabled"
        const val PUSH_NOTIFICATIONS_DISABLED_BY_SYSTEM =
            "push_notifications_disabled_by_system"
        const val TOKEN_REMOVAL_PENDING = "token_removal_pending"
    }
}
