package com.photo.starsnap.di

import android.util.Log
import com.photo.starsnap.datastore.SessionManager
import com.photo.starsnap.datastore.TokenManager
import com.photo.starsnap.datastore.FcmTokenStore
import com.photo.starsnap.datastore.AuthSessionGate
import com.photo.starsnap.network.token.TokenRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import dagger.Lazy

class AuthAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val tokenRepository: Lazy<TokenRepository>,
    private val cookieJar: PersistentCookieJar,
    private val sessionManager: SessionManager,
    private val fcmTokenStore: FcmTokenStore,
    private val authSessionGate: AuthSessionGate,
) : Authenticator {

    companion object {
        private const val TAG = "AuthAuthenticator"
        private const val MAX_RETRY_COUNT = 3
    }

    // Refreshes are serialized after acquiring the session gate. Gate-owning requests reuse it.
    private val refreshMutex = Mutex()

    // 재발급 네트워크 호출은 한 번만 수행하고, 그 시점 이후에 보내진 요청들은 결과를 공유하도록 기록
    @Volatile
    private var lastRefreshedAtMillis: Long = 0L

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d(TAG, response.toString())
        // Auth: false이면 jwt token 없이 전달
        if (response.request.tag(SkipAuthentication::class.java) != null) {
            return null
        }

        if(response.code != 401)
            return response.request.newBuilder()
                .build()

        // 재발급 후에도 계속 401이 나면(끝없이 재시도되지 않도록) 일정 횟수 이후엔 포기하고 로그아웃 처리
        if (responseCount(response) >= MAX_RETRY_COUNT) {
            Log.d(TAG, "재시도 한도 초과: 로그아웃 처리")
            runBlocking {
                authSessionGate.withRequestLock(response.request) { clearSession() }
            }
            return null
        }

        if (response.request.url.pathSegments.contains("refresh")) {
            Log.d(TAG, "토큰 재발급 요청 자체가 실패함: 로그아웃 처리")
            runBlocking {
                authSessionGate.withRequestLock(response.request) { clearSession() }
            }
            return null
        }

        // 쿠키 기반으로 변경: refresh 토큰은 HttpOnly 쿠키로 내려오므로
        // 재발급 요청은 서버의 /api/auth/refresh 엔드포인트를 호출하면
        // CookieJar가 자동으로 쿠키를 포함시킵니다.
        return runBlocking {
            authSessionGate.withRequestLock(response.request) {
                refreshMutex.withLock {
                    // Multiple 401s can share a refresh completed after their original requests.
                    if (response.sentRequestAtMillis < lastRefreshedAtMillis) {
                        return@withLock response.request.newBuilder().build()
                    }

                    if (!fcmTokenStore.isAuthenticated()) return@withLock null

                    runCatching {
                        tokenRepository.get().reissueToken()
                    }.fold(
                        onSuccess = {
                            tokenManager.saveExpiredAt(it.expiredAt)
                            lastRefreshedAtMillis = System.currentTimeMillis()
                            response.request.newBuilder().build()
                        },
                        onFailure = {
                            Log.d(TAG, "Refresh Token 만료: 로그아웃 처리")
                            clearSession()
                            null
                        }
                    )
                }
            }
        }
    }

    // 토큰 재발급 실패 시 로컬 토큰/쿠키를 모두 지우고 앱 전역에 세션 만료를 알린다(자동 로그아웃).
    private suspend fun clearSession() {
        tokenManager.deleteData()
        cookieJar.clear()
        fcmTokenStore.setAuthenticated(false)
        sessionManager.notifySessionExpired()
    }
}
