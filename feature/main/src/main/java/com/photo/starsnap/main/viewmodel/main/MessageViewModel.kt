package com.photo.starsnap.main.viewmodel.main

import android.util.Log
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photo.starsnap.network.message.ChatSocketListener
import com.photo.starsnap.network.message.ChatSocketManager
import com.photo.starsnap.network.message.MessageRepository
import com.photo.starsnap.network.message.dto.ChatMessageDto
import com.photo.starsnap.network.message.dto.ChatMessageDeletedFrame
import com.photo.starsnap.network.message.dto.ChatMessageRateLimitedFrame
import com.photo.starsnap.network.message.dto.ChatMessageUpdatedFrame
import com.photo.starsnap.network.message.dto.ChatRoomCreatePayload
import com.photo.starsnap.network.message.dto.ChatRoomSummaryDto
import com.photo.starsnap.network.message.dto.ChatSendPayload
import com.photo.starsnap.network.message.dto.ChatTypingFrame
import com.photo.starsnap.network.message.dto.ChatUpdatePayload
import com.photo.starsnap.network.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ArrayDeque
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

data class ChatUiMessage(
    val id: String,
    val mine: Boolean,
    val senderUserId: String,
    val senderUsername: String,
    val text: String,
    val status: String,
    val createdAt: String,
    val createdAtIso: String = "",
)

data class ChatHistoryLoadError(
    val message: String,
    val retryOlder: Boolean,
)

data class ChatDraftRestoreEvent(
    val id: Long,
    val roomId: String,
    val content: String,
)

internal class ChatDraftRestoreStore {
    private val _events = MutableStateFlow<Map<String, List<ChatDraftRestoreEvent>>>(emptyMap())
    val events = _events.asStateFlow()

    fun enqueue(event: ChatDraftRestoreEvent) {
        _events.update { current ->
            current + (event.roomId to (current[event.roomId].orEmpty() + event))
        }
    }

    fun acknowledge(eventId: Long) {
        _events.update { current ->
            current.mapValues { (_, events) -> events.filterNot { it.id == eventId } }
                .filterValues { it.isNotEmpty() }
        }
    }
}

internal fun Map<String, List<ChatDraftRestoreEvent>>.nextRestorableDraft(
    roomId: String,
    currentInput: String,
): ChatDraftRestoreEvent? {
    val event = this[roomId]?.firstOrNull() ?: return null
    return event.takeIf {
        currentInput.isBlank() || currentInput.trim() == event.content
    }
}

internal class MessageSendRateLimiter(
    private val nowMillis: () -> Long = SystemClock::elapsedRealtime,
    private val windowMillis: Long = 10_000L,
    private val maxMessagesPerWindow: Int = 5,
    private val localBlockMillis: Long = 30_000L,
) {
    private val sentAtMillis = ArrayDeque<Long>()
    private var blockedUntilMillis = 0L

    @Synchronized
    fun checkBeforeSend(): Long {
        val now = nowMillis()
        val existingBlock = remainingMillisAt(now)
        if (existingBlock > 0L) return existingBlock

        pruneOldSends(now)
        if (sentAtMillis.size < maxMessagesPerWindow) return 0L

        sentAtMillis.clear()
        blockedUntilMillis = now + localBlockMillis
        return localBlockMillis
    }

    @Synchronized
    fun recordSuccessfulSend() {
        val now = nowMillis()
        pruneOldSends(now)
        sentAtMillis.addLast(now)
    }

    @Synchronized
    fun mergeServerBlock(retryAfterSeconds: Long): Long {
        val now = nowMillis()
        val safeRetrySeconds = retryAfterSeconds.coerceIn(0L, MAX_SERVER_RETRY_SECONDS)
        val serverDeadline = now + safeRetrySeconds * 1_000L
        blockedUntilMillis = maxOf(blockedUntilMillis, serverDeadline)
        return remainingMillisAt(now)
    }

    @Synchronized
    fun remainingMillis(): Long = remainingMillisAt(nowMillis())

    private fun remainingMillisAt(now: Long): Long {
        val remaining = (blockedUntilMillis - now).coerceAtLeast(0L)
        if (remaining == 0L) blockedUntilMillis = 0L
        return remaining
    }

    private fun pruneOldSends(now: Long) {
        val cutoff = now - windowMillis
        while (sentAtMillis.isNotEmpty() && sentAtMillis.first() <= cutoff) {
            sentAtMillis.removeFirst()
        }
    }

    private companion object {
        const val MAX_SERVER_RETRY_SECONDS = 24L * 60L * 60L
    }
}

private fun Long.toCountdownSeconds(): Int {
    if (this <= 0L) return 0
    return ((this + 999L) / 1_000L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val chatSocketManager: ChatSocketManager
) : ViewModel() {

    companion object {
        const val TAG = "MessageViewModel"
        private const val MAX_PENDING_DRAFTS = 20
        private const val LOCAL_RATE_LIMIT_MESSAGE =
            "메시지를 너무 빠르게 보냈어요. 잠시 후 다시 시도해주세요."
    }

    private val _rooms = MutableStateFlow<List<ChatRoomSummaryDto>>(emptyList())
    val rooms = _rooms.asStateFlow()

    private val _roomsLoading = MutableStateFlow(true)
    val roomsLoading = _roomsLoading.asStateFlow()

    private val _roomsError = MutableStateFlow<String?>(null)
    val roomsError = _roomsError.asStateFlow()

    private val _roomPreviews = MutableStateFlow<Map<String, String>>(emptyMap())
    val roomPreviews = _roomPreviews.asStateFlow()

    private val _selectedRoom = MutableStateFlow<ChatRoomSummaryDto?>(null)
    val selectedRoom = _selectedRoom.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatUiMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _historyLoading = MutableStateFlow(false)
    val historyLoading = _historyLoading.asStateFlow()

    private val _olderHistoryLoading = MutableStateFlow(false)
    val olderHistoryLoading = _olderHistoryLoading.asStateFlow()

    private val _historyError = MutableStateFlow<ChatHistoryLoadError?>(null)
    val historyError = _historyError.asStateFlow()

    private val _hasMoreMessages = MutableStateFlow(false)
    val hasMoreMessages = _hasMoreMessages.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _connected = MutableStateFlow(false)
    val connected = _connected.asStateFlow()

    private val _typingSenderUserId = MutableStateFlow<String?>(null)
    val typingSenderUserId = _typingSenderUserId.asStateFlow()

    private val _sendCooldownRemainingSeconds = MutableStateFlow(0)
    val sendCooldownRemainingSeconds = _sendCooldownRemainingSeconds.asStateFlow()

    private val draftRestoreStore = ChatDraftRestoreStore()
    val draftRestoreEvents = draftRestoreStore.events

    private var myUserId: String = ""
    private var myUsername: String = ""
    private var started = false
    private var socketReconnectJob: Job? = null
    private var typingStopJob: Job? = null
    private var remoteTypingStopJob: Job? = null
    private var sendCooldownJob: Job? = null
    private var roomsLoadJob: Job? = null
    private var historyLoadJob: Job? = null
    private var olderHistoryLoadJob: Job? = null
    private var localTypingRoomId: String? = null
    private val roomsLoadGeneration = AtomicLong(0)
    private val historyLoadGeneration = AtomicLong(0)
    private val olderHistoryLoadGeneration = AtomicLong(0)
    @Volatile
    private var sendCooldownGeneration = 0L
    private val sendCooldownLock = Any()
    private val nextPendingDraftId = AtomicLong(0)
    private val sendRateLimiter = MessageSendRateLimiter()
    private val pendingDrafts = ArrayDeque<PendingChatDraft>()

    private val socketListener = object : ChatSocketListener {
        override fun onMessage(message: ChatMessageDto) {
            handleIncoming(message)
        }

        override fun onMessageUpdated(frame: ChatMessageUpdatedFrame) {
            handleMessageUpdated(frame.message)
        }

        override fun onMessageDeleted(frame: ChatMessageDeletedFrame) {
            handleMessageDeleted(frame)
        }

        override fun onMessageRateLimited(frame: ChatMessageRateLimitedFrame) {
            handleMessageRateLimited(frame)
        }

        override fun onTyping(frame: ChatTypingFrame) {
            handleTyping(frame)
        }

        override fun onConnectionChanged(connected: Boolean) {
            _connected.value = connected
            if (connected) {
                socketReconnectJob?.cancel()
                socketReconnectJob = null
                refreshRooms()
                _selectedRoom.value?.let(::loadHistory)
            } else {
                scheduleSocketReconnect()
            }
        }
    }

    /** 로그인 후 최초 1회: 내 정보/방 목록/소켓 연결. */
    fun start() {
        if (started) return
        started = true
        viewModelScope.launch {
            loadMe()
            refreshRooms()
            chatSocketManager.connect(socketListener)
        }
    }

    private fun scheduleSocketReconnect() {
        if (socketReconnectJob?.isActive == true) return
        socketReconnectJob = viewModelScope.launch {
            var attempt = 0
            while (isActive && !chatSocketManager.isConnected()) {
                val delayMillis = minOf(1_000L * (1L shl attempt.coerceAtMost(4)), 15_000L)
                delay(delayMillis)
                chatSocketManager.connect(socketListener)
                attempt += 1
            }
        }
    }

    private suspend fun loadMe() {
        runCatching { userRepository.getUserData() }
            .onSuccess {
                myUserId = it.userId
                myUsername = it.username
            }
            .onFailure { Log.d(TAG, "loadMe failed", it) }
    }

    fun refreshRooms() {
        roomsLoadJob?.cancel()
        val generation = roomsLoadGeneration.incrementAndGet()
        _roomsLoading.value = true
        _roomsError.value = null
        roomsLoadJob = viewModelScope.launch {
            try {
                val list = messageRepository.getRooms()
                if (generation == roomsLoadGeneration.get()) {
                    _rooms.value = list
                    updateRoomPreviews(list)
                    _roomsError.value = null
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (generation == roomsLoadGeneration.get()) {
                    Log.d(TAG, "refreshRooms failed", error)
                    _roomsError.value = "네트워크 상태를 확인한 뒤 다시 시도해 주세요."
                }
            } finally {
                if (generation == roomsLoadGeneration.get()) {
                    _roomsLoading.value = false
                    roomsLoadJob = null
                }
            }
        }
    }

    private fun updateRoomPreviews(rooms: List<ChatRoomSummaryDto>) {
        _roomPreviews.value = rooms.associate { room -> room.roomId to roomPreviewText(room) }
    }

    private fun roomPreviewText(room: ChatRoomSummaryDto): String {
        val last = room.lastMessage ?: return ""
        if (last.status == "DELETED") return "삭제된 메시지"
        return last.content
    }

    fun selectRoom(room: ChatRoomSummaryDto) {
        stopTyping()
        remoteTypingStopJob?.cancel()
        _typingSenderUserId.value = null
        _error.value = null
        _historyError.value = null
        olderHistoryLoadGeneration.incrementAndGet()
        _olderHistoryLoading.value = false
        _selectedRoom.value = room
        _messages.value = emptyList()
        _hasMoreMessages.value = false
        loadHistory(room)
    }

    fun roomDisplayName(room: ChatRoomSummaryDto): String =
        room.title ?: room.members
            .filter { it.userId != myUserId }
            .joinToString(", ") { it.username }
            .ifBlank { "대화" }

    fun roomProfileImageUrl(room: ChatRoomSummaryDto): String? =
        room.members.firstOrNull { it.userId != myUserId }?.profileImageUrl

    fun roomParticipantCount(room: ChatRoomSummaryDto): Int =
        room.members.count { it.userId != myUserId }

    /** username 으로 1:1 방을 열거나 생성한 뒤, 방 선택이 완료되면 콜백을 실행한다. */
    fun openChatWith(username: String, onRoomReady: () -> Unit) {
        start()
        viewModelScope.launch {
            if (myUserId.isBlank()) loadMe()
            val existing = _rooms.value.firstOrNull { room ->
                room.members.size == 2 &&
                    room.members.any { it.userId == myUserId } &&
                    room.members.any { it.username == username }
            }
            if (existing != null) {
                selectRoom(existing)
                onRoomReady()
                return@launch
            }
            runCatching { userRepository.getUserByUsername(username) }
                .onSuccess { partner ->
                    if (partner.userId == myUserId) {
                        _error.value = "자기 자신과의 대화방은 만들 수 없어요."
                        return@onSuccess
                    }

                    runCatching {
                        messageRepository.createRoom(
                            ChatRoomCreatePayload(memberUserIds = listOf(partner.userId))
                        )
                    }
                        .onSuccess { room ->
                            selectRoom(room)
                            refreshRooms()
                            onRoomReady()
                        }
                        .onFailure { createRoomError ->
                            Log.d(TAG, "createRoom failed", createRoomError)
                            _error.value = resolveCreateRoomErrorMessage(createRoomError)
                        }
                }
                .onFailure {
                    Log.d(TAG, "openChatWith failed", it)
                    _error.value = "상대방을 찾을 수 없어요."
                }
        }
    }

    private fun resolveCreateRoomErrorMessage(error: Throwable): String {
        val httpException = error as? HttpException
        if (httpException?.code() == 400) {
            val serverMessage = runCatching {
                httpException.response()?.errorBody()?.string()?.let { body ->
                    JSONObject(body).optString("message")
                }
            }.getOrNull().orEmpty()

            if (serverMessage.contains("a chat room needs at least two members")) {
                return "최소 1명의 다른 사용자를 초대해야 대화방을 만들 수 있어요."
            }
            if (serverMessage.contains("only friends can be invited")) {
                return "친구인 사용자만 대화방에 초대할 수 있어요."
            }
            if (serverMessage.contains("member not found")) {
                return "존재하지 않는 사용자가 포함되어 있어요. 사용자명을 다시 확인해주세요."
            }
            if (serverMessage.isNotBlank()) {
                return serverMessage
            }
        }

        return "대화방을 만들지 못했어요. 잠시 후 다시 시도해주세요."
    }

    private fun loadHistory(room: ChatRoomSummaryDto) {
        historyLoadJob?.cancel()
        olderHistoryLoadJob?.cancel()
        olderHistoryLoadJob = null
        olderHistoryLoadGeneration.incrementAndGet()
        _olderHistoryLoading.value = false
        val generation = historyLoadGeneration.incrementAndGet()
        val baselineMessages = _messages.value
        _historyError.value = null
        _historyLoading.value = true
        historyLoadJob = viewModelScope.launch {
            try {
                val historyPage = messageRepository.getHistory(room.roomId)
                if (!isCurrentHistoryRequest(generation, room.roomId)) return@launch

                val historyMessages = historyPage.messages.map(::toUiMessage)
                _messages.update { current ->
                    if (isCurrentHistoryRequest(generation, room.roomId)) {
                        mergeChatHistory(
                            history = historyMessages,
                            current = current,
                            baseline = baselineMessages,
                        )
                    } else {
                        current
                    }
                }
                if (!isCurrentHistoryRequest(generation, room.roomId)) return@launch
                _hasMoreMessages.value = historyPage.hasMore && historyPage.messages.isNotEmpty()
                _historyError.value = null
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (isCurrentHistoryRequest(generation, room.roomId)) {
                    Log.d(TAG, "loadHistory failed", error)
                    _historyError.value = ChatHistoryLoadError(
                        message = "메시지를 불러오지 못했어요.",
                        retryOlder = false,
                    )
                }
            } finally {
                if (generation == historyLoadGeneration.get()) {
                    _historyLoading.value = false
                    historyLoadJob = null
                }
            }
        }
    }

    fun loadOlderMessages() {
        val room = _selectedRoom.value ?: return
        val beforeMessageId = _messages.value.firstOrNull()?.id ?: return
        if (_historyLoading.value || !_hasMoreMessages.value || _olderHistoryLoading.value) return

        olderHistoryLoadJob?.cancel()
        val generation = olderHistoryLoadGeneration.incrementAndGet()
        _historyError.value = null
        _olderHistoryLoading.value = true
        olderHistoryLoadJob = viewModelScope.launch {
            try {
                val historyPage = messageRepository.getHistory(
                    roomId = room.roomId,
                    beforeMessageId = beforeMessageId,
                )
                if (!isCurrentOlderHistoryRequest(generation, room.roomId)) return@launch

                val olderMessages = historyPage.messages.map(::toUiMessage)
                _messages.update { current ->
                    if (isCurrentOlderHistoryRequest(generation, room.roomId)) {
                        val currentMessageIds = current.mapTo(mutableSetOf(), ChatUiMessage::id)
                        olderMessages.filterNot { it.id in currentMessageIds } + current
                    } else {
                        current
                    }
                }
                if (!isCurrentOlderHistoryRequest(generation, room.roomId)) return@launch
                _hasMoreMessages.value = historyPage.hasMore && historyPage.messages.isNotEmpty()
                _historyError.value = null
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (isCurrentOlderHistoryRequest(generation, room.roomId)) {
                    Log.d(TAG, "loadOlderMessages failed", error)
                    _historyError.value = ChatHistoryLoadError(
                        message = "이전 메시지를 불러오지 못했어요.",
                        retryOlder = true,
                    )
                }
            } finally {
                if (generation == olderHistoryLoadGeneration.get()) {
                    _olderHistoryLoading.value = false
                    olderHistoryLoadJob = null
                }
            }
        }
    }

    fun retryHistory() {
        val room = _selectedRoom.value ?: return
        if (_historyError.value?.retryOlder == true) {
            loadOlderMessages()
        } else {
            loadHistory(room)
        }
    }

    private fun isCurrentHistoryRequest(generation: Long, roomId: String): Boolean =
        generation == historyLoadGeneration.get() && _selectedRoom.value?.roomId == roomId

    private fun isCurrentOlderHistoryRequest(generation: Long, roomId: String): Boolean =
        shouldCommitOlderHistory(
            requestGeneration = generation,
            currentGeneration = olderHistoryLoadGeneration.get(),
            requestRoomId = roomId,
            currentRoomId = _selectedRoom.value?.roomId,
            fullHistoryLoading = _historyLoading.value,
        )

    fun onDraftChanged(text: String) {
        val room = _selectedRoom.value
        if (room == null || text.isBlank()) {
            stopTyping()
            return
        }

        if (localTypingRoomId != room.roomId) {
            stopTyping()
            if (chatSocketManager.sendTyping(room.roomId, true)) {
                localTypingRoomId = room.roomId
            }
        }

        typingStopJob?.cancel()
        typingStopJob = viewModelScope.launch {
            delay(2_000)
            stopTyping()
        }
    }

    fun onInputFocusChanged(isFocused: Boolean) {
        if (!isFocused) stopTyping()
    }

    private fun stopTyping() {
        typingStopJob?.cancel()
        typingStopJob = null
        val roomId = localTypingRoomId ?: return
        chatSocketManager.sendTyping(roomId, false)
        localTypingRoomId = null
    }

    private fun handleTyping(frame: ChatTypingFrame) {
        if (_selectedRoom.value?.roomId != frame.roomId || frame.senderUserId == myUserId) return

        remoteTypingStopJob?.cancel()
        if (!frame.isTyping) {
            _typingSenderUserId.value = null
            return
        }

        _typingSenderUserId.value = frame.senderUserId
        remoteTypingStopJob = viewModelScope.launch {
            delay(3_000)
            if (_typingSenderUserId.value == frame.senderUserId) {
                _typingSenderUserId.value = null
            }
        }
    }

    fun send(rawText: String, onSent: () -> Unit) {
        val text = rawText.trim()
        val room = _selectedRoom.value
        if (text.isEmpty() || room == null || myUserId.isBlank()) return
        val localCooldownMillis = sendRateLimiter.checkBeforeSend()
        if (localCooldownMillis > 0L) {
            showSendCooldown(localCooldownMillis, LOCAL_RATE_LIMIT_MESSAGE)
            return
        }
        if (!chatSocketManager.isConnected()) {
            _error.value = "실시간 연결을 복구 중이에요. 연결된 뒤 다시 보내주세요."
            return
        }
        _error.value = null
        stopTyping()

        // 서버가 발신자에게도 echo 하므로 목록은 수신 콜백에서 갱신된다.
        val payload = ChatSendPayload(roomId = room.roomId, content = text)
        val pendingDraft = PendingChatDraft(
            id = nextPendingDraftId.incrementAndGet(),
            roomId = room.roomId,
            content = text,
        )
        synchronized(pendingDrafts) {
            if (pendingDrafts.size >= MAX_PENDING_DRAFTS) pendingDrafts.removeFirst()
            pendingDrafts.addLast(pendingDraft)
        }
        if (chatSocketManager.send(payload)) {
            sendRateLimiter.recordSuccessfulSend()
            onSent()
        } else {
            synchronized(pendingDrafts) { pendingDrafts.remove(pendingDraft) }
            _error.value = "실시간 연결을 복구 중이에요. 연결된 뒤 다시 보내주세요."
        }
    }

    private fun handleMessageRateLimited(frame: ChatMessageRateLimitedFrame) {
        val pendingDraft = synchronized(pendingDrafts) {
            val matchingDraft = if (frame.roomId != null && frame.content != null) {
                pendingDrafts.firstOrNull { draft ->
                    draft.roomId == frame.roomId && draft.content == frame.content
                }
            } else {
                pendingDrafts.firstOrNull()
            }
            matchingDraft?.also(pendingDrafts::remove)
        }
        val rejectedDraft = pendingDraft ?: frame.roomId
            ?.let { roomId ->
                frame.content
                    ?.takeIf { it.isNotBlank() }
                    ?.let { content ->
                        PendingChatDraft(
                            id = nextPendingDraftId.incrementAndGet(),
                            roomId = roomId,
                            content = content,
                        )
                    }
            }
        rejectedDraft?.let { draft ->
            val restoreEvent = ChatDraftRestoreEvent(
                id = draft.id,
                roomId = draft.roomId,
                content = draft.content,
            )
            draftRestoreStore.enqueue(restoreEvent)
        }

        val remainingMillis = sendRateLimiter.mergeServerBlock(frame.retryAfterSeconds)
        val message = frame.message.ifBlank { LOCAL_RATE_LIMIT_MESSAGE }
        showSendCooldown(remainingMillis, message)
    }

    fun acknowledgeDraftRestored(eventId: Long) {
        draftRestoreStore.acknowledge(eventId)
    }

    private fun showSendCooldown(remainingMillis: Long, message: String) {
        synchronized(sendCooldownLock) {
            val generation = ++sendCooldownGeneration
            _error.value = message
            _sendCooldownRemainingSeconds.value = remainingMillis.toCountdownSeconds()

            sendCooldownJob?.cancel()
            sendCooldownJob = viewModelScope.launch {
                while (isActive) {
                    val currentRemainingMillis = sendRateLimiter.remainingMillis()
                    val shouldContinue = synchronized(sendCooldownLock) {
                        if (generation != sendCooldownGeneration) {
                            false
                        } else {
                            _sendCooldownRemainingSeconds.value =
                                currentRemainingMillis.toCountdownSeconds()
                            if (currentRemainingMillis <= 0L) {
                                if (_error.value == message) {
                                    _error.value = null
                                }
                                sendCooldownJob = null
                                false
                            } else {
                                true
                            }
                        }
                    }
                    if (!shouldContinue) return@launch
                    delay(minOf(1_000L, currentRemainingMillis))
                }
            }
        }
    }

    fun updateMessage(messageId: String, rawText: String) {
        val text = rawText.trim()
        val room = _selectedRoom.value
        if (text.isEmpty() || room == null || myUserId.isBlank()) return
        _error.value = null

        viewModelScope.launch {
            runCatching { messageRepository.update(messageId, ChatUpdatePayload(content = text)) }
                .onSuccess { handleMessageUpdated(it) }
                .onFailure {
                    Log.d(TAG, "update failed", it)
                    _error.value = "메시지 수정에 실패했어요."
                }
        }
    }

    fun deleteMessage(messageId: String) {
        if (myUserId.isBlank()) return
        _error.value = null

        viewModelScope.launch {
            runCatching { messageRepository.delete(messageId) }
                .onSuccess {
                    _messages.update { current ->
                        current.map { message ->
                            if (message.id == messageId) {
                                message.copy(status = "DELETED", text = "삭제된 메시지")
                            } else {
                                message
                            }
                        }
                    }
                    refreshRooms()
                }
                .onFailure {
                    Log.d(TAG, "delete failed", it)
                    _error.value = "메시지 삭제에 실패했어요."
                }
        }
    }

    private fun handleIncoming(message: ChatMessageDto) {
        acknowledgePendingDraft(message)
        if (_selectedRoom.value?.roomId == message.roomId) {
            if (message.senderUserId != myUserId) _typingSenderUserId.value = null
            appendMessage(message)
        }
        refreshRooms()
    }

    private fun acknowledgePendingDraft(message: ChatMessageDto) {
        if (message.senderUserId != myUserId) return
        synchronized(pendingDrafts) {
            val matchingDraft = pendingDrafts.firstOrNull { draft ->
                draft.roomId == message.roomId && draft.content == message.content
            }
            if (matchingDraft != null) pendingDrafts.remove(matchingDraft)
        }
    }

    private fun handleMessageUpdated(message: ChatMessageDto) {
        if (_selectedRoom.value?.roomId != message.roomId) return

        val updated = toUiMessage(message)
        _messages.update { messages ->
            if (_selectedRoom.value?.roomId != message.roomId) {
                messages
            } else if (messages.any { it.id == updated.id }) {
                messages.map { current ->
                    if (current.id == updated.id) updated else current
                }
            } else {
                (messages + updated).sortedBy { it.createdAtIso.ifBlank { it.createdAt } }
            }
        }
        refreshRooms()
    }

    private fun handleMessageDeleted(frame: ChatMessageDeletedFrame) {
        if (_selectedRoom.value?.roomId == frame.roomId) {
            _messages.update { current ->
                if (_selectedRoom.value?.roomId != frame.roomId) {
                    current
                } else {
                    current.map { message ->
                        if (message.id == frame.messageId) {
                            message.copy(status = frame.status, text = "삭제된 메시지")
                        } else {
                            message
                        }
                    }
                }
            }
        }
        refreshRooms()
    }

    private fun appendMessage(message: ChatMessageDto) {
        val incoming = toUiMessage(message)
        _messages.update { current ->
            if (_selectedRoom.value?.roomId != message.roomId || current.any { it.id == incoming.id }) {
                current
            } else {
                current + incoming
            }
        }
    }

    private fun toUiMessage(message: ChatMessageDto): ChatUiMessage {
        val text = if (message.status == "DELETED") "삭제된 메시지" else message.content
        return ChatUiMessage(
            id = message.id,
            mine = message.senderUserId == myUserId,
            senderUserId = message.senderUserId,
            senderUsername = message.senderUsername,
            text = text,
            status = message.status,
            createdAt = formatTime(message.createdAt),
            createdAtIso = message.createdAt,
        )
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        stopTyping()
        socketReconnectJob?.cancel()
        remoteTypingStopJob?.cancel()
        synchronized(sendCooldownLock) {
            sendCooldownGeneration += 1
            sendCooldownJob?.cancel()
        }
        chatSocketManager.disconnect()
        super.onCleared()
    }

    private fun formatTime(iso: String): String =
        runCatching {
            LocalDateTime.parse(iso).format(DateTimeFormatter.ofPattern("HH:mm"))
        }.getOrElse { "" }

    private data class PendingChatDraft(
        val id: Long,
        val roomId: String,
        val content: String,
    )

}

internal fun shouldCommitOlderHistory(
    requestGeneration: Long,
    currentGeneration: Long,
    requestRoomId: String,
    currentRoomId: String?,
    fullHistoryLoading: Boolean,
): Boolean =
    !fullHistoryLoading &&
        requestGeneration == currentGeneration &&
        requestRoomId == currentRoomId

internal fun mergeChatHistory(
    history: List<ChatUiMessage>,
    current: List<ChatUiMessage>,
    baseline: List<ChatUiMessage> = emptyList(),
): List<ChatUiMessage> {
    val currentById = current.associateBy(ChatUiMessage::id)
    val baselineById = baseline.associateBy(ChatUiMessage::id)
    val mergedById = linkedMapOf<String, ChatUiMessage>()
    history.forEach { message ->
        val changedSinceRequest = currentById[message.id]
            ?.takeIf { currentMessage -> currentMessage != baselineById[message.id] }
        mergedById[message.id] = changedSinceRequest ?: message
    }
    current.forEach { message ->
        mergedById.putIfAbsent(message.id, message)
    }

    return mergedById.values.sortedWith(
        compareBy<ChatUiMessage> { message -> message.createdAtIso.ifBlank { message.createdAt } },
    )
}
