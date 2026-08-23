package com.photo.starsnap.main.ui.screen.main.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photo.starsnap.main.utils.TextPattern
import com.photo.starsnap.network.auth.AuthRepository
import com.photo.starsnap.network.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

enum class UsernameAvailability {
    Unchanged,
    Invalid,
    Checking,
    Available,
    Taken,
    Error,
}

data class ProfileSettingState(
    val availability: UsernameAvailability = UsernameAvailability.Unchanged,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ProfileSettingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileSettingState())
    val state = _state.asStateFlow()

    private var availabilityJob: Job? = null

    fun checkUsername(username: String, originalUsername: String) {
        availabilityJob?.cancel()
        val normalizedUsername = username.trim()

        when {
            normalizedUsername == originalUsername -> {
                _state.value = _state.value.copy(
                    availability = UsernameAvailability.Unchanged,
                    errorMessage = null,
                )
            }

            !TextPattern.USER_NAME.matches(normalizedUsername) -> {
                _state.value = _state.value.copy(
                    availability = UsernameAvailability.Invalid,
                    errorMessage = null,
                )
            }

            else -> {
                _state.value = _state.value.copy(
                    availability = UsernameAvailability.Checking,
                    errorMessage = null,
                )
                availabilityJob = viewModelScope.launch {
                    delay(700)
                    runCatching {
                        authRepository.validUsername(normalizedUsername)
                    }.onSuccess {
                        _state.value = _state.value.copy(
                            availability = UsernameAvailability.Available,
                        )
                    }.onFailure { throwable ->
                        _state.value = _state.value.copy(
                            availability = if (throwable is HttpException && throwable.code() == 409) {
                                UsernameAvailability.Taken
                            } else {
                                UsernameAvailability.Error
                            },
                        )
                    }
                }
            }
        }
    }

    fun saveUsername(username: String, originalUsername: String) {
        if (_state.value.saving) return

        val normalizedUsername = username.trim()
        if (normalizedUsername == originalUsername) {
            _state.value = _state.value.copy(saved = true, errorMessage = null)
            return
        }
        if (_state.value.availability != UsernameAvailability.Available) {
            _state.value = _state.value.copy(errorMessage = "닉네임 중복 확인을 완료해주세요.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true, errorMessage = null)
            runCatching {
                userRepository.changeUsername(normalizedUsername)
            }.onSuccess {
                _state.value = _state.value.copy(
                    saving = false,
                    saved = true,
                    errorMessage = null,
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    saving = false,
                    errorMessage = "프로필 수정에 실패했습니다. 잠시 후 다시 시도해주세요.",
                )
            }
        }
    }

    fun consumeSaved() {
        _state.value = _state.value.copy(saved = false)
    }
}
