package com.sns.starsnap.network.message

import com.sns.starsnap.network.message.dto.ChatMessageDto
import com.sns.starsnap.network.message.dto.ChatMessageHistoryPageDto
import com.sns.starsnap.network.message.dto.ChatRoomCreatePayload
import com.sns.starsnap.network.message.dto.ChatRoomSummaryDto
import com.sns.starsnap.network.message.dto.ChatSendPayload
import com.sns.starsnap.network.message.dto.ChatUpdatePayload
import javax.inject.Inject

class MessageApiRepositoryImpl @Inject constructor(
    private val messageApi: MessageApi
) : MessageRepository {
    override suspend fun getRooms(): List<ChatRoomSummaryDto> {
        return messageApi.getRooms()
    }

    override suspend fun getHistory(
        roomId: String,
        beforeMessageId: String?,
        size: Int
    ): ChatMessageHistoryPageDto {
        return messageApi.getHistory(roomId, beforeMessageId, size)
    }

    override suspend fun createRoom(payload: ChatRoomCreatePayload): ChatRoomSummaryDto {
        return messageApi.createRoom(payload)
    }

    override suspend fun send(payload: ChatSendPayload): ChatMessageDto {
        return messageApi.send(payload)
    }

    override suspend fun update(messageId: String, payload: ChatUpdatePayload): ChatMessageDto {
        return messageApi.update(messageId, payload)
    }

    override suspend fun delete(messageId: String) {
        messageApi.delete(messageId)
    }
}
