package com.photo.starsnap.network.message.dto

// 서버(POST /api/message/send, /ws-chat)로 보내는 메시지 페이로드
data class ChatSendPayload(
    val roomId: String,
    val content: String
)

data class ChatUpdatePayload(
    val content: String
)

// 서버가 내려주는 메시지.
data class ChatMessageDto(
    val id: String,
    val roomId: String,
    val senderUserId: String,
    val senderUsername: String,
    val content: String,
    val status: String = "NORMAL",
    val createdAt: String
)

data class ChatMessageHistoryPageDto(
    val messages: List<ChatMessageDto>,
    val hasMore: Boolean
)

data class ChatTypingPayload(
    val type: String = "typing",
    val roomId: String,
    val isTyping: Boolean
)

data class ChatTypingFrame(
    val type: String,
    val roomId: String,
    val senderUserId: String,
    val isTyping: Boolean
)

data class ChatMessageRateLimitedFrame(
    val type: String,
    val code: String,
    val status: Int,
    val roomId: String? = null,
    val content: String? = null,
    val retryAfterSeconds: Long,
    val message: String
)

data class ChatMessageUpdatedFrame(
    val type: String,
    val message: ChatMessageDto
)

data class ChatMessageDeletedFrame(
    val type: String,
    val messageId: String,
    val roomId: String,
    val senderUserId: String,
    val status: String = "DELETED"
)

// 채팅방의 마지막 메시지.
data class ChatRoomLastMessageDto(
    val id: String,
    val senderUserId: String,
    val senderUsername: String,
    val content: String = "",
    val status: String = "NORMAL",
    val createdAt: String
)

// 채팅방 목록 요약. roomId 는 chat_room 테이블의 PK.
data class ChatRoomSummaryDto(
    val roomId: String,
    val title: String? = null,
    val members: List<ChatRoomMemberDto> = emptyList(),
    val lastMessage: ChatRoomLastMessageDto? = null,
    val lastMessageAt: String
)

data class ChatRoomMemberDto(
    val userId: String,
    val username: String,
    val profileImageUrl: String? = null
)

data class ChatRoomCreatePayload(
    val title: String? = null,
    val memberUserIds: List<String>
)
