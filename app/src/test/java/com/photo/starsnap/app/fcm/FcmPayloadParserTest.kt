package com.photo.starsnap.app.fcm

import org.junit.Assert.assertEquals
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
