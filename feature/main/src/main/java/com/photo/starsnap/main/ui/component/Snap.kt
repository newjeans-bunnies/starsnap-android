package com.photo.starsnap.main.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.photo.starsnap.designsystem.R
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.StarSnapTypography
import com.photo.starsnap.main.ui.component.skeleton.SnapSkeleton
import com.photo.starsnap.main.utils.constant.Constant.getImageUrl
import com.photo.starsnap.main.utils.clickableSingle
import com.photo.starsnap.network.snap.dto.CommentDto
import com.photo.starsnap.network.snap.dto.SnapResponseDto
import com.photo.starsnap.network.star.dto.StarGroupResponseDto
import com.photo.starsnap.network.star.dto.StarResponseDto
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration

@Composable
fun Snap(snapImageUrl: String?, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(StarSnapColor.surface)
            .clickableSingle(
                onClickLabel = "스냅 상세 보기",
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        GlideImage(
            imageModel = { getImageUrl(snapImageUrl) },
            imageOptions = ImageOptions(
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.Center
            ),
            loading = { SnapSkeleton() }
        )
    }
}

// 홈 피드 카드 (web SnapCard 기준: 라운드 카드 + 이미지 + 좋아요 오버레이 + 작성자 행)
@Composable
fun SnapFeedCard(
    snap: SnapResponseDto,
    onLike: (String) -> Unit,
    onClick: () -> Unit
) {
    var liked by remember(snap.snapData.snapId) { mutableStateOf(snap.snapData.likeState) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StarSnapColor.surface)
            .border(1.dp, StarSnapColor.border, RoundedCornerShape(16.dp))
            .clickableSingle(
                onClickLabel = "스냅 상세 보기",
                role = Role.Button,
                onClick = onClick,
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(snap.snapData.imageAspectRatio ?: 1f)
        ) {
            GlideImage(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StarSnapColor.surfaceSubtle),
                imageModel = { getImageUrl(snap.snapData.imageKey) },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                ),
                loading = { SnapSkeleton() }
            )
            IconToggleButton(
                checked = liked,
                onCheckedChange = {
                    liked = it
                    onLike(snap.snapData.snapId)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(StarSnapColor.surface.copy(alpha = 0.88f)),
            ) {
                Icon(
                    imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (liked) "좋아요 취소" else "좋아요",
                    modifier = Modifier.size(18.dp),
                    tint = if (liked) StarSnapColor.danger else StarSnapColor.textSubtle,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 44.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundProfileImage(
                imageKey = snap.createdUser.imageKey,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(StarSnapColor.surfaceSubtle)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = snap.createdUser.username,
                style = StarSnapTypography.label.copy(color = StarSnapColor.text),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SnapIcon(
    liked: Boolean,
    onLikeChange: (Boolean) -> Unit,
    likeEnabled: Boolean = true,
    saved: Boolean = false,
    onSaveChange: (Boolean) -> Unit = {},
    onShare: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 좋아요 pill
        Row(
            modifier = Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (liked) StarSnapColor.dangerSoft else Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (liked) StarSnapColor.danger else StarSnapColor.border,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickableSingle(
                    enabled = likeEnabled,
                    onClickLabel = if (liked) "좋아요 취소" else "좋아요",
                    role = Role.Button,
                ) { onLikeChange(!liked) }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (liked) StarSnapColor.danger else StarSnapColor.textSubtle,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "좋아요",
                style = StarSnapTypography.label.copy(
                    color = if (liked) StarSnapColor.danger else StarSnapColor.textSubtle,
                ),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 저장 pill
        Row(
            modifier = Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (saved) StarSnapColor.brandSoft else Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (saved) StarSnapColor.brand else StarSnapColor.border,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickableSingle(
                    onClickLabel = if (saved) "저장 취소" else "저장",
                    role = Role.Button,
                ) { onSaveChange(!saved) }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.save_icon),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (saved) StarSnapColor.text else StarSnapColor.textSubtle,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "저장",
                style = StarSnapTypography.label.copy(
                    color = if (saved) StarSnapColor.text else StarSnapColor.textSubtle,
                ),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 공유
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, StarSnapColor.border, RoundedCornerShape(12.dp))
                .clickableSingle(
                    onClickLabel = "공유",
                    role = Role.Button,
                    onClick = onShare,
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "공유",
                modifier = Modifier.size(18.dp),
                tint = StarSnapColor.textSubtle,
            )
        }
    }
}

@Composable
fun SnapUserName(username: String) {
    Text(text = username, style = StarSnapTypography.caption)
}

@Composable
fun SnapTitle(title: String) {
    Text(text = title, style = StarSnapTypography.label)
}

// 스냅 상세 메인 제목 (web: 큰 bold ink)
@Composable
fun SnapDetailTitle(title: String) {
    Text(text = title, style = StarSnapTypography.heading.copy(color = StarSnapColor.text))
}

@Composable
fun SnapTag(tags: List<String>) {
    if (tags.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tagName ->
            Text(
                text = "#$tagName",
                style = StarSnapTypography.label.copy(color = StarSnapColor.textSubtle),
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(StarSnapColor.surface)
                    .border(1.dp, StarSnapColor.border, RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun SnapDateTaken(dateTaken: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.calendar_icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = StarSnapColor.textSubtle,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = dateTaken,
            style = StarSnapTypography.caption.copy(color = StarSnapColor.textSubtle),
        )
    }
}

@Composable
fun SnapSource(source: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.search_icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = StarSnapColor.textSubtle,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = source,
            style = StarSnapTypography.caption.copy(color = StarSnapColor.textSubtle),
        )
    }
}

@Composable
fun SnapCreateAt(createAt: String) {
    val formattedTime = getTimeAgo(createAt)
    Text(
        text = formattedTime,
        style = StarSnapTypography.caption.copy(color = StarSnapColor.textMuted),
    )
}

@Composable
fun SnapDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = StarSnapColor.border,
    )
}

fun getTimeAgo(createAt: String): String {
    val createdDateTime = runCatching {
        LocalDateTime.parse(createAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }.recoverCatching {
        LocalDateTime.parse(createAt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }.getOrElse {
        return "알 수 없음"
    }

    val now = LocalDateTime.now()

    val duration = Duration.between(createdDateTime, now)

    return when {
        duration.seconds < 60 -> "방금 전"
        duration.toMinutes() < 60 -> "${duration.toMinutes()}분 전"
        duration.toHours() < 24 -> "${duration.toHours()}시간 전"
        duration.toDays() < 7 -> "${duration.toDays()}일 전"
        duration.toDays() < 30 -> "${duration.toDays() / 7}주 전"
        duration.toDays() < 365 -> "${duration.toDays() / 30}달 전"
        else -> "${duration.toDays() / 365}년 전"
    }
}

@Composable
fun SnapImage(imageUrl: String?) {
    GlideImage(
        modifier = Modifier.fillMaxWidth(),
        imageModel = { getImageUrl(imageUrl) },
        imageOptions = ImageOptions(
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.Center
        ),
        loading = { SnapSkeleton() }
    )
}

@Composable
fun SnapUser(
    profileImageUrl: String?,
    username: String,
    createAt: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 44.dp)
            .padding(vertical = 4.dp)
            .clickableSingle(
                enabled = onClick != null,
                onClickLabel = "$username 프로필 보기",
                role = Role.Button,
            ) {
                onClick?.invoke()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundProfileImage(
            imageKey = profileImageUrl,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(StarSnapColor.surfaceSubtle)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = username,
                style = StarSnapTypography.label.copy(color = StarSnapColor.text),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (createAt.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                SnapCreateAt(createAt)
            }
        }
    }
}

@Composable
fun SnapInformation(
    title: String,
    tags: List<String>,
    dateTaken: String,
    source: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SnapDetailTitle(title)
        SnapTag(tags)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (dateTaken.isNotBlank()) SnapDateTaken(dateTaken)
            if (source.isNotBlank()) SnapSource(source)
        }
    }
}

// web 기준: 연결된 StarGroup 섹션 (라벨 + 가로 아바타+이름)
@Composable
fun SnapConnectedGroups(groups: List<StarGroupResponseDto>) {
    if (groups.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "StarGroup",
            style = StarSnapTypography.caption.copy(color = StarSnapColor.textMuted),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            groups.forEach { group ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GlideImage(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(StarSnapColor.surfaceSubtle, CircleShape),
                        imageModel = { getImageUrl(group.imageKey) },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = group.name,
                        style = StarSnapTypography.caption.copy(color = StarSnapColor.textSubtle),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// web 기준: 연결된 Star 섹션
@Composable
fun SnapConnectedStars(stars: List<StarResponseDto>) {
    if (stars.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Star",
            style = StarSnapTypography.caption.copy(color = StarSnapColor.textMuted),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            stars.forEach { star ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GlideImage(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(StarSnapColor.surfaceSubtle, CircleShape),
                        imageModel = { getImageUrl(star.imageKey) },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = star.name,
                        style = StarSnapTypography.caption.copy(color = StarSnapColor.textSubtle),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun SnapMessages(
    messages: List<CommentDto>?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "메시지",
            style = StarSnapTypography.label.copy(color = StarSnapColor.text),
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (messages.isNullOrEmpty()) {
            Text(
                text = "메시지가 없습니다.",
                style = StarSnapTypography.label.copy(color = StarSnapColor.textMuted),
            )
        } else {
            messages.forEach { message ->
                SnapMessage(message)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun SnapMessage(
    message: CommentDto
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        RoundProfileImage(
            imageKey = message.profileKey,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(StarSnapColor.surfaceSubtle)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.username,
                    style = StarSnapTypography.label.copy(color = StarSnapColor.text),
                )
                Spacer(modifier = Modifier.width(6.dp))
                SnapCreateAt(message.createdAt.toString())
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message.content,
                style = StarSnapTypography.label.copy(color = StarSnapColor.textSubtle),
            )
        }
    }
}

// web 기준: 댓글 입력바 (라운드 입력 + 브랜드 전송 버튼)
@Composable
fun SnapCommentInput(
    value: String,
    onValueChange: (String) -> Unit,
    sending: Boolean,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(StarSnapColor.surface)
                .border(1.dp, StarSnapColor.border, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp)
                .semantics {
                    contentDescription = "댓글 입력"
                },
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = StarSnapTypography.label.copy(color = StarSnapColor.text),
                singleLine = true,
                enabled = !sending,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = "댓글 달기...",
                            style = StarSnapTypography.label.copy(color = StarSnapColor.textMuted),
                        )
                    }
                    inner()
                }
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (value.isBlank() || sending) {
                        StarSnapColor.surfaceSubtle
                    } else {
                        StarSnapColor.brand
                    },
                )
                .clickableSingle(
                    enabled = value.isNotBlank() && !sending,
                    onClickLabel = "댓글 전송",
                    role = Role.Button,
                    onClick = onSend,
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "댓글 전송",
                modifier = Modifier.size(18.dp),
                tint = if (value.isBlank() || sending) {
                    StarSnapColor.textMuted
                } else {
                    StarSnapColor.onBrand
                },
            )
        }
    }
}

@Composable
fun SelectImage(index: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
            .background(StarSnapColor.text.copy(alpha = 0.3f))
    ){
        Text(
            text = (index + 1).toString(),
            style = StarSnapTypography.heading.copy(color = StarSnapColor.surface),
        )
    }
}

@Composable
fun SnapStar() {

}

@Composable
fun RelatedSnap(
    snap: SnapResponseDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(164.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(StarSnapColor.surface)
            .border(1.dp, StarSnapColor.border, RoundedCornerShape(14.dp))
            .clickableSingle(
                onClickLabel = "비슷한 얼굴의 스냅 상세 보기",
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio((snap.snapData.imageAspectRatio ?: 1f).coerceIn(0.8f, 1.4f))
        ) {
            GlideImage(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StarSnapColor.surfaceSubtle),
                imageModel = { getImageUrl(snap.snapData.imageKey) },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                ),
                loading = { SnapSkeleton() }
            )
        }
        Text(
            text = snap.snapData.title,
            style = StarSnapTypography.label.copy(color = StarSnapColor.text),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 10.dp, top = 10.dp, end = 10.dp)
        )
        Text(
            text = snap.createdUser.username,
            style = StarSnapTypography.caption.copy(color = StarSnapColor.textMuted),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 10.dp, top = 2.dp, end = 10.dp, bottom = 10.dp)
        )
    }
}
