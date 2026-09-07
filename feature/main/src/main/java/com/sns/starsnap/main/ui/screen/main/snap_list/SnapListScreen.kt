package com.sns.starsnap.main.ui.screen.main.snap_list

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.sns.starsnap.designsystem.StarSnapColor
import com.sns.starsnap.designsystem.text.StarSnapTypography
import com.sns.starsnap.main.ui.component.SnapFeedCard
import com.sns.starsnap.main.ui.component.skeleton.SnapFeedSkeletonCard
import com.sns.starsnap.main.ui.component.skeleton.loadingSemantics
import com.sns.starsnap.main.utils.NavigationRoute.SNAP
import com.sns.starsnap.main.utils.clickableSingle
import com.sns.starsnap.main.viewmodel.main.SnapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnapListScreen(navController: NavController, viewModel: SnapViewModel) {
    LaunchedEffect(Unit) { Log.d("화면", "SnapListScreen") }

    val snaps = viewModel.snapList.collectAsLazyPagingItems()
    var refreshing by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    LaunchedEffect(snaps.loadState.refresh) {
        if (refreshing && snaps.loadState.refresh !is LoadState.Loading) {
            refreshing = false
        }
    }

    Scaffold(
        containerColor = StarSnapColor.canvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = {
                refreshing = true
                snaps.refresh()
            },
            state = pullState,
            modifier = Modifier.padding(padding)
        ) {
            when (snaps.loadState.refresh) {
                is LoadState.Error -> {
                    FeedStateMessage(
                        title = "홈을 불러오지 못했어요",
                        description = "네트워크 상태를 확인한 뒤 다시 시도해 주세요.",
                        actionLabel = "다시 시도",
                        onAction = {
                            snaps.retry()
                            snaps.refresh()
                        }
                    )
                }

                is LoadState.NotLoading, LoadState.Loading -> {
                    LazyVerticalStaggeredGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 20.dp,
                            end = 16.dp,
                            bottom = 24.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp
                    ) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "홈",
                                    style = StarSnapTypography.headingLarge,
                                    color = StarSnapColor.text
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "팔로우한 스타들의 최신 스냅을 둘러보세요",
                                    style = StarSnapTypography.label,
                                    color = StarSnapColor.textSubtle
                                )
                                Spacer(Modifier.height(18.dp))
                                HomeCategoryChip(label = "전체")
                                Spacer(Modifier.height(12.dp))
                            }
                        }

                        if (snaps.loadState.refresh is LoadState.Loading && snaps.itemCount == 0) {
                            items(8) { index ->
                                SnapFeedSkeletonCard(
                                    index = index,
                                    modifier = if (index == 0) {
                                        Modifier.loadingSemantics("스냅 목록")
                                    } else {
                                        Modifier
                                    },
                                )
                            }
                        } else if (snaps.itemCount == 0) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                FeedStateCard(
                                    title = "아직 표시할 스냅이 없어요",
                                    description = "새로운 스냅이 등록되면 이곳에서 확인할 수 있어요."
                                )
                            }
                        } else {
                            items(snaps.itemCount) { index ->
                                val snap = snaps[index]
                                if (snap == null) {
                                    SnapFeedSkeletonCard(index)
                                } else {
                                    SnapFeedCard(
                                        snap = snap,
                                        onLike = { snapId -> viewModel.toggleLike(snapId) },
                                        onClick = {
                                            viewModel.selectSnap(snap)
                                            navController.navigate(SNAP)
                                        }
                                    )
                                }
                            }
                        }

                        if (snaps.loadState.append is LoadState.Loading) {
                            items(2) { index ->
                                SnapFeedSkeletonCard(
                                    index = snaps.itemCount + index,
                                    modifier = if (index == 0) {
                                        Modifier.loadingSemantics("추가 스냅")
                                    } else {
                                        Modifier
                                    },
                                )
                            }
                        }

                        if (snaps.loadState.append is LoadState.Error) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                FeedStateCard(
                                    title = "스냅을 더 불러오지 못했어요",
                                    description = "잠시 후 다시 시도해 주세요.",
                                    actionLabel = "다시 시도",
                                    onAction = snaps::retry
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeCategoryChip(label: String) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .clip(CircleShape)
            .background(StarSnapColor.text)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = StarSnapTypography.label,
            color = StarSnapColor.surface
        )
    }
}

@Composable
private fun FeedStateMessage(
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        FeedStateCard(title, description, actionLabel, onAction)
    }
}

@Composable
private fun FeedStateCard(
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StarSnapColor.surface)
            .border(1.dp, StarSnapColor.border, RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = StarSnapTypography.title,
            color = StarSnapColor.text
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = description,
            style = StarSnapTypography.label,
            color = StarSnapColor.textSubtle
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StarSnapColor.brand)
                    .clickableSingle(onClick = onAction)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionLabel,
                    style = StarSnapTypography.label,
                    color = StarSnapColor.onBrand
                )
            }
        }
    }
}
