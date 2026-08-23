package com.photo.starsnap.main.viewmodel.main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChatDraftRestoreStoreTest {

    @Test
    fun queuedDraftSurvivesRoomSwitchUntilItIsRestoredAndAcknowledged() {
        val store = ChatDraftRestoreStore()
        val event = ChatDraftRestoreEvent(id = 1L, roomId = "room-a", content = "retry me")
        store.enqueue(event)

        assertNull(store.events.value.nextRestorableDraft("room-b", currentInput = ""))
        assertEquals(event, store.events.value.nextRestorableDraft("room-a", currentInput = ""))

        store.acknowledge(event.id)
        assertNull(store.events.value.nextRestorableDraft("room-a", currentInput = ""))
    }

    @Test
    fun newerInputKeepsQueuedDraftUntilInputCanBeSafelyRestored() {
        val store = ChatDraftRestoreStore()
        val event = ChatDraftRestoreEvent(id = 2L, roomId = "room-a", content = "retry me")
        store.enqueue(event)

        assertNull(store.events.value.nextRestorableDraft("room-a", currentInput = "new draft"))
        assertEquals(event, store.events.value.nextRestorableDraft("room-a", currentInput = " "))
    }
}
