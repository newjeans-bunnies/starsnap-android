package com.sns.starsnap.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private val Context.themePreferencesDataStore by preferencesDataStore(
    name = "theme_preferences",
)

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val darkModePreference: Flow<Boolean?> = context.themePreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences -> preferences[DARK_MODE_ENABLED] }
        .onEach(::cacheDarkModePreference)

    suspend fun setDarkMode(enabled: Boolean) {
        context.themePreferencesDataStore.edit { preferences ->
            preferences[DARK_MODE_ENABLED] = enabled
        }
        cacheDarkModePreference(enabled)
    }

    suspend fun useSystemTheme() {
        context.themePreferencesDataStore.edit { preferences ->
            preferences.remove(DARK_MODE_ENABLED)
        }
        cacheDarkModePreference(null)
    }

    private fun cacheDarkModePreference(preference: Boolean?) {
        context.getSharedPreferences(BOOTSTRAP_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .apply {
                if (preference == null) {
                    remove(DARK_MODE_BOOTSTRAP_KEY)
                } else {
                    putBoolean(DARK_MODE_BOOTSTRAP_KEY, preference)
                }
            }
            .apply()
    }

    companion object {
        private const val BOOTSTRAP_PREFERENCES_NAME = "theme_bootstrap"
        private const val DARK_MODE_BOOTSTRAP_KEY = "dark_mode_enabled"
        private val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")

        fun getBootstrapDarkMode(context: Context): Boolean? {
            val preferences = context.getSharedPreferences(
                BOOTSTRAP_PREFERENCES_NAME,
                Context.MODE_PRIVATE,
            )
            return if (preferences.contains(DARK_MODE_BOOTSTRAP_KEY)) {
                preferences.getBoolean(DARK_MODE_BOOTSTRAP_KEY, false)
            } else {
                null
            }
        }
    }
}
