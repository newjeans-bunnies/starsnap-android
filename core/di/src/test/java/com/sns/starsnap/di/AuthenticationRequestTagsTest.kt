package com.sns.starsnap.di

import com.sns.starsnap.datastore.AuthSessionGate
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthenticationRequestTagsTest {

    @Test(timeout = 2_000)
    fun `gate-owning request reuses the existing session lock`() = runBlocking {
        val gate = AuthSessionGate()
        val request = Request.Builder()
            .url("https://example.test/fcm-token")
            .header(SESSION_GATE_HEADER, SESSION_GATE_HELD)
            .build()
            .withAuthenticationRequestTags()
        var enteredNestedOperation = false

        gate.withLock {
            gate.withRequestLock(request) {
                enteredNestedOperation = true
            }
        }

        assertTrue(enteredNestedOperation)
    }

    @Test
    fun `internal session header becomes a local request tag`() {
        val request = Request.Builder()
            .url("https://example.test/fcm-token")
            .header(SESSION_GATE_HEADER, SESSION_GATE_HELD)
            .build()

        val tagged = request.withAuthenticationRequestTags()

        assertNull(tagged.header(SESSION_GATE_HEADER))
        assertSame(SessionGateHeld, tagged.tag(SessionGateHeld::class.java))
    }

    @Test
    fun `public auth header becomes a local skip-authentication tag`() {
        val request = Request.Builder()
            .url("https://example.test/login")
            .header("Auth", "false")
            .build()

        val tagged = request.withAuthenticationRequestTags()

        assertNull(tagged.header("Auth"))
        assertSame(SkipAuthentication, tagged.tag(SkipAuthentication::class.java))
    }

    @Test
    fun `cookie-only authentication removes authorization header and preserves cookie`() {
        val request = Request.Builder()
            .url("https://example.test/profile")
            .header("Authorization", "Bearer legacy-token")
            .header("Cookie", "access-token=cookie-token")
            .build()

        val cookieOnly = request.withCookieOnlyAuthentication()

        assertNull(cookieOnly.header("Authorization"))
        assertTrue(cookieOnly.header("Cookie")?.contains("access-token=cookie-token") == true)
    }
}
