package com.photo.starsnap.main.viewmodel.auth

import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.photo.starsnap.datastore.TokenManager
import com.photo.starsnap.datastore.SessionManager
import com.photo.starsnap.datastore.AuthSessionGate
import com.photo.starsnap.di.PersistentCookieJar
import com.photo.starsnap.main.viewmodel.state.AutoLoginState
import com.photo.starsnap.main.viewmodel.state.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.photo.starsnap.network.auth.AuthRepository
import com.photo.starsnap.network.auth.dto.rq.LoginDto
import com.photo.starsnap.network.notification.FcmTokenSyncer
import com.photo.starsnap.network.token.TokenRepository

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository,
    private val tokenManager: TokenManager,
    private val fcmTokenSyncer: FcmTokenSyncer,
    private val cookieJar: PersistentCookieJar,
    private val sessionManager: SessionManager,
    private val authSessionGate: AuthSessionGate,
) : ViewModel() {

    private val _loginState = MutableLiveData(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    private val _autoLoginState = MutableLiveData(AutoLoginState.Idle)
    val autoLoginState: LiveData<AutoLoginState> = _autoLoginState

    companion object {
        private const val TAG = "LoginViewModel"
    }

    fun reissueToken() = viewModelScope.launch {
        _autoLoginState.value = AutoLoginState.Loading
        authSessionGate.withLock {
        runCatching {
            tokenRepository.reissueToken()
        }.onSuccess {
            tokenManager.deleteData()
            tokenManager.saveExpiredAt(it.expiredAt)
            fcmTokenSyncer.onAuthenticated()
            Log.d(TAG, "Session refreshed")
            _autoLoginState.value = AutoLoginState.Success
        }.onFailure {
            Log.e(TAG, "reissueToken error", it)
            tokenManager.deleteData()
            fcmTokenSyncer.onSignedOut()
            _autoLoginState.value = AutoLoginState.Failure
        }
        }
    }

    fun login(username: String, password: String) = viewModelScope.launch {
        _loginState.value = LoginState.Loading
        authSessionGate.withLock {
        runCatching {
            cookieJar.clear()
            authRepository.login(LoginDto(username, password))
        }.onSuccess {
            Log.d(TAG, "✅ Login successful")

            // HttpOnly 쿠키는 자동으로 처리됨 - 명시적 저장 불필요
            // expiredAt만 저장 (이는 쿠키가 아닌 response body에 포함됨)
            tokenManager.deleteData()
            tokenManager.saveExpiredAt(it.expiredAt)
            fcmTokenSyncer.onAuthenticated()
            Log.d(TAG, "✅ Expiration saved: ${it.expiredAt}")
            Log.d(TAG, "ℹ️ HttpOnly 쿠키는 CookieJar가 자동으로 처리합니다")

            _loginState.value = LoginState.Success
        }.onFailure {
            Log.e(TAG, "❌ login error: ${it.message}", it)
            _loginState.value = LoginState.Failure
        }
        }
    }

    fun logout() = viewModelScope.launch {
        authSessionGate.withLock {
        fcmTokenSyncer.onSignedOut()
        runCatching { authRepository.logout() }
            .onFailure { Log.w(TAG, "Backend logout failed", it) }

        tokenManager.deleteData()
        cookieJar.clear()
        sessionManager.notifySessionExpired()
        }
    }

    fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        GoogleIdTokenCredential.createFrom(credential.data)
                        Log.d(OAuthViewModel.TAG, "Google credential parsed")
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(OAuthViewModel.TAG, "Received an invalid google id token response", e)
                        _loginState.value = LoginState.Failure
                    } catch (e: Exception) {
                        Log.e(OAuthViewModel.TAG, e.toString())
                        _loginState.value = LoginState.Failure
                    }
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(OAuthViewModel.TAG, "Unexpected type of credential")
                _loginState.value = LoginState.Failure
            }
        }
    }
}
