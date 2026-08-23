package com.photo.starsnap.main.ui.screen.main.snap_list

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.photo.starsnap.designsystem.R
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.StarSnapTypography
import com.photo.starsnap.main.ui.component.SnapCommentInput
import com.photo.starsnap.main.ui.component.SnapConnectedGroups
import com.photo.starsnap.main.ui.component.SnapConnectedStars
import com.photo.starsnap.main.ui.component.SnapDivider
import com.photo.starsnap.main.ui.component.SnapIcon
import com.photo.starsnap.main.ui.component.SnapImage
import com.photo.starsnap.main.ui.component.SnapInformation
import com.photo.starsnap.main.ui.component.SnapMessages
import com.photo.starsnap.main.ui.component.SnapUser
import com.photo.starsnap.main.ui.component.TopAppBar
import com.photo.starsnap.main.viewmodel.main.SnapViewModel
import com.photo.starsnap.main.viewmodel.main.StarViewModel
import com.photo.starsnap.main.viewmodel.main.UserViewModel
import com.photo.starsnap.network.snap.dto.CommentDto

@Composable
fun SnapScreen(
    navController: NavController,
    viewModel: SnapViewModel,
    starViewModel: StarViewModel,
    userViewModel: UserViewModel,
    onNavigate: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        Log.d("화면", "SnapScreen")
    }
    val snap = viewModel.snapState.collectAsState()
    val connected = viewModel.connectedState.collectAsState().value
    val myUsername = userViewModel.userData.collectAsState().value.username
    val canEdit = myUsername.isNotBlank() && myUsername == snap.value.selectSnap?.createdUser?.username
    var menuExpanded by remember { mutableStateOf(false) }
    var deleteDialogOpen by remember { mutableStateOf(false) }
    var deleteLoading by remember { mutableStateOf(false) }
    var liked by remember(snap.value.selectSnap?.snapData?.snapId) {
        mutableStateOf(snap.value.selectSnap?.snapData?.likeState ?: false)
    }
    var likeLoading by remember { mutableStateOf(false) }

    var saved by remember(snap.value.selectSnap?.snapData?.snapId) {
        mutableStateOf(snap.value.selectSnap?.snapData?.saveState ?: false)
    }
    var saveLoading by remember { mutableStateOf(false) }

    val localComments = remember(snap.value.selectSnap?.snapData?.snapId) { mutableStateListOf<CommentDto>() }
    var commentText by remember(snap.value.selectSnap?.snapData?.snapId) { mutableStateOf("") }
    var commentSending by remember { mutableStateOf(false) }

    LaunchedEffect(snap.value.selectSnap?.snapData?.likeState) {
        liked = snap.value.selectSnap?.snapData?.likeState ?: false
    }

    LaunchedEffect(snap.value.selectSnap?.snapData?.snapId) {
        viewModel.resolveConnected(snap.value.selectSnap?.snapData?.tags ?: emptyList())
    }

    if (deleteDialogOpen) {
        AlertDialog(
            onDismissRequest = { if (!deleteLoading) deleteDialogOpen = false },
            containerColor = StarSnapColor.surface,
            titleContentColor = StarSnapColor.text,
            textContentColor = StarSnapColor.textSubtle,
            title = { Text("스냅 삭제", style = StarSnapTypography.title) },
            text = {
                Text(
                    "삭제한 스냅은 복구할 수 없어요.",
                    style = StarSnapTypography.label
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !deleteLoading,
                    onClick = {
                        val snapId = snap.value.selectSnap?.snapData?.snapId ?: return@TextButton
                        deleteLoading = true
                        viewModel.deleteSnap(
                            snapId = snapId,
                            onSuccess = {
                                deleteLoading = false
                                deleteDialogOpen = false
                                navController.popBackStack()
                            },
                            onFailure = {
                                deleteLoading = false
                            }
                        )
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = StarSnapColor.danger)
                ) { Text("삭제", style = StarSnapTypography.label) }
            },
            dismissButton = {
                TextButton(
                    enabled = !deleteLoading,
                    onClick = { deleteDialogOpen = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = StarSnapColor.textSubtle)
                ) {
                    Text("취소", style = StarSnapTypography.label)
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = stringResource(R.string.snap_top_app_bar_title),
                onBack = { navController.popBackStack() },
                actions = {
                    if (canEdit) {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "더보기")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "삭제",
                                            style = StarSnapTypography.label,
                                            color = StarSnapColor.danger
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        deleteDialogOpen = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        containerColor = StarSnapColor.canvas,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // User header (web: 상단 작성자 바 - 아바타 + 유저명 + 날짜)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SnapUser(
                    profileImageUrl = snap.value.selectSnap?.createdUser?.imageKey,
                    username = snap.value.selectSnap?.createdUser?.username ?: "",
                    createAt = snap.value.selectSnap?.snapData?.createdAt ?: "",
                    onClick = {
                        val createdUser = snap.value.selectSnap?.createdUser ?: return@SnapUser
                        val username = java.net.URLEncoder.encode(createdUser.username ?: "", "UTF-8")
                        val imageKey = java.net.URLEncoder.encode(createdUser.imageKey ?: "", "UTF-8")
                        onNavigate("user?username=$username&imageKey=$imageKey")
                    }
                )
            }

            // Snap Image
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                SnapImage(snap.value.selectSnap?.snapData?.imageKey)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                // Snap Icon(message, save, like)
                SnapIcon(
                    liked = liked,
                    likeEnabled = !likeLoading,
                    saved = saved,
                    onSaveChange = {
                        val snapId = snap.value.selectSnap?.snapData?.snapId ?: return@SnapIcon
                        if (saveLoading) return@SnapIcon

                        saveLoading = true
                        val currentlySaved = saved

                        viewModel.toggleSave(
                            snapId = snapId,
                            currentlySaved = currentlySaved,
                            onSuccess = { nowSaved ->
                                saved = nowSaved
                                saveLoading = false
                            },
                            onFailure = {
                                saved = currentlySaved
                                saveLoading = false
                            }
                        )
                    },
                    onLikeChange = {
                        val snapId = snap.value.selectSnap?.snapData?.snapId ?: return@SnapIcon
                        if (likeLoading) return@SnapIcon

                        likeLoading = true

                        viewModel.toggleLike(
                            snapId = snapId,
                            onSuccess = { linked ->
                                liked = linked
                                likeLoading = false
                            },
                            onFailure = {
                                liked = snap.value.selectSnap?.snapData?.likeState ?: liked
                                likeLoading = false
                            }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SnapInformation(
                    title = snap.value.selectSnap?.snapData?.title ?: "",
                    tags = snap.value.selectSnap?.snapData?.tags ?: emptyList(),
                    dateTaken = snap.value.selectSnap?.snapData?.dateTaken ?: "",
                    source = snap.value.selectSnap?.snapData?.source ?: ""
                )
                if (connected.starGroups.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SnapConnectedGroups(connected.starGroups)
                }
                if (connected.stars.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SnapConnectedStars(connected.stars)
                }
                Spacer(modifier = Modifier.height(16.dp))
                SnapDivider()
                Spacer(modifier = Modifier.height(16.dp))
                SnapMessages((snap.value.selectSnap?.snapData?.comments ?: emptyList()) + localComments)
                Spacer(modifier = Modifier.height(12.dp))
                SnapCommentInput(
                    value = commentText,
                    onValueChange = { commentText = it },
                    sending = commentSending,
                    onSend = {
                        val snapId = snap.value.selectSnap?.snapData?.snapId ?: return@SnapCommentInput
                        val text = commentText.trim()
                        if (text.isEmpty() || commentSending) return@SnapCommentInput

                        commentSending = true
                        viewModel.createComment(
                            snapId = snapId,
                            content = text,
                            onSuccess = { comment ->
                                localComments.add(comment)
                                commentText = ""
                                commentSending = false
                            },
                            onFailure = {
                                commentSending = false
                            }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
