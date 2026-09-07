package com.sns.starsnap.main.ui.screen.main.profile

import android.util.Log
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.sns.starsnap.designsystem.CustomColor
import com.sns.starsnap.designsystem.StarSnapColor
import com.sns.starsnap.designsystem.text.CustomTextStyle
import com.sns.starsnap.designsystem.text.CustomTextStyle.title5
import com.sns.starsnap.designsystem.text.CustomTextStyle.title7
import com.sns.starsnap.main.ui.component.DataLoadErrorCard
import com.sns.starsnap.main.ui.component.SnapFeedCard
import com.sns.starsnap.main.ui.component.skeleton.ProfileHeaderSkeleton
import com.sns.starsnap.main.ui.component.skeleton.SnapFeedSkeletonCard
import com.sns.starsnap.main.ui.component.skeleton.loadingSemantics
import com.sns.starsnap.main.utils.NavigationRoute.SNAP
import com.sns.starsnap.main.utils.clickableSingle
import com.sns.starsnap.main.utils.constant.Constant.getImageUrl
import com.sns.starsnap.main.viewmodel.main.FriendRelation
import com.sns.starsnap.main.viewmodel.main.SnapViewModel
import com.sns.starsnap.main.viewmodel.main.UserViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage

private data class UserReportReason(
    val id: String,
    val label: String,
    val detail: String,
)

private val USER_REPORT_REASONS = listOf(
    UserReportReason("impersonation", "사칭 계정", "다른 사람, 기관, 브랜드를 사칭한 계정입니다."),
    UserReportReason("spam", "스팸/광고", "반복적인 광고 또는 스팸성 활동을 하는 계정입니다."),
    UserReportReason("abuse", "모욕/비방", "타인을 모욕하거나 공격하는 행위를 하는 계정입니다."),
    UserReportReason("privacy", "개인정보 침해", "개인정보를 무단으로 노출하거나 악용하는 계정입니다."),
    UserReportReason("other", "기타", "운영정책 위반이 의심되는 기타 사유입니다."),
)

@Composable
fun UserScreen(
    mainNavController: NavHostController,
    userViewModel: UserViewModel,
    snapViewModel: SnapViewModel,
    initialUsername: String,
    initialImageKey: String,
    onOpenMessages: (String) -> Unit = {},
) {
    LaunchedEffect(initialUsername) {
        Log.d("화면", "UserScreen")
        userViewModel.loadUserScreen(initialUsername)
    }

    val state by userViewModel.userScreenState.collectAsStateWithLifecycle()
    val profile = state.profile
    val requestedUsername = initialUsername.trim()
    val stateMatchesRequestedUser = state.requestedUsername == requestedUsername &&
        (profile == null || profile.username.trim().equals(requestedUsername, ignoreCase = true))
    val showUserLoading = state.loading || !stateMatchesRequestedUser
    val displayName = profile?.username?.ifBlank { initialUsername } ?: initialUsername
    val displayImage = profile?.profileImageUrl?.takeUnless { it.isNullOrBlank() } ?: initialImageKey
    val isPrivateLocked = profile?.isPrivate == true && !state.isSelf && state.relation != FriendRelation.FRIEND
    var reportDialogOpen by remember { mutableStateOf(false) }
    var selectedReportReason by remember { mutableStateOf<UserReportReason?>(null) }

    Scaffold(
        containerColor = CustomColor.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            columns = StaggeredGridCells.Fixed(1),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                if (showUserLoading) {
                    ProfileHeaderSkeleton(
                        modifier = Modifier.loadingSemantics("사용자 프로필"),
                        actionRows = 1,
                    )
                } else {
                    UserHeaderCard(
                        name = displayName.ifBlank { "사용자" },
                        username = "@${displayName.ifBlank { initialUsername.ifBlank { "사용자" } }}",
                        subtitle = "스타스냅 사용자",
                        imageKey = displayImage,
                        relation = state.relation,
                        friendActionSubmitting = state.friendActionSubmitting,
                        showActions = !state.isSelf && !profile?.userId.isNullOrBlank(),
                        showMessage = !isPrivateLocked,
                        onFriendAction = { userViewModel.handleFriendAction() },
                        onAcceptFriendRequest = { userViewModel.acceptFriendRequestOnUserScreen() },
                        onRejectFriendRequest = { userViewModel.rejectFriendRequestOnUserScreen() },
                        onMessage = {
                            val username = profile?.username?.ifBlank { initialUsername } ?: initialUsername
                            if (username.isNotBlank()) onOpenMessages(username)
                        },
                        reportSubmitting = state.reportSubmitting,
                        onReport = {
                            userViewModel.clearUserReportError()
                            selectedReportReason = null
                            reportDialogOpen = true
                        },
                    )
                }
            }

            when {
                showUserLoading -> {
                    items(8) { index ->
                        SnapFeedSkeletonCard(index)
                    }
                }

                isPrivateLocked -> {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        PrivateAccountLockedNotice(displayName = displayName.ifBlank { "사용자" })
                    }
                }

                !state.error.isNullOrBlank() -> {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        DataLoadErrorCard(
                            title = "프로필을 불러오지 못했어요",
                            description = state.error ?: "잠시 후 다시 시도해 주세요.",
                            onRetry = { userViewModel.loadUserScreen(initialUsername) },
                        )
                    }
                }

                state.snaps.isEmpty() -> {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Text(
                            text = "표시할 스냅이 없습니다.",
                            style = title5.copy(color = CustomColor.sub_title)
                        )
                    }
                }

                else -> {
                    items(state.snaps.size) { index ->
                        val snap = state.snaps[index]
                        SnapFeedCard(
                            snap = snap,
                            onLike = { snapId -> snapViewModel.toggleLike(snapId) },
                            onClick = {
                                snapViewModel.selectSnap(snap)
                                mainNavController.navigate(SNAP)
                            }
                        )
                    }
                }
            }
        }

        if (reportDialogOpen && !state.isSelf) {
            AlertDialog(
                onDismissRequest = {
                    if (state.reportSubmitting) return@AlertDialog
                    reportDialogOpen = false
                    selectedReportReason = null
                    userViewModel.clearUserReportError()
                },
                title = {
                    Text(
                        text = "유저 신고하기",
                        style = title5.copy(color = CustomColor.light_black)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "사유를 선택해 신고를 접수해주세요.",
                            style = title7.copy(color = CustomColor.sub_title)
                        )

                        USER_REPORT_REASONS.forEach { reason ->
                            val selected = selectedReportReason?.id == reason.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) CustomColor.brand.copy(alpha = 0.15f) else CustomColor.surface)
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) CustomColor.brand else CustomColor.line,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(enabled = !state.reportSubmitting) {
                                        selectedReportReason = reason
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = reason.label,
                                        style = title5.copy(color = CustomColor.light_black)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = reason.detail,
                                        style = title7.copy(color = CustomColor.sub_title)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${displayName.ifBlank { "사용자" }} 님 신고하기",
                            style = title5.copy(color = CustomColor.light_black)
                        )

                        if (!state.reportError.isNullOrBlank()) {
                            Text(
                                text = state.reportError.orEmpty(),
                                style = title7.copy(color = CustomColor.danger)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = selectedReportReason != null && !state.reportSubmitting,
                        onClick = {
                            val reason = selectedReportReason ?: return@TextButton
                            userViewModel.reportUserOnUserScreen(
                                explanation = "${reason.label} - ${reason.detail}",
                                onSuccess = {
                                    reportDialogOpen = false
                                    selectedReportReason = null
                                }
                            )
                        }
                    ) {
                        Text(if (state.reportSubmitting) "신고 접수 중..." else "신고하기")
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !state.reportSubmitting,
                        onClick = {
                            reportDialogOpen = false
                            selectedReportReason = null
                            userViewModel.clearUserReportError()
                        }
                    ) {
                        Text("취소")
                    }
                }
            )
        }
    }
}

@Composable
private fun UserHeaderCard(
    name: String,
    username: String,
    subtitle: String,
    imageKey: String?,
    relation: FriendRelation,
    friendActionSubmitting: Boolean,
    reportSubmitting: Boolean,
    showActions: Boolean,
    showMessage: Boolean,
    onFriendAction: () -> Unit,
    onAcceptFriendRequest: () -> Unit,
    onRejectFriendRequest: () -> Unit,
    onMessage: () -> Unit,
    onReport: () -> Unit,
) {
    var actionMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CustomColor.surface)
            .border(1.dp, CustomColor.line, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        GlideImage(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(CustomColor.placeholder, CircleShape),
            imageModel = { resolveProfileImage(imageKey) },
            imageOptions = ImageOptions(
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = name, style = CustomTextStyle.TitleLarge.copy(color = CustomColor.light_black))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = username, style = title5.copy(color = CustomColor.sub_title))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = subtitle, style = title7.copy(color = CustomColor.sub_title))

        if (showActions) {
            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (relation) {
                        FriendRelation.NONE -> {
                            FriendActionButton(
                                text = if (friendActionSubmitting) "처리 중..." else "친구 추가",
                                modifier = Modifier.weight(1f),
                                filled = true,
                                enabled = !friendActionSubmitting,
                                onClick = onFriendAction,
                            )
                        }

                        FriendRelation.REQUEST_SENT -> {
                            FriendActionButton(
                                text = if (friendActionSubmitting) "처리 중..." else "요청 취소",
                                modifier = Modifier.weight(1f),
                                filled = false,
                                enabled = !friendActionSubmitting,
                                onClick = onFriendAction,
                            )
                        }

                        FriendRelation.REQUEST_RECEIVED -> {
                            FriendActionButton(
                                text = if (friendActionSubmitting) "처리 중..." else "수락",
                                modifier = Modifier.weight(1f),
                                filled = true,
                                enabled = !friendActionSubmitting,
                                onClick = onAcceptFriendRequest,
                            )
                            FriendActionButton(
                                text = if (friendActionSubmitting) "처리 중..." else "거절",
                                modifier = Modifier.weight(1f),
                                filled = false,
                                enabled = !friendActionSubmitting,
                                onClick = onRejectFriendRequest,
                            )
                        }

                        FriendRelation.FRIEND -> {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CustomColor.surface)
                                    .border(1.dp, CustomColor.line, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "친구", style = title5.copy(color = CustomColor.light_black))
                            }
                        }
                    }
                }

                if (showMessage) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CustomColor.surface)
                            .border(1.dp, CustomColor.line, RoundedCornerShape(10.dp))
                            .clickableSingle { onMessage() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "메시지", style = title5.copy(color = CustomColor.light_black))
                    }
                }

                Box {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CustomColor.surface)
                            .border(1.dp, CustomColor.line, RoundedCornerShape(10.dp))
                            .clickableSingle { actionMenuExpanded = !actionMenuExpanded },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "유저 메뉴",
                            tint = CustomColor.sub_title,
                        )
                    }

                    DropdownMenu(
                        expanded = actionMenuExpanded,
                        onDismissRequest = { actionMenuExpanded = false }
                    ) {
                        if (relation == FriendRelation.FRIEND) {
                            DropdownMenuItem(
                                text = { Text("친구 끊기") },
                                enabled = !friendActionSubmitting,
                                onClick = {
                                    actionMenuExpanded = false
                                    onFriendAction()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("신고하기") },
                            enabled = !reportSubmitting,
                            onClick = {
                                actionMenuExpanded = false
                                onReport()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendActionButton(
    text: String,
    modifier: Modifier = Modifier,
    filled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (filled) CustomColor.brand else CustomColor.surface)
            .border(
                width = 1.dp,
                color = if (filled) CustomColor.brand else CustomColor.line,
                shape = RoundedCornerShape(10.dp)
            )
            .clickableSingle(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = title5.copy(
                color = if (filled) StarSnapColor.onBrand else CustomColor.light_black
            )
        )
    }
}

@Composable
private fun PrivateAccountLockedNotice(displayName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = CustomColor.sub_title,
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "비공개 계정입니다",
            style = title5.copy(color = CustomColor.light_black),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${displayName}님과 친구가 되면 스냅을 볼 수 있어요.",
            style = title7.copy(color = CustomColor.sub_title),
        )
    }
}

private fun resolveProfileImage(imageKey: String?): String? {
    val trimmed = imageKey?.trim().orEmpty()
    if (trimmed.isBlank()) return null
    return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else getImageUrl(trimmed)
}
