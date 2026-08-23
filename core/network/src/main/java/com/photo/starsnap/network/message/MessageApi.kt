package com.photo.starsnap.network.message

import com.photo.starsnap.network.message.dto.ChatMessageDto
import com.photo.starsnap.network.message.dto.ChatMessageHistoryPageDto
import com.photo.starsnap.network.message.dto.ChatRoomCreatePayload
import com.photo.starsnap.network.message.dto.ChatRoomSummaryDto
import com.photo.starsnap.network.message.dto.ChatSendPayload
import com.photo.starsnap.network.message.dto.ChatUpdatePayload
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MessageApi {
    @GET("/api/message/rooms") // 채팅방 목록
    suspend fun getRooms(): List<ChatRoomSummaryDto>

    @GET("/api/message/history")
    suspend fun getHistory(
        @Query("room-id") roomId: String,
        @Query("before-message-id") beforeMessageId: String? = null,
        @Query("size") size: Int = 50
    ): ChatMessageHistoryPageDto

    @POST("/api/message/rooms")
    suspend fun createRoom(@Body payload: ChatRoomCreatePayload): ChatRoomSummaryDto

    @POST("/api/message/send") // 메시지 전송
    suspend fun send(@Body payload: ChatSendPayload): ChatMessageDto

    @PUT("/api/message/{messageId}")
    suspend fun update(
        @Path("messageId") messageId: String,
        @Body payload: ChatUpdatePayload
    ): ChatMessageDto

    @DELETE("/api/message/{messageId}")
    suspend fun delete(@Path("messageId") messageId: String)
}
