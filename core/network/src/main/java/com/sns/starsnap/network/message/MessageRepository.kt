package com.sns.starsnap.network.message

import com.sns.starsnap.network.message.dto.ChatMessageDto
import com.sns.starsnap.network.message.dto.ChatMessageHistoryPageDto
import com.sns.starsnap.network.message.dto.ChatRoomCreatePayload
import com.sns.starsnap.network.message.dto.ChatRoomSummaryDto
import com.sns.starsnap.network.message.dto.ChatSendPayload
import com.sns.starsnap.network.message.dto.ChatUpdatePayload

interface MessageRepository {
    suspend fun getRooms(): List<ChatRoomSummaryDto>
    suspend fun getHistory(
        roomId: String,
        beforeMessageId: String? = null,
        size: Int = 50
    ): ChatMessageHistoryPageDto
    suspend fun createRoom(payload: ChatRoomCreatePayload): ChatRoomSummaryDto
    suspend fun send(payload: ChatSendPayload): ChatMessageDto
    suspend fun update(messageId: String, payload: ChatUpdatePayload): ChatMessageDto
    suspend fun delete(messageId: String)
}
