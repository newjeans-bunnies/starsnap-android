package com.sns.starsnap.main.viewmodel.auth

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sns.starsnap.main.utils.TextPattern
import com.sns.starsnap.network.auth.AuthRepository
import com.sns.starsnap.network.auth.dto.rq.SignupDto
import com.sns.starsnap.network.auth.dto.rq.VerifyEmailRequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    companion object {
        const val TAG = "SignupViewModel"
    }

    private var usernameCheckJob: Job? = null
    private var emailCheckJob: Job? = null
    private var timer: CountDownTimer? = null
    private var resendTimer: CountDownTimer? = null


    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> get() = _uiState

    private val _timerUiState = MutableStateFlow(TimerUiState())
    val timerUiState: StateFlow<TimerUiState> get() = _timerUiState


    var username: String
        get() = _uiState.value.username
        set(value) {
            _uiState.value = _uiState.value.copy(
                username = value, usernameButtonState = false
            )

            // 기존 검사 Job이 있다면 취소
            usernameCheckJob?.cancel()

            // 300ms 대기 후 검사 실행
            usernameCheckJob = viewModelScope.launch {
                delay(400) // 사용자가 입력을 멈춘 후 500ms 뒤 실행
                checkValidUserName(value)
            }
        }

    var password: String
        get() = _uiState.value.password
        set(value) {
            checkValidPassword(value, confirmPassword)
            _uiState.value =
                _uiState.value.copy(password = value, confirmPassword = confirmPassword)
        }

    var confirmPassword: String
        get() = _uiState.value.confirmPassword
        set(value) {
            checkValidPassword(password, value)
            _uiState.value = _uiState.value.copy(confirmPassword = value, password = password)
        }

    var email: String
        get() = _uiState.value.email
        set(value) {
            _uiState.value = _uiState.value.copy(email = value, emailSendButtonState = false)

            // 기존 검사 Job이 있다면 취소
            emailCheckJob?.cancel()

            // 500ms 대기 후 검사 실행
            usernameCheckJob = viewModelScope.launch {
                delay(400)
                checkValidEmail(value)
            }
        }

    var verifyCode: String
        get() = _uiState.value.verifyCode
        set(value) {
            _uiState.value = _uiState.value.copy(
                verifyCode = value,  // `verifyCode`도 복사해야 할 수 있습니다.
                verifyButtonState = value.length == 4
            )
            Log.d(TAG, value)

            if (value.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    verifyCodeState = VerifyCodeState.DEFAULT,
                    verifyButtonState = false  // `verifyButtonState`도 초기화해야 할 수 있습니다.
                )
            }

            Log.d(TAG, "${value.length} verifyButtonState: ${_uiState.value.verifyButtonState}")
        }

    // 회원가입
    fun signup() = viewModelScope.launch {
        val currentState = _uiState.value
        _uiState.value = _uiState.value.copy(signupState = State.LOADING)
        runCatching {
            delay(1000)
            authRepository.signup(
                SignupDto(
                    currentState.username,
                    currentState.password,
                    currentState.email,
                    currentState.token
                )
            )
        }.onSuccess {
            Log.d(TAG, it.toString())
            _uiState.value = _uiState.value.copy(signupState = State.SUCCESS)
        }.onFailure { e ->
            Log.d(TAG, e.message.toString())
            _uiState.value = _uiState.value.copy(signupState = State.ERROR)
        }
    }

    // 닉네임 유효성 검사
    private fun checkValidUserName(username: String) = viewModelScope.launch {
        val isUserNameValid = TextPattern.USER_NAME.toPattern().matcher(username).matches()
        if (username.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                usernameButtonState = false,
                usernameValidState = ValidState.DEFAULT,
            )
        } else if (isUserNameValid) {
            _uiState.value = _uiState.value.copy(
                usernameValidState = ValidState.LOADING,
                usernameButtonState = false
            )
            runCatching {
                delay(300)
                authRepository.validUsername(username)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    usernameButtonState = true,
                    usernameValidState = ValidState.SUCCESS
                )
                Log.d(TAG, it.status.toString())
            }.onFailure { e ->
                if (e.message == "HTTP 409 ") {
                    // 닉네임 중복일때 어떤 애러 메시지 나오는지 확인 뒤 EXIST 처리 필요
                    _uiState.value =
                        _uiState.value.copy(
                            usernameButtonState = false,
                            usernameValidState = ValidState.EXIST,
                        )
                } else {
                    _uiState.value =
                        _uiState.value.copy(
                            usernameButtonState = false,
                            usernameValidState = ValidState.INTERNET_ERROR,
                        )
                }
                Log.d(TAG, e.message.toString())
            }
        } else {
            _uiState.value = _uiState.value.copy(
                usernameButtonState = false,
                usernameValidState = ValidState.ERROR,
            )
        }
    }

    // 이메일 유효성 검사
    private fun checkValidEmail(email: String) = viewModelScope.launch {
        val isEmailValid = TextPattern.EMAIL.toPattern().matcher(email).matches()

        if (email.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                emailSendButtonState = false,
                emailValidState = ValidState.DEFAULT,
            )
        } else if (isEmailValid) {
            runCatching {
                authRepository.validEmail(email)
                _uiState.value = _uiState.value.copy(
                    emailSendButtonState = false, emailValidState = ValidState.LOADING
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    emailValidState = ValidState.SUCCESS, emailSendButtonState = true
                )
            }.onFailure { e ->
                if (e.message == "HTTP 409 ") {
                    // 닉네임 중복일때 어떤 애러 메시지 나오는지 확인 뒤 EXIST 처리 필요
                    _uiState.value = _uiState.value.copy(
                        emailSendButtonState = false, emailValidState = ValidState.EXIST
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        emailSendButtonState = false, emailValidState = ValidState.INTERNET_ERROR
                    )
                }
                Log.d(TAG, e.message.toString())
            }
        } else {
            _uiState.value = _uiState.value.copy(
                emailSendButtonState = false, emailValidState = ValidState.ERROR
            )
        }
    }

    // 인증 번호 발송
    fun sendEmail() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(validCodeState = State.LOADING, verifyCode = "")
        timerStart()
        timerResendStart()
        runCatching {
            authRepository.send(_uiState.value.email)
        }.onSuccess {
            Log.d(TAG, it.toString())
        }.onFailure { e ->
            Log.d(TAG, e.message.toString())
            _uiState.value = _uiState.value.copy(validCodeState = State.INTERNET_ERROR)
            _timerUiState.value =
                _timerUiState.value.copy(isTimerRunning = false, timerValue = 0L, resendTime = 0L)
            timer?.cancel()
            resendTimer?.cancel()
        }
    }

    // 인증 번호 확인
    fun checkVerifyCode(verifyCode: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            verifyCodeState = VerifyCodeState.DEFAULT,
            verifyButtonState = false
        )
        runCatching {
            authRepository.verify(VerifyEmailRequestDto(email, verifyCode))
        }.onSuccess {
            Log.d(TAG, it.toString())
            _uiState.value = _uiState.value.copy(
                token = it.token,
                verifyButtonState = true,
                verifyCodeState = VerifyCodeState.SUCCESS
            )

            // 코드 인증 완료시 타이머 종료
            timer?.cancel()
            resendTimer?.cancel()
            _timerUiState.value =
                _timerUiState.value.copy(timerValue = 0L, isTimerRunning = false, resendTime = 0L)
        }.onFailure { e ->
            if (e.message == "HTTP 409 " || e.message == "HTTP 400 ") {
                _uiState.value =
                    _uiState.value.copy(
                        verifyCodeState = VerifyCodeState.ERROR,
                        verifyButtonState = false,
                        verifyCode = ""
                    )
            } else {
                _uiState.value =
                    _uiState.value.copy(
                        verifyCodeState = VerifyCodeState.INTERNET_ERROR,
                        verifyButtonState = false
                    )
            }
            Log.d(TAG, e.message.toString())
        }
    }

    // 비밀번호 유효성 검사
    private fun checkValidPassword(password: String, confirmPassword: String) {
        val isPasswordValid = TextPattern.PASSWORD.toPattern().matcher(password).matches()

        val passwordState = when {
            password.isEmpty() -> PasswordState.DEFAULT
            !isPasswordValid -> PasswordState.ERROR
            confirmPassword.isEmpty() -> PasswordState.CONFIRM_EMPTY
            password != confirmPassword -> PasswordState.INVALID_CONFIRM
            else -> PasswordState.SUCCESS
        }

        _uiState.value = _uiState.value.copy(
            passwordButtonState = passwordState == PasswordState.SUCCESS,
            passwordValidState = passwordState
        )
    }


    private fun timerStart() {
        timer?.cancel()
        timer = object : CountDownTimer(300_000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _timerUiState.value = _timerUiState.value.copy(
                    isTimerRunning = true,
                    timerValue = millisUntilFinished / 1000
                )

                Log.d(TAG, "일반: ${millisUntilFinished / 1000}")
            }

            override fun onFinish() {
                // 타이머 종료 처리
                _uiState.value = _uiState.value.copy(
                    verifyCodeState = VerifyCodeState.RESEND,
                    verifyButtonState = false
                )
                _timerUiState.value =
                    _timerUiState.value.copy(
                        timerValue = 0L,
                        isTimerRunning = false
                    ) // 타이머 종료 시 0초로 설정
            }
        }.start()
    }

    private fun timerResendStart() {
        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(60_000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _timerUiState.value = _timerUiState.value.copy(
                    isTimerRunning = true,
                    resendTime = millisUntilFinished / 1000
                )
                Log.d(TAG, "재전송: ${millisUntilFinished / 1000}")
            }

            override fun onFinish() {
                // 타이머 종료 처리
                _timerUiState.value = _timerUiState.value.copy(
                    resendTime = 0L,
                )
            }
        }.start()
    }

    fun resetVerifyCodeState() {
        _uiState.value = _uiState.value.copy(
            verifyCodeState = VerifyCodeState.DEFAULT,
            verifyButtonState = false,
            verifyCode = ""
        )
    }
}

data class SignupUiState(
    val username: String = "", // 닉네임
    val password: String = "", // 비밀번호
    val email: String = "", // 이메일
    var verifyCode: String = "", // 인증번호
    val token: String = "", // 회원가입 토큰

    val usernameButtonState: Boolean = false,
    val usernameValidState: ValidState = ValidState.DEFAULT,

    val confirmPassword: String = "",
    val passwordButtonState: Boolean = false,
    val passwordValidState: PasswordState = PasswordState.DEFAULT, // 비밀번호 상태

    val emailSendButtonState: Boolean = false,
    val emailValidState: ValidState = ValidState.DEFAULT,
    val validCodeState: State = State.DEFAULT,

    val verifyButtonState: Boolean = false,
    val verifyCodeState: VerifyCodeState = VerifyCodeState.DEFAULT,

    val consentButtonState: Boolean = false,
    val signupButtonState: Boolean = false,

    val signupState: State = State.DEFAULT
)

data class TimerUiState(
    val isTimerRunning: Boolean = false,
    val timerValue: Long = 300L,
    val resendTime: Long = 60L
)

enum class State {
    SUCCESS, LOADING, ERROR, DEFAULT, INTERNET_ERROR
}

enum class PasswordState {
    SUCCESS,          // 비밀번호 정규식 일치 + 비밀번호 확인과 동일
    ERROR,            // 비밀번호 정규식 불일치
    DEFAULT,          // 비밀번호 미입력
    CONFIRM_EMPTY,    // 비밀번호 확인 미입력
    INVALID_CONFIRM   // 비밀번호 정규식은 일치하지만 비밀번호 확인이 없거나 다를 때
}

enum class ValidState {
    SUCCESS,          // 사용 가능한 아이디
    EXIST,            // 사용중인 아이디
    ERROR,            // 닉네임 정규식 불일치()
    LOADING,          // 닉네임 사용 가능 여부 확인중.
    DEFAULT,           // 닉네임 입력 전
    INTERNET_ERROR    // 인터넷 애러
}

enum class VerifyCodeState {
    SUCCESS,          // 인증 성공
    ERROR,            // 인증 실패
    LOADING,          // 인증 확인
    DEFAULT,          // 인증 코드 입력 전
    RESEND,           // 인증 번호 시간 지났을 경우
    INTERNET_ERROR    // 인터넷 애러
}