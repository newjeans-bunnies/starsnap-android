package com.photo.starsnap.main.ui.screen.main.profile

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.photo.starsnap.designsystem.CustomColor
import com.photo.starsnap.designsystem.text.CustomTextStyle
import com.photo.starsnap.designsystem.text.CustomTextStyle.title5
import com.photo.starsnap.designsystem.text.CustomTextStyle.title7
import com.photo.starsnap.main.ui.component.SnapFeedCard
import com.photo.starsnap.main.ui.component.skeleton.SnapFeedSkeletonCard
import com.photo.starsnap.main.utils.NavigationRoute.SNAP
import com.photo.starsnap.main.utils.clickableSingle
import com.photo.starsnap.main.utils.constant.Constant.getImageUrl
import com.photo.starsnap.main.viewmodel.main.SnapViewModel
import com.photo.starsnap.main.viewmodel.main.UserData
import com.photo.starsnap.main.viewmodel.main.UserViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch

private enum class ProfileTab(val label: String) {
    MySnaps("내 스냅"),
    Saved("저장됨")
}

@Composable
fun ProfileScreen(
    mainNavController: NavController,
    userViewModel: UserViewModel,
    snapViewModel: SnapViewModel? = null,
    onEditProfile: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenSnap: ((String) -> Unit)? = null
) {
    LaunchedEffect(Unit) {
        Log.d("화면", "ProfileScreen")
    }

    val user by userViewModel.userData.collectAsStateWithLifecycle()
    val ownSnapsState by userViewModel.ownProfileSnapsState.collectAsStateWithLifecycle()
    val savedSnaps = snapViewModel?.savedSnaps?.collectAsStateWithLifecycle()?.value.orEmpty()
    val savedSnapsLoading = snapViewModel?.savedSnapsLoading?.collectAsStateWithLifecycle()?.value ?: false
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { ProfileTab.entries.size })
    val coroutineScope = rememberCoroutineScope()
    val selectedTab = ProfileTab.entries[pagerState.currentPage]

    LaunchedEffect(user.username) {
        userViewModel.loadOwnProfileSnaps(user.username)
    }
    LaunchedEffect(snapViewModel) {
        snapViewModel?.loadSavedSnaps()
    }

    Scaffold(
        containerColor = CustomColor.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OwnProfileHeader(
                user = user,
                modifier = Modifier.padding(16.dp),
                onEditProfile = onEditProfile,
                onOpenSettings = onOpenSettings,
                onShareProfile = {
                    context.startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "@${user.username}")
                            },
                            null
                        )
                    )
                }
            )

            ProfileTabs(
                selectedTab = selectedTab,
                onSelect = { tab ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(ProfileTab.entries.indexOf(tab))
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val tab = ProfileTab.entries[page]
                val activeSnaps = if (tab == ProfileTab.MySnaps) ownSnapsState.snaps else savedSnaps
                val isLoading = if (tab == ProfileTab.MySnaps) ownSnapsState.loading else savedSnapsLoading
                val error = if (tab == ProfileTab.MySnaps) ownSnapsState.error else null

                LazyVerticalStaggeredGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = StaggeredGridCells.Fixed(1),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp
                ) {
                    when {
                        isLoading -> {
                            items(8) { index ->
                                SnapFeedSkeletonCard(index)
                            }
                        }

                        !error.isNullOrBlank() -> item(span = StaggeredGridItemSpan.FullLine) {
                            ProfileMessage(error, CustomColor.danger)
                        }

                        activeSnaps.isEmpty() -> item(span = StaggeredGridItemSpan.FullLine) {
                            ProfileMessage("표시할 스냅이 없습니다.", CustomColor.sub_title)
                        }

                        else -> items(activeSnaps.size) { index ->
                            val snap = activeSnaps[index]
                            SnapFeedCard(
                                snap = snap,
                                onLike = { snapId -> snapViewModel?.toggleLike(snapId) },
                                onClick = {
                                    snapViewModel?.selectSnap(snap)
                                    onOpenSnap?.invoke(SNAP)
                                        ?: mainNavController.navigate(SNAP)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnProfileHeader(
    user: UserData,
    modifier: Modifier = Modifier,
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onShareProfile: () -> Unit
) {
    Column(
        modifier = modifier
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
            imageModel = { resolveProfileImage(user.profileImageUrl) },
            imageOptions = ImageOptions(
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user.username.ifBlank { "사용자" },
            style = CustomTextStyle.TitleLarge.copy(color = CustomColor.light_black),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "@${user.username.ifBlank { "사용자" }}",
            style = title5.copy(color = CustomColor.sub_title)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "스타스냅 사용자",
            style = title7.copy(color = CustomColor.sub_title)
        )

        Spacer(modifier = Modifier.height(18.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileActionButton(
                text = "프로필 수정",
                modifier = Modifier.fillMaxWidth(),
                onClick = onEditProfile
            )
            ProfileActionButton(
                text = "공유",
                modifier = Modifier.fillMaxWidth(),
                onClick = onShareProfile
            )
            ProfileActionButton(
                text = "설정",
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun ProfileTabs(
    selectedTab: ProfileTab,
    onSelect: (ProfileTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            ProfileTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .height(44.dp)
                        .clickableSingle { onSelect(tab) }
                ) {
                    Text(
                        text = tab.label,
                        style = title5.copy(
                            color = if (isSelected) CustomColor.light_black else CustomColor.muted
                        ),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                if (isSelected) CustomColor.light_black else CustomColor.background
                            )
                    )
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = CustomColor.line)
    }
}

@Composable
private fun ProfileActionButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CustomColor.surface)
            .border(1.dp, CustomColor.line, RoundedCornerShape(12.dp))
            .clickableSingle { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = title5.copy(color = CustomColor.light_black))
    }
}

@Composable
private fun ProfileMessage(message: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = message,
        style = title5.copy(color = color),
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

private fun resolveProfileImage(imageKey: String?): String? {
    val trimmed = imageKey?.trim().orEmpty()
    if (trimmed.isBlank()) return null
    return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else getImageUrl(trimmed)
}
