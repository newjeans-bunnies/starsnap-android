package com.photo.starsnap.di

import com.photo.starsnap.datastore.AuthSessionGate
import okhttp3.Request

internal object SkipAuthentication

/** Marks a request whose caller already owns [AuthSessionGate]. */
internal object SessionGateHeld

internal const val SESSION_GATE_HEADER = "X-StarSnap-Session-Gate"
internal const val SESSION_GATE_HELD = "held"

internal fun Request.withAuthenticationRequestTags(): Request {
    val skipsAuthentication = header("Auth") == "false"
    val sessionGateHeld = header(SESSION_GATE_HEADER) == SESSION_GATE_HELD
    if (!skipsAuthentication && !sessionGateHeld) return this

    return newBuilder().apply {
        if (skipsAuthentication) {
            removeHeader("Auth")
            tag(SkipAuthentication::class.java, SkipAuthentication)
        }
        if (sessionGateHeld) {
            removeHeader(SESSION_GATE_HEADER)
            tag(SessionGateHeld::class.java, SessionGateHeld)
        }
    }.build()
}

internal suspend fun <T> AuthSessionGate.withRequestLock(
    request: Request,
    action: suspend () -> T,
): T = if (request.tag(SessionGateHeld::class.java) != null) {
    action()
} else {
    withLock(action)
}
