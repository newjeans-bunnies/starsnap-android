package com.sns.starsnap.app.fcm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FcmPayloadParserTest {

    @Test
    fun notificationFieldsTakePrecedenceOverDataFallbacks() {
        val payload = FcmPayloadParser.parse(
            notificationTitle = "친구 요청",
            notificationBody = "별님이 친구 요청을 보냈어요.",
            data = mapOf(
                "title" to "fallback title",
                "body" to "fallback body",
                "type" to "friend_request",
                "actorUserId" to "user-1",
            ),
        )

        assertEquals("친구 요청", payload?.title)
        assertEquals("별님이 친구 요청을 보냈어요.", payload?.body)
        assertEquals("friend_request", payload?.type)
        assertEquals("user-1", payload?.actorUserId)
        assertNull(payload?.snapId)
        assertNull(payload?.eventId)
        assertNull(payload?.roomId)
        assertNull(payload?.messageId)
        assertNull(payload?.senderUserId)
    }

    @Test
    fun chatMessageKeepsDestinationIdentifiers() {
        val payload = FcmPayloadParser.parse(
            notificationTitle = null,
            notificationBody = null,
            data = mapOf(
                "title" to "별님",
                "body" to "안녕하세요",
                "type" to "chat_message",
                "roomId" to "room-1",
                "messageId" to "message-1",
                "senderUserId" to "sender-1",
            ),
        )

        assertEquals("별님", payload?.title)
        assertEquals("안녕하세요", payload?.body)
        assertEquals("chat_message", payload?.type)
        assertEquals("room-1", payload?.roomId)
        assertEquals("message-1", payload?.messageId)
        assertEquals("sender-1", payload?.senderUserId)
    }

    @Test
    fun snapLikeKeepsSnapDestination() {
        val payload = FcmPayloadParser.parse(
            notificationTitle = null,
            notificationBody = null,
            data = mapOf(
                "title" to "새로운 좋아요",
                "body" to "별님 님이 회원님의 스냅을 좋아해요.",
                "type" to "snap_liked",
                "actorUserId" to "actor-1",
                "snapId" to "snap-1",
                "eventId" to "like-1",
            ),
        )

        assertEquals("새로운 좋아요", payload?.title)
        assertEquals("snap_liked", payload?.type)
        assertEquals("actor-1", payload?.actorUserId)
        assertEquals("snap-1", payload?.snapId)
        assertEquals("like-1", payload?.eventId)
    }

    @Test
    fun snapLikeRetryUsesStableNotificationRequestCode() {
        val payload = FcmPayloadParser.parse(
            notificationTitle = null,
            notificationBody = null,
            data = mapOf(
                "body" to "좋아요 알림",
                "type" to "snap_liked",
                "eventId" to "like-1",
            ),
        )!!
        val otherPayload = payload.copy(eventId = "like-2")

        val firstAttempt = StarSnapNotificationManager.resolveRequestCode("fcm-message-1", payload)
        val retryAttempt = StarSnapNotificationManager.resolveRequestCode("fcm-message-2", payload)

        assertEquals(firstAttempt, retryAttempt)
        assertNotEquals(firstAttempt, StarSnapNotificationManager.resolveRequestCode("fcm-message-3", otherPayload))
    }

    @Test
    fun dataOnlyMessageUsesSafeDefaults() {
        val payload = FcmPayloadParser.parse(
            notificationTitle = null,
            notificationBody = null,
            data = mapOf(
                "body" to "새로운 소식이 있어요.",
                "type" to "unsupported_destination",
            ),
        )

        assertEquals("StarSnap", payload?.title)
        assertEquals("새로운 소식이 있어요.", payload?.body)
        assertEquals("general", payload?.type)
        assertNull(payload?.actorUserId)
    }

    @Test
    fun messageWithoutBodyIsIgnored() {
        val payload = FcmPayloadParser.parse(
            notificationTitle = "제목만 있음",
            notificationBody = " ",
            data = emptyMap(),
        )

        assertNull(payload)
    }
}
