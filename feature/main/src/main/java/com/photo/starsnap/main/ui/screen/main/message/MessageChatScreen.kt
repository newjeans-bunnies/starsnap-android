package com.photo.starsnap.main.ui.screen.main.message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.StarSnapTypography
import com.photo.starsnap.main.ui.component.DataLoadErrorCard
import com.photo.starsnap.main.ui.component.TopAppBar
import com.photo.starsnap.main.ui.component.skeleton.SnapSkeleton
import com.photo.starsnap.main.utils.constant.Constant
import com.photo.starsnap.main.viewmodel.main.ChatUiMessage
import com.photo.starsnap.main.viewmodel.main.MessageViewModel
import com.photo.starsnap.main.viewmodel.main.nextRestorableDraft
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.flow.drop

@Composable
fun MessageChatScreen(
    mainNavController: NavHostController,
    messageViewModel: MessageViewModel
) {
    val selectedRoom by messageViewModel.selectedRoom.collectAsStateWithLifecycle()
    val messages by messageViewModel.messages.collectAsStateWithLifecycle()
    val historyLoading by messageViewModel.historyLoading.collectAsStateWithLifecycle()
    val olderHistoryLoading by messageViewModel.olderHistoryLoading.collectAsStateWithLifecycle()
    val historyError by messageViewModel.historyError.collectAsStateWithLifecycle()
    val hasMoreMessages by messageViewModel.hasMoreMessages.collectAsStateWithLifecycle()
    val error by messageViewModel.error.collectAsStateWithLifecycle()
    val typingSenderUserId by messageViewModel.typingSenderUserId.collectAsStateWithLifecycle()
    val sendCooldownRemainingSeconds by
        messageViewModel.sendCooldownRemainingSeconds.collectAsStateWithLifecycle()
    val draftRestoreEvents by messageViewModel.draftRestoreEvents.collectAsStateWithLifecycle()
    val isPartnerTyping = typingSenderUserId != null
    val roomName = selectedRoom?.let(messageViewModel::roomDisplayName) ?: "메시지"
    val roomProfileImageUrl = selectedRoom
        ?.let(messageViewModel::roomProfileImageUrl)
        ?.let(Constant::getImageUrl)
    val otherParticipantCount = selectedRoom?.let(messageViewModel::roomParticipantCount) ?: 0
    val roomParticipantCount = selectedRoom?.members?.size ?: 0
    val isGroupChat = otherParticipantCount > 1
    val typingMemberName = selectedRoom?.members
        ?.firstOrNull { it.userId == typingSenderUserId }
        ?.username
    val roomStatusText = if (isPartnerTyping) {
        "${typingMemberName ?: roomName} 입력 중"
    } else {
        "${roomParticipantCount}명 참여 중"
    }

    var input by remember { mutableStateOf(TextFieldValue("")) }
    var actionMessage by remember { mutableStateOf<ChatUiMessage?>(null) }
    var editingMessage by remember { mutableStateOf<ChatUiMessage?>(null) }
    var editText by remember { mutableStateOf("") }
    var deleteMessage by remember { mutableStateOf<ChatUiMessage?>(null) }
    val listState = rememberLazyListState()
    val currentHasMoreMessages by rememberUpdatedState(hasMoreMessages)
    val draftRestoreEvent = selectedRoom?.roomId?.let { roomId ->
        draftRestoreEvents.nextRestorableDraft(roomId, input.text)
    }

    androidx.compose.runtime.LaunchedEffect(
        draftRestoreEvent?.id,
        selectedRoom?.roomId,
        input.text,
    ) {
        val event = draftRestoreEvent ?: return@LaunchedEffect
        if (event.roomId == selectedRoom?.roomId) {
            input = TextFieldValue(
                text = event.content,
                selection = TextRange(event.content.length),
            )
            messageViewModel.acknowledgeDraftRestored(event.id)
        }
    }

    androidx.compose.runtime.LaunchedEffect(messages.lastOrNull()?.id) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    androidx.compose.runtime.LaunchedEffect(isPartnerTyping, messages.size) {
        if (isPartnerTyping && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    androidx.compose.runtime.LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }.drop(1).collect { isAtTop ->
            if (isAtTop && currentHasMoreMessages) {
                messageViewModel.loadOlderMessages()
            }
        }
    }

    Scaffold(
        containerColor = StarSnapColor.canvas,
        topBar = {
            TopAppBar(
                title = roomName,
                onBack = { mainNavController.popBackStack() },
                titleContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(StarSnapColor.surfaceSubtle, CircleShape)
                                .clip(CircleShape)
                                .semantics {
                                    contentDescription = "$roomName 프로필"
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = roomName.trim().take(1).ifBlank { "S" },
                                style = StarSnapTypography.label.copy(color = StarSnapColor.textSubtle),
                            )
                            if (!roomProfileImageUrl.isNullOrBlank()) {
                                GlideImage(
                                    modifier = Modifier.fillMaxSize(),
                                    imageModel = { roomProfileImageUrl },
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = roomName,
                                style = StarSnapTypography.label.copy(color = StarSnapColor.text),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.semantics {
                                    if (isPartnerTyping) liveRegion = LiveRegionMode.Polite
                                    stateDescription = roomStatusText
                                },
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isPartnerTyping) {
                                                StarSnapColor.textSubtle
                                            } else {
                                                StarSnapColor.success
                                            },
                                        ),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = roomStatusText,
                                    style = StarSnapTypography.caption.copy(
                                        color = if (isPartnerTyping) {
                                            StarSnapColor.textSubtle
                                        } else {
                                            StarSnapColor.success
                                        },
                                    ),
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                contentPadding = PaddingValues(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (historyLoading && messages.isEmpty()) {
                    items(
                        count = 7,
                        key = { "history-skeleton-$it" },
                    ) { index ->
                        MessageBubbleSkeleton(
                            index = index,
                            modifier = if (index == 0) {
                                Modifier.messageLoadingSemantics("메시지 내역")
                            } else {
                                Modifier
                            },
                        )
                    }
                } else if (historyError != null && messages.isEmpty()) {
                    item(key = "history-error") {
                        DataLoadErrorCard(
                            title = "메시지를 불러오지 못했어요",
                            description = historyError?.message.orEmpty(),
                            onRetry = messageViewModel::retryHistory,
                        )
                    }
                } else {
                    if (olderHistoryLoading) {
                        items(
                            count = 2,
                            key = { "older-history-skeleton-$it" },
                        ) { index ->
                            MessageBubbleSkeleton(
                                index = index + 1,
                                modifier = if (index == 0) {
                                    Modifier.messageLoadingSemantics("이전 메시지")
                                } else {
                                    Modifier
                                },
                            )
                        }
                    }

                    itemsIndexed(messages, key = { _, message -> message.id }) { index, message ->
                        val previousMessage = messages.getOrNull(index - 1)
                        MessageBubble(
                            message = message,
                            showSenderName = isGroupChat &&
                                !message.mine &&
                                (previousMessage == null || previousMessage.senderUserId != message.senderUserId),
                            showMessageTime = previousMessage == null ||
                                previousMessage.senderUserId != message.senderUserId ||
                                previousMessage.createdAt != message.createdAt,
                            onOpenActions = {
                                if (message.status != "DELETED") actionMessage = message
                            }
                        )
                    }
                    if (isPartnerTyping) {
                        item(key = "partner-typing") {
                            PartnerTypingBubble()
                        }
                    }
                }
            }

            if (historyError != null && messages.isNotEmpty()) {
                DataLoadErrorCard(
                    title = if (historyError?.retryOlder == true) {
                        "이전 메시지를 더 불러오지 못했어요"
                    } else {
                        "메시지를 새로고침하지 못했어요"
                    },
                    description = historyError?.message.orEmpty(),
                    onRetry = messageViewModel::retryHistory,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            if (error != null) {
                Text(
                    text = error ?: "",
                    style = StarSnapTypography.caption.copy(color = StarSnapColor.danger),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StarSnapColor.dangerSoft)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (sendCooldownRemainingSeconds > 0) {
                Text(
                    text = "${sendCooldownRemainingSeconds}초 후 다시 보낼 수 있어요.",
                    style = StarSnapTypography.caption.copy(color = StarSnapColor.danger),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StarSnapColor.surface)
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                            stateDescription = "${sendCooldownRemainingSeconds}초 남음"
                        }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            ChatInputBar(
                value = input,
                cooldownRemainingSeconds = sendCooldownRemainingSeconds,
                onValueChange = {
                    input = it
                    messageViewModel.onDraftChanged(it.text)
                },
                onFocusChanged = messageViewModel::onInputFocusChanged,
                onSend = {
                    val text = input.text
                    if (text.isNotBlank()) {
                        messageViewModel.send(text) {
                            input = TextFieldValue("")
                        }
                    }
                }
            )
        }

        actionMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { actionMessage = null },
                title = { Text("메시지", style = StarSnapTypography.title) },
                text = {
                    Text(
                        "메시지를 수정하거나 삭제할 수 있어요.",
                        style = StarSnapTypography.bodySmall,
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        editText = message.text
                        editingMessage = message
                        actionMessage = null
                    }) {
                        Text("수정", style = StarSnapTypography.label)
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            deleteMessage = message
                            actionMessage = null
                        }) {
                            Text("삭제", style = StarSnapTypography.label)
                        }
                        TextButton(onClick = { actionMessage = null }) {
                            Text("취소", style = StarSnapTypography.label)
                        }
                    }
                }
            )
        }

        editingMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { editingMessage = null },
                title = { Text("메시지 수정", style = StarSnapTypography.title) },
                text = {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = StarSnapTypography.bodySmall,
                        minLines = 3,
                        maxLines = 5
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            messageViewModel.updateMessage(message.id, editText)
                            editingMessage = null
                        },
                        enabled = editText.isNotBlank()
                    ) {
                        Text("수정", style = StarSnapTypography.label)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingMessage = null }) {
                        Text("취소", style = StarSnapTypography.label)
                    }
                }
            )
        }

        deleteMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { deleteMessage = null },
                title = { Text("메시지 삭제", style = StarSnapTypography.title) },
                text = {
                    Text(
                        "삭제한 메시지는 복구할 수 없어요.",
                        style = StarSnapTypography.bodySmall,
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        messageViewModel.deleteMessage(message.id)
                        deleteMessage = null
                    }) {
                        Text("삭제", style = StarSnapTypography.label)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteMessage = null }) {
                        Text("취소", style = StarSnapTypography.label)
                    }
                }
            )
        }
    }
}

@Composable
private fun MessageBubbleSkeleton(index: Int, modifier: Modifier = Modifier) {
    val mine = index % 3 == 2
    val bubbleWidth = when (index % 4) {
        0 -> 184.dp
        1 -> 224.dp
        2 -> 156.dp
        else -> 204.dp
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (mine) Alignment.End else Alignment.Start,
    ) {
        if (!mine && index % 2 == 0) {
            SnapSkeleton(
                modifier = Modifier
                    .width(72.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        SnapSkeleton(
            modifier = Modifier
                .width(bubbleWidth)
                .height(if (index % 2 == 0) 44.dp else 38.dp)
                .clip(RoundedCornerShape(16.dp)),
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun MessageBubble(
    message: ChatUiMessage,
    showSenderName: Boolean,
    showMessageTime: Boolean,
    onOpenActions: () -> Unit,
) {
    val horizontalAlignment = if (message.mine) Alignment.End else Alignment.Start
    val bubbleColor = if (message.mine) StarSnapColor.brand else StarSnapColor.surface
    val bubbleShape = RoundedCornerShape(
        topStart = if (message.mine) 16.dp else 4.dp,
        topEnd = if (message.mine) 4.dp else 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp,
    )
    val textColor = when {
        message.status == "DELETED" -> StarSnapColor.textMuted
        message.mine -> StarSnapColor.onBrand
        else -> StarSnapColor.text
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment,
    ) {
        if (showSenderName) {
            Text(
                text = message.senderUsername,
                style = StarSnapTypography.caption.copy(color = StarSnapColor.textSubtle),
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            if (message.mine && showMessageTime) {
                Text(
                    text = message.createdAt,
                    style = StarSnapTypography.micro.copy(color = StarSnapColor.textMuted),
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Row(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .heightIn(min = 48.dp)
                    .clip(bubbleShape)
                    .background(bubbleColor)
                    .then(
                        if (message.mine && message.status != "DELETED") {
                            Modifier.combinedClickable(
                                onClick = {},
                                onLongClickLabel = "메시지 작업 열기",
                                onLongClick = onOpenActions,
                            )
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.text,
                    style = StarSnapTypography.bodySmall.copy(color = textColor),
                )
                if (message.status == "EDITED") {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(수정됨)",
                        style = StarSnapTypography.micro.copy(color = StarSnapColor.textMuted),
                    )
                }
            }
            if (!message.mine && showMessageTime) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = message.createdAt,
                    style = StarSnapTypography.micro.copy(color = StarSnapColor.textMuted),
                )
            }
        }
    }
}

@Composable
private fun PartnerTypingBubble() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp,
                    ),
                )
                .background(StarSnapColor.surface)
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = "상대방이 메시지를 입력 중이에요"
                }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(StarSnapColor.textSubtle)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "메시지를 입력 중이에요",
                style = StarSnapTypography.caption.copy(color = StarSnapColor.textSubtle),
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    value: TextFieldValue,
    cooldownRemainingSeconds: Int,
    onValueChange: (TextFieldValue) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onSend: () -> Unit
) {
    val canSend = value.text.isNotBlank() && cooldownRemainingSeconds == 0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StarSnapColor.surface),
    ) {
        HorizontalDivider(color = StarSnapColor.border)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(StarSnapColor.surfaceSubtle)
                    .border(1.dp, StarSnapColor.border, RoundedCornerShape(24.dp))
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = StarSnapTypography.bodySmall.copy(color = StarSnapColor.text),
                    cursorBrush = SolidColor(StarSnapColor.text),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = { if (canSend) onSend() },
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "메시지 입력" }
                        .onFocusChanged { onFocusChanged(it.isFocused) },
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.text.isEmpty() && value.composition == null) {
                                Text(
                                    text = "메시지를 입력하세요...",
                                    style = StarSnapTypography.bodySmall.copy(
                                        color = StarSnapColor.textMuted,
                                    ),
                                )
                            }
                            inner()
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            val sendDescription = if (cooldownRemainingSeconds > 0) {
                "${cooldownRemainingSeconds}초 후 메시지 전송 가능"
            } else {
                "메시지 보내기"
            }
            IconButton(
                onClick = onSend,
                enabled = canSend,
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = sendDescription }
                    .clip(CircleShape)
                    .background(
                        if (canSend) StarSnapColor.brand else StarSnapColor.surfaceSubtle,
                    ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = if (canSend) StarSnapColor.onBrand else StarSnapColor.textMuted,
                )
            }
        }
    }
}
