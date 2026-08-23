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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.photo.starsnap.designsystem.CustomColor
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.CustomTextStyle
import com.photo.starsnap.designsystem.text.StarSnapFontSize
import com.photo.starsnap.main.ui.component.TopAppBar
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
    val roomParticipantCount = selectedRoom?.let(messageViewModel::roomParticipantCount) ?: 0
    val isGroupChat = roomParticipantCount > 1

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
                                .size(32.dp)
                                .background(CustomColor.light_gray, CircleShape)
                                .clip(CircleShape)
                        ) {
                            if (roomProfileImageUrl != null) {
                                GlideImage(
                                    modifier = Modifier.fillMaxSize(),
                                    imageModel = { roomProfileImageUrl }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = roomName,
                                style = CustomTextStyle.title2,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isPartnerTyping) "메시지를 입력 중이에요" else "${roomParticipantCount}명 참여 중",
                                style = CustomTextStyle.hint2.copy(
                                    color = if (isPartnerTyping) CustomColor.sub_title else CustomColor.gray
                                ),
                                maxLines = 1
                            )
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
                        onLongClick = {
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

            if (error != null) {
                Text(
                    text = error ?: "",
                    style = CustomTextStyle.title4.copy(color = CustomColor.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StarSnapColor.dangerSoft)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (sendCooldownRemainingSeconds > 0) {
                Text(
                    text = "${sendCooldownRemainingSeconds}초 후 다시 보낼 수 있어요.",
                    style = CustomTextStyle.title4.copy(color = StarSnapColor.textMuted),
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
                title = { Text("메시지") },
                text = { Text("메시지를 수정하거나 삭제할 수 있어요.") },
                confirmButton = {
                    TextButton(onClick = {
                        editText = message.text
                        editingMessage = message
                        actionMessage = null
                    }) {
                        Text("수정")
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            deleteMessage = message
                            actionMessage = null
                        }) {
                            Text("삭제")
                        }
                        TextButton(onClick = { actionMessage = null }) {
                            Text("취소")
                        }
                    }
                }
            )
        }

        editingMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { editingMessage = null },
                title = { Text("메시지 수정") },
                text = {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier.fillMaxWidth(),
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
                        Text("수정")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingMessage = null }) {
                        Text("취소")
                    }
                }
            )
        }

        deleteMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { deleteMessage = null },
                title = { Text("메시지 삭제") },
                text = { Text("삭제한 메시지는 복구할 수 없어요.") },
                confirmButton = {
                    TextButton(onClick = {
                        messageViewModel.deleteMessage(message.id)
                        deleteMessage = null
                    }) {
                        Text("삭제")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteMessage = null }) {
                        Text("취소")
                    }
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun MessageBubble(
    message: ChatUiMessage,
    showSenderName: Boolean,
    showMessageTime: Boolean,
    onLongClick: () -> Unit,
) {
    val horizontalAlignment = if (message.mine) Alignment.End else Alignment.Start
    val bubbleColor = if (message.mine) StarSnapColor.brand else StarSnapColor.surface
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
                style = CustomTextStyle.hint2.copy(color = CustomColor.gray),
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            if (message.mine && showMessageTime) {
                Text(
                    text = message.createdAt,
                    style = CustomTextStyle.hint2.copy(color = CustomColor.gray)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Row(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bubbleColor)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .then(
                        if (message.mine && message.status != "DELETED") {
                            Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = onLongClick
                            )
                        } else {
                            Modifier
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.text,
                    style = CustomTextStyle.title2.copy(color = textColor)
                )
                if (message.status == "EDITED") {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(수정됨)",
                        style = CustomTextStyle.hint2.copy(
                            color = CustomColor.gray,
                            fontSize = StarSnapFontSize.xs,
                        )
                    )
                }
            }
            if (!message.mine && showMessageTime) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = message.createdAt,
                    style = CustomTextStyle.hint2.copy(color = CustomColor.gray)
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
                .clip(RoundedCornerShape(16.dp))
                .background(StarSnapColor.surface)
                .border(1.dp, StarSnapColor.border, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(CustomColor.light_gray)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "메시지를 입력 중이에요",
                style = CustomTextStyle.hint1
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StarSnapColor.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(StarSnapColor.surfaceSubtle)
                .border(1.dp, StarSnapColor.border, RoundedCornerShape(24.dp))
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = CustomTextStyle.title2.copy(color = CustomColor.light_black),
                cursorBrush = SolidColor(CustomColor.light_black),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { onFocusChanged(it.isFocused) },
                decorationBox = { inner ->
                    if (value.text.isEmpty() && value.composition == null) {
                        Text(text = "메시지를 입력하세요...", style = CustomTextStyle.hint1)
                    }
                    inner()
                }
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            enabled = canSend,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (canSend) StarSnapColor.brand else StarSnapColor.surfaceSubtle
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = if (cooldownRemainingSeconds > 0) {
                    "${cooldownRemainingSeconds}초 후 메시지 전송 가능"
                } else {
                    "메시지 보내기"
                },
                tint = if (canSend) StarSnapColor.onBrand else StarSnapColor.textMuted
            )
        }
    }
}
