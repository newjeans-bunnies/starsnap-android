package com.sns.starsnap.main.viewmodel.main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MessageHistoryMergeTest {
    @Test
    fun `fresh history keeps previously loaded older messages before latest page and live messages after it`() {
        val history = listOf(
            message("m3", "2026-08-26T10:03:00", "server m3"),
            message("m4", "2026-08-26T10:04:00", "server m4"),
        )
        val current = listOf(
            message("m1", "2026-08-26T10:01:00", "older m1"),
            message("m2", "2026-08-26T10:02:00", "older m2"),
            message("m3", "2026-08-26T10:03:00", "socket-updated m3"),
            message("m5", "2026-08-26T10:05:00", "live m5"),
        )
        val baseline = current
            .filterNot { it.id == "m5" }
            .map { if (it.id == "m3") it.copy(text = "baseline m3") else it }

        val merged = mergeChatHistory(history = history, current = current, baseline = baseline)

        assertEquals(listOf("m1", "m2", "m3", "m4", "m5"), merged.map(ChatUiMessage::id))
        assertEquals("socket-updated m3", merged.first { it.id == "m3" }.text)
    }

    @Test
    fun `fresh history and in-flight socket messages are deduplicated and ordered by server timestamp`() {
        val history = listOf(
            message("m2", "2026-08-26T10:02:00"),
            message("m3", "2026-08-26T10:03:00"),
        )
        val current = listOf(
            message("m4", "2026-08-26T10:04:00"),
            message("m3", "2026-08-26T10:03:00", "socket m3"),
        )

        val merged = mergeChatHistory(history = history, current = current)

        assertEquals(listOf("m2", "m3", "m4"), merged.map(ChatUiMessage::id))
        assertEquals("socket m3", merged.first { it.id == "m3" }.text)
    }

    @Test
    fun `reconnect history replaces unchanged baseline with server edit and delete`() {
        val baseline = listOf(
            message("edited", "2026-08-26T10:01:00", "offline old text"),
            message("deleted", "2026-08-26T10:02:00", "offline visible text"),
        )
        val history = listOf(
            message("edited", "2026-08-26T10:01:00", "server edited text"),
            message("deleted", "2026-08-26T10:02:00", "삭제된 메시지", status = "DELETED"),
        )

        val merged = mergeChatHistory(
            history = history,
            current = baseline,
            baseline = baseline,
        )

        assertEquals("server edited text", merged.first { it.id == "edited" }.text)
        assertEquals("DELETED", merged.first { it.id == "deleted" }.status)
        assertEquals("삭제된 메시지", merged.first { it.id == "deleted" }.text)
    }

    @Test
    fun `socket update received after request start wins over response duplicate`() {
        val baseline = listOf(message("m1", "2026-08-26T10:01:00", "baseline"))
        val current = listOf(message("m1", "2026-08-26T10:01:00", "live socket update"))
        val history = listOf(message("m1", "2026-08-26T10:01:00", "response value"))

        val merged = mergeChatHistory(history = history, current = current, baseline = baseline)

        assertEquals("live socket update", merged.single().text)
    }

    @Test
    fun `stale older success cannot commit cursor or clear full refresh error after refresh fails`() {
        val roomId = "room-1"
        val staleOlderGeneration = 4L
        val generationAfterFullRefreshStarted = staleOlderGeneration + 1L

        assertFalse(
            shouldCommitOlderHistory(
                requestGeneration = staleOlderGeneration,
                currentGeneration = staleOlderGeneration,
                requestRoomId = roomId,
                currentRoomId = roomId,
                fullHistoryLoading = true,
            ),
        )

        val canCommitAfterFullRefreshFailure = shouldCommitOlderHistory(
            requestGeneration = staleOlderGeneration,
            currentGeneration = generationAfterFullRefreshStarted,
            requestRoomId = roomId,
            currentRoomId = roomId,
            fullHistoryLoading = false,
        )

        var hasMore = false
        var historyError: String? = "full refresh failed"
        if (canCommitAfterFullRefreshFailure) {
            hasMore = true
            historyError = null
        }

        assertFalse(canCommitAfterFullRefreshFailure)
        assertFalse(hasMore)
        assertEquals("full refresh failed", historyError)
    }

    private fun message(
        id: String,
        createdAtIso: String,
        text: String = id,
        status: String = "NORMAL",
    ) = ChatUiMessage(
        id = id,
        mine = false,
        senderUserId = "sender",
        senderUsername = "sender",
        text = text,
        status = status,
        createdAt = createdAtIso.substringAfter('T').take(5),
        createdAtIso = createdAtIso,
    )
}
