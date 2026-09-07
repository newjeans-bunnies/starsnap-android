package com.sns.starsnap.main.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sns.starsnap.datastore.AuthSessionGate
import com.sns.starsnap.datastore.TokenManager
import com.sns.starsnap.di.PersistentCookieJar
import com.sns.starsnap.main.utils.TextPattern
import com.sns.starsnap.network.auth.AuthRepository
import com.sns.starsnap.network.auth.dto.PasswordResetCodeRequestDto
import com.sns.starsnap.network.auth.dto.PasswordResetConfirmRequestDto
import com.sns.starsnap.network.auth.dto.PasswordResetEmailRequestDto
import com.sns.starsnap.network.notification.FcmTokenSyncer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val fcmTokenSyncer: FcmTokenSyncer,
    private val cookieJar: PersistentCookieJar,
    private val authSessionGate: AuthSessionGate,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PasswordResetUiState())
    val uiState: StateFlow<PasswordResetUiState> = _uiState.asStateFlow()

    private var resetToken = ""
    private var resendCountdown: Job? = null

    fun updateEmail(value: String) = updateState(email = value.take(320))

    fun updateCode(value: String) = updateState(
        code = value.filter { it in '0'..'9' }.take(6),
    )

    fun updateNewPassword(value: String) = updateState(newPassword = value.take(50))

    fun updateConfirmPassword(value: String) = updateState(confirmPassword = value.take(50))

    fun requestCode() {
        _uiState.value.passwordResetRequestCooldownMessage()?.let { return showError(it) }
        val email = _uiState.value.email.trim()
        passwordResetEmailError(email)?.let { return showError(it) }
        launchRequest {
            val response = authRepository.requestPasswordReset(PasswordResetEmailRequestDto(email))
            _uiState.update {
                it.copy(
                    email = email,
                    code = "",
                    step = PasswordResetStep.CODE,
                    statusMessage = response.message.ifBlank { "인증번호를 보냈습니다." },
                    errorMessage = null,
                )
            }
            startResendCountdown(response.resendAfterSeconds)
        }
    }

    fun verifyCode() {
        val state = _uiState.value
        passwordResetCodeError(state.code)?.let { return showError(it) }
        launchRequest {
            val response = authRepository.verifyPasswordReset(
                PasswordResetCodeRequestDto(state.email, state.code),
            )
            resetToken = response.resetToken
            _uiState.update {
                it.copy(
                    code = "",
                    step = PasswordResetStep.PASSWORD,
                    statusMessage = null,
                    errorMessage = null,
                )
            }
        }
    }

    fun confirmPassword() {
        val state = _uiState.value
        runPasswordResetConfirmation(state.newPassword, state.confirmPassword) {
            if (resetToken.isBlank()) {
                showError("인증 정보가 만료되었습니다. 다시 인증해 주세요.")
            } else {
                launchRequest {
                    authRepository.confirmPasswordReset(
                        PasswordResetConfirmRequestDto(resetToken, state.newPassword),
                    )
                    authSessionGate.withLock {
                        fcmTokenSyncer.onSignedOut()
                        tokenManager.deleteData()
                        cookieJar.clear()
                    }
                    resetToken = ""
                    resendCountdown?.cancel()
                    _uiState.update {
                        it.copy(
                            newPassword = "",
                            confirmPassword = "",
                            resendSecondsRemaining = 0,
                            step = PasswordResetStep.SUCCESS,
                            statusMessage = null,
                            errorMessage = null,
                        )
                    }
                }
            }
        }?.let(::showError)
    }

    fun goBack() {
        when (_uiState.value.step) {
            PasswordResetStep.PASSWORD -> restartAtEmail()
            PasswordResetStep.CODE -> {
                resendCountdown?.cancel()
                _uiState.update { it.restartAtEmail() }
            }
            else -> Unit
        }
    }

    private fun restartAtEmail() {
        resetToken = ""
        resendCountdown?.cancel()
        _uiState.update { it.restartAtEmail() }
    }

    private fun startResendCountdown(seconds: Long) {
        resendCountdown?.cancel()
        val initial = seconds.coerceAtLeast(0)
        _uiState.update { it.copy(resendSecondsRemaining = initial) }
        if (initial == 0L) return
        resendCountdown = viewModelScope.launch {
            repeat(initial.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()) {
                delay(1_000)
                _uiState.update {
                    it.copy(resendSecondsRemaining = (it.resendSecondsRemaining - 1).coerceAtLeast(0))
                }
            }
        }
    }

    private fun updateState(
        email: String? = null,
        code: String? = null,
        newPassword: String? = null,
        confirmPassword: String? = null,
    ) {
        _uiState.update {
            it.copy(
                email = email ?: it.email,
                code = code ?: it.code,
                newPassword = newPassword ?: it.newPassword,
                confirmPassword = confirmPassword ?: it.confirmPassword,
                errorMessage = null,
            )
        }
    }

    private fun launchRequest(request: suspend () -> Unit) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                request()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                error.passwordResetRetryAfterSeconds()?.let(::startResendCountdown)
                showError(error.passwordResetMessage())
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }
}

enum class PasswordResetStep { EMAIL, CODE, PASSWORD, SUCCESS }

data class PasswordResetUiState(
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val step: PasswordResetStep = PasswordResetStep.EMAIL,
    val isLoading: Boolean = false,
    val resendSecondsRemaining: Long = 0,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
)

internal fun PasswordResetUiState.restartAtEmail(): PasswordResetUiState = copy(
    code = "",
    newPassword = "",
    confirmPassword = "",
    step = PasswordResetStep.EMAIL,
    isLoading = false,
    resendSecondsRemaining = 0,
    statusMessage = null,
    errorMessage = null,
)

internal fun PasswordResetUiState.passwordResetRequestCooldownMessage(): String? =
    resendSecondsRemaining.takeIf { it > 0 }?.let { "${it}초 후에 다시 요청해 주세요." }

internal fun passwordResetEmailError(email: String): String? = when {
    email.isBlank() -> "이메일을 입력해 주세요."
    !TextPattern.EMAIL.matches(email) -> "올바른 이메일 주소를 입력해 주세요."
    else -> null
}

internal fun passwordResetCodeError(code: String): String? = when {
    code.length != 6 || !code.all { it in '0'..'9' } -> "인증번호 6자리를 입력해 주세요."
    else -> null
}

internal fun runPasswordResetConfirmation(
    password: String,
    confirmation: String,
    action: () -> Unit,
): String? {
    val error = passwordResetPasswordError(password, confirmation)
    if (error != null) return error
    action()
    return null
}

internal fun passwordResetPasswordError(password: String, confirmation: String): String? = when {
    password.isBlank() -> "새 비밀번호를 입력해 주세요."
    password.toByteArray(Charsets.UTF_8).size > 72 ->
        "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다."
    !TextPattern.PASSWORD.matches(password) ->
        "비밀번호는 8~50자의 영문 대·소문자, 숫자, 특수문자를 포함해야 합니다."
    confirmation.isBlank() -> "비밀번호 확인을 입력해 주세요."
    password != confirmation -> "비밀번호가 일치하지 않습니다."
    else -> null
}

private fun Throwable.passwordResetMessage(): String = when ((this as? HttpException)?.code()) {
    400 -> "인증번호 또는 재설정 정보가 올바르지 않거나 만료되었습니다."
    422 -> "입력한 내용을 다시 확인해 주세요."
    429 -> "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."
    503 -> "현재 비밀번호 재설정을 사용할 수 없습니다. 잠시 후 다시 시도해 주세요."
    else -> "네트워크 연결을 확인한 뒤 다시 시도해 주세요."
}

private fun Throwable.passwordResetRetryAfterSeconds(): Long? {
    val response = (this as? HttpException)?.response() ?: return null
    return passwordResetRetryAfterSeconds(
        statusCode = response.code(),
        headerValue = response.headers()["Retry-After"],
    )
}

internal fun passwordResetRetryAfterSeconds(statusCode: Int, headerValue: String?): Long? {
    if (statusCode != 429) return null
    return headerValue
        ?.trim()
        ?.toLongOrNull()
        ?.takeIf { it in 0..Int.MAX_VALUE.toLong() }
}
