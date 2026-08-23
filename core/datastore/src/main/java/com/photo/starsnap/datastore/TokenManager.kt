package com.photo.starsnap.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "TokenManager"
        private const val DATASTORE_NAME = "TOKEN_DATASTORE"
        private val LEGACY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val LEGACY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val EXPIRED_AT = stringPreferencesKey("expired_at")
    }

    private val Context.tokenDataStore by preferencesDataStore(DATASTORE_NAME)

    suspend fun saveExpiredAt(expiredAt: String) {
        try {
            context.tokenDataStore.edit { prefs ->
                prefs[EXPIRED_AT] = expiredAt
                Log.d(TAG, "Expired at saved: $expiredAt")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save expired at", e)
        }
    }

    suspend fun deleteData() {
        try {
            context.tokenDataStore.edit { prefs ->
                prefs.remove(LEGACY_ACCESS_TOKEN)
                prefs.remove(LEGACY_REFRESH_TOKEN)
                prefs.remove(EXPIRED_AT)
                Log.d(TAG, "Session metadata and legacy tokens cleared")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear session data", e)
        }
    }

    fun getExpiredAt(): Flow<String> {
        return context.tokenDataStore.data
            .catch { exception ->
                Log.e(TAG, "Error reading expired at", exception)
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { prefs ->
                prefs[EXPIRED_AT] ?: ""
            }
    }
}
