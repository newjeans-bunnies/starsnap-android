package com.sns.starsnap.main.viewmodel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sns.starsnap.datastore.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ThemeUiState(
    val isLoaded: Boolean = false,
    val darkModePreference: Boolean? = null,
)

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
) : ViewModel() {
    val uiState: StateFlow<ThemeUiState> = themePreferences.darkModePreference
        .map { preference ->
            ThemeUiState(
                isLoaded = true,
                darkModePreference = preference,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeUiState(),
        )

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setDarkMode(enabled)
        }
    }

    fun useSystemTheme() {
        viewModelScope.launch {
            themePreferences.useSystemTheme()
        }
    }
}
