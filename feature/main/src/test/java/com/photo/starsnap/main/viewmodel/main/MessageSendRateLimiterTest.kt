package com.photo.starsnap.main.viewmodel.main

import org.junit.Assert.assertEquals
import org.junit.Test

class MessageSendRateLimiterTest {

    @Test
    fun sixthSendWithinTenSecondsStartsThirtySecondBlock() {
        var nowMillis = 0L
        val limiter = MessageSendRateLimiter(nowMillis = { nowMillis })

        repeat(5) {
            assertEquals(0L, limiter.checkBeforeSend())
            limiter.recordSuccessfulSend()
            nowMillis += 1_000L
        }

        assertEquals(30_000L, limiter.checkBeforeSend())

        nowMillis += 1_000L
        assertEquals(29_000L, limiter.remainingMillis())
    }

    @Test
    fun sendAtTenSecondBoundaryIsAllowed() {
        var nowMillis = 0L
        val limiter = MessageSendRateLimiter(nowMillis = { nowMillis })

        repeat(5) {
            assertEquals(0L, limiter.checkBeforeSend())
            limiter.recordSuccessfulSend()
        }

        nowMillis = 10_000L

        assertEquals(0L, limiter.checkBeforeSend())
    }

    @Test
    fun serverDeadlineOnlyExtendsExistingBlock() {
        var nowMillis = 0L
        val limiter = MessageSendRateLimiter(nowMillis = { nowMillis })

        assertEquals(30_000L, limiter.mergeServerBlock(retryAfterSeconds = 30L))

        nowMillis = 5_000L
        assertEquals(25_000L, limiter.mergeServerBlock(retryAfterSeconds = 10L))
        assertEquals(30_000L, limiter.mergeServerBlock(retryAfterSeconds = 30L))

        nowMillis = 35_000L
        assertEquals(0L, limiter.remainingMillis())
    }

    @Test
    fun shortServerBlockDoesNotResetLocalSendWindow() {
        var nowMillis = 0L
        val limiter = MessageSendRateLimiter(nowMillis = { nowMillis })

        repeat(5) {
            assertEquals(0L, limiter.checkBeforeSend())
            limiter.recordSuccessfulSend()
        }

        assertEquals(1_000L, limiter.mergeServerBlock(retryAfterSeconds = 1L))

        nowMillis = 1_000L
        assertEquals(30_000L, limiter.checkBeforeSend())
    }
}
