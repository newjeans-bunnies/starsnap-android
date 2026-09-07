package com.sns.starsnap.datastore

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 토큰 재발급 실패 등으로 세션이 만료되었을 때 이를 앱 전역(UI 계층)에 알리기 위한 이벤트 버스.
 * OkHttp Authenticator는 백그라운드 스레드에서 동작하므로 네비게이션을 직접 수행할 수 없어,
 * 이 이벤트를 구독하는 쪽(MainActivity 등)에서 로그인 화면으로 이동시킨다.
 */
@Singleton
class SessionManager @Inject constructor() {

    private val _sessionExpiredEvents = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val sessionExpiredEvents: SharedFlow<Unit> = _sessionExpiredEvents

    fun notifySessionExpired() {
        _sessionExpiredEvents.tryEmit(Unit)
    }
}
