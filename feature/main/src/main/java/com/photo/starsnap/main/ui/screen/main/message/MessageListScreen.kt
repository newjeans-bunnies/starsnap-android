package com.photo.starsnap.main.ui.screen.main.message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.StarSnapTypography
import com.photo.starsnap.main.ui.component.TopAppBar
import com.photo.starsnap.main.utils.constant.Constant
import com.photo.starsnap.main.viewmodel.main.MessageViewModel
import com.photo.starsnap.network.message.dto.ChatRoomSummaryDto
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun MessageListScreen(
    mainNavController: NavHostController,
    messageViewModel: MessageViewModel,
) {
    val rooms by messageViewModel.rooms.collectAsStateWithLifecycle()
    val roomPreviews by messageViewModel.roomPreviews.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val visibleRooms = remember(rooms, roomPreviews, query) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            rooms
        } else {
            rooms.filter { room ->
                messageViewModel.roomDisplayName(room).contains(normalizedQuery, ignoreCase = true) ||
                    roomPreviews[room.roomId].orEmpty().contains(normalizedQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        messageViewModel.start()
        messageViewModel.refreshRooms()
    }

    Scaffold(
        containerColor = StarSnapColor.canvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(title = "메시지", onBack = { mainNavController.popBackStack() })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            MessageSearchField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )

            if (visibleRooms.isEmpty()) {
                EmptyMessageState(hasQuery = query.isNotBlank())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(visibleRooms, key = { it.roomId }) { room ->
                        RoomRow(
                            room = room,
                            roomName = messageViewModel.roomDisplayName(room),
                            roomImageUrl = messageViewModel.roomProfileImageUrl(room)
                                ?.let(Constant::getImageUrl),
                            preview = roomPreviews[room.roomId],
                        ) {
                            messageViewModel.selectRoom(room)
                            mainNavController.navigate("message_chat")
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun MessageSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(StarSnapColor.surface)
            .border(1.dp, StarSnapColor.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "대화 검색",
            tint = StarSnapColor.textMuted,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            cursorBrush = SolidColor(StarSnapColor.text),
            textStyle = StarSnapTypography.bodySmall,
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = "대화 검색",
                            style = StarSnapTypography.bodySmall.copy(color = StarSnapColor.textMuted),
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun EmptyMessageState(hasQuery: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StarSnapColor.surface)
                .border(1.dp, StarSnapColor.border, RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (hasQuery) "검색 결과가 없어요." else "아직 대화가 없어요.",
                style = StarSnapTypography.title,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (hasQuery) {
                    "다른 이름이나 메시지로 검색해 보세요."
                } else {
                    "친구 프로필에서 메시지를 시작할 수 있어요."
                },
                style = StarSnapTypography.bodySmall.copy(color = StarSnapColor.textSubtle),
            )
        }
    }
}

@Composable
private fun RoomRow(
    room: ChatRoomSummaryDto,
    roomName: String,
    roomImageUrl: String?,
    preview: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StarSnapColor.surface)
            .border(1.dp, StarSnapColor.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlideImage(
            modifier = Modifier
                .size(48.dp)
                .background(StarSnapColor.surfaceSubtle, CircleShape)
                .clip(CircleShape),
            imageModel = { roomImageUrl },
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = roomName,
                    style = StarSnapTypography.label.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = room.lastMessageAt.substringAfter("T", room.lastMessageAt).take(5),
                    style = StarSnapTypography.caption.copy(color = StarSnapColor.textMuted),
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = preview?.ifBlank { "대화를 시작해보세요." } ?: "대화를 시작해보세요.",
                style = StarSnapTypography.bodySmall.copy(color = StarSnapColor.textSubtle),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
