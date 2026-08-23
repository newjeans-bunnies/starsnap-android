package com.photo.starsnap.network.message

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.photo.starsnap.network.message.dto.ChatMessageDto
import com.photo.starsnap.network.message.dto.ChatMessageDeletedFrame
import com.photo.starsnap.network.message.dto.ChatMessageRateLimitedFrame
import com.photo.starsnap.network.message.dto.ChatMessageUpdatedFrame
import com.photo.starsnap.network.message.dto.ChatSendPayload
import com.photo.starsnap.network.message.dto.ChatTypingFrame
import com.photo.starsnap.network.message.dto.ChatTypingPayload
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/** 채팅 소켓 이벤트 콜백. UI(ViewModel) 계층에서 구현한다. */
interface ChatSocketListener {
    fun onMessage(message: ChatMessageDto)
    fun onMessageUpdated(frame: ChatMessageUpdatedFrame)
    fun onMessageDeleted(frame: ChatMessageDeletedFrame)
    fun onMessageRateLimited(frame: ChatMessageRateLimitedFrame)
    fun onTyping(frame: ChatTypingFrame)
    fun onConnectionChanged(connected: Boolean)
}

/**
 * /ws-chat 실시간 채팅 소켓. 로그인 쿠키가 담긴 기존 OkHttpClient 를 재사용하므로
 * 별도의 인증 헤더 없이 핸드셰이크에서 서버가 principal 을 인식한다.
 *
 * 코루틴/Flow 의존을 :core:network 로 끌어오지 않기 위해 콜백 기반으로 노출한다.
 */
class ChatSocketManager(
    private val okHttpClient: OkHttpClient,
    baseUrl: String
) {
    companion object {
        private const val TAG = "ChatSocketManager"
    }

    private val gson = Gson()
    private val wsUrl: String = resolveWsUrl(baseUrl)

    private var webSocket: WebSocket? = null
    private var listener: ChatSocketListener? = null
    @Volatile
    private var connected = false

    fun isConnected(): Boolean = connected

    @Synchronized
    fun connect(listener: ChatSocketListener) {
        this.listener = listener
        if (webSocket != null) return
        val request = Request.Builder().url(wsUrl).build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                connected = true
                this@ChatSocketManager.listener?.onConnectionChanged(true)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val frame = runCatching { gson.fromJson(text, JsonObject::class.java) }.getOrNull() ?: return
                if (frame.get("type")?.asString == "typing") {
                    Log.d(TAG, "typing frame: $frame")
                    runCatching { gson.fromJson(frame, ChatTypingFrame::class.java) }
                        .getOrNull()
                        ?.let { this@ChatSocketManager.listener?.onTyping(it) }
                    return
                }

                if (frame.get("type")?.asString == "message-rate-limited") {
                    Log.d(TAG, "message-rate-limited frame: $frame")
                    runCatching { gson.fromJson(frame, ChatMessageRateLimitedFrame::class.java) }
                        .getOrNull()
                        ?.let { this@ChatSocketManager.listener?.onMessageRateLimited(it) }
                    return
                }

                if (frame.get("type")?.asString == "message-updated") {
                    Log.d(TAG, "message-updated frame: $frame")
                    runCatching { gson.fromJson(frame, ChatMessageUpdatedFrame::class.java) }
                        .getOrNull()
                        ?.let { this@ChatSocketManager.listener?.onMessageUpdated(it) }
                    return
                }

                if (frame.get("type")?.asString == "message-deleted") {
                    Log.d(TAG, "message-deleted frame: $frame")
                    runCatching { gson.fromJson(frame, ChatMessageDeletedFrame::class.java) }
                        .getOrNull()
                        ?.let { this@ChatSocketManager.listener?.onMessageDeleted(it) }
                    return
                }

                runCatching { gson.fromJson(text, ChatMessageDto::class.java) }
                    .getOrNull()
                    ?.let { this@ChatSocketManager.listener?.onMessage(it) }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                notifyDisconnected(webSocket)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                notifyDisconnected(webSocket)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                notifyDisconnected(webSocket)
                Log.d(TAG, "socket failure", t)
            }
        })
    }

    /** 소켓이 열려 있으면 소켓으로 전송하고 true, 아니면 false 를 반환한다. */
    fun send(payload: ChatSendPayload): Boolean {
        val socket = webSocket?.takeIf { connected } ?: return false
        return socket.send(gson.toJson(payload))
    }

    fun sendTyping(roomId: String, isTyping: Boolean): Boolean {
        val socket = webSocket?.takeIf { connected } ?: return false
        return socket.send(gson.toJson(ChatTypingPayload(roomId = roomId, isTyping = isTyping)))
    }

    @Synchronized
    fun disconnect() {
        webSocket?.close(1000, null)
        webSocket = null
        connected = false
        listener = null
    }

    @Synchronized
    private fun notifyDisconnected(socket: WebSocket) {
        if (webSocket !== socket) return
        webSocket = null
        connected = false
        listener?.onConnectionChanged(false)
    }

    private fun resolveWsUrl(baseUrl: String): String {
        val trimmed = baseUrl.trimEnd('/')
        val wsBase = when {
            trimmed.startsWith("https://") -> "wss://" + trimmed.removePrefix("https://")
            trimmed.startsWith("http://") -> "ws://" + trimmed.removePrefix("http://")
            else -> trimmed
        }
        return "$wsBase/ws-chat"
    }
}
