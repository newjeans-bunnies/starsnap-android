package com.photo.starsnap.main.ui.screen.main.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.photo.starsnap.designsystem.R
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.StarSnapTypography
import com.photo.starsnap.main.ui.component.SnapFeedCard
import com.photo.starsnap.main.ui.component.skeleton.SnapFeedSkeletonCard
import com.photo.starsnap.main.utils.NavigationRoute.SNAP
import com.photo.starsnap.main.utils.clickableSingle
import com.photo.starsnap.main.viewmodel.main.SnapViewModel

@Composable
fun SearchScreen(
    mainNavController: NavController,
    snapViewModel: SnapViewModel
) {
    LaunchedEffect(Unit) { Log.d("화면", "SearchScreen") }

    val snaps = snapViewModel.snapList.collectAsLazyPagingItems()
    var query by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf("전체") }
    val normalizedQuery = query.trim().lowercase()
    val loadedSnaps = snaps.itemSnapshotList.items
    val visibleSnaps = remember(normalizedQuery, loadedSnaps) {
        if (normalizedQuery.isEmpty()) {
            loadedSnaps
        } else {
            loadedSnaps.filter { snap ->
                snap.snapData.title.lowercase().contains(normalizedQuery) ||
                    snap.createdUser.username.lowercase().contains(normalizedQuery) ||
                    snap.snapData.tags.any { tag -> tag.lowercase().contains(normalizedQuery) }
            }
        }
    }
    val popularKeywords = remember(loadedSnaps) {
        loadedSnaps
            .flatMap { it.snapData.tags }
            .map { it.removePrefix("#").trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(5)
    }

    LazyVerticalStaggeredGrid(
        modifier = Modifier
            .fillMaxSize()
            .background(StarSnapColor.canvas),
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
                    text = "탐색",
                    style = StarSnapTypography.headingLarge,
                    color = StarSnapColor.text
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "스타, 유저, 스냅을 검색해보세요",
                    style = StarSnapTypography.label,
                    color = StarSnapColor.textSubtle
                )
                Spacer(Modifier.height(20.dp))
                SnapSearchTopBar(
                    onQueryChange = { query = it },
                    onClear = { query = "" }
                )

                if (query.isBlank() && popularKeywords.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "인기 검색어",
                            style = StarSnapTypography.label,
                            color = StarSnapColor.textMuted
                        )
                        popularKeywords.forEach { keyword ->
                            SearchKeywordChip(keyword) { query = keyword }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("전체", "스냅").forEach { label ->
                        SearchCategoryChip(
                            label = label,
                            selected = tab == label,
                            onClick = { tab = label }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        when {
            snaps.loadState.refresh is LoadState.Loading && loadedSnaps.isEmpty() -> {
                items(8) { index -> SnapFeedSkeletonCard(index) }
            }

            snaps.loadState.refresh is LoadState.Error && loadedSnaps.isEmpty() -> {
                item(span = StaggeredGridItemSpan.FullLine) {
                    SearchStateCard(
                        title = "검색 결과를 불러오지 못했어요",
                        description = "네트워크 상태를 확인한 뒤 다시 시도해 주세요.",
                        actionLabel = "다시 시도",
                        onAction = snaps::retry
                    )
                }
            }

            query.isBlank() && snaps.itemCount > 0 -> {
                items(snaps.itemCount) { index ->
                    val snap = snaps[index]
                    if (snap == null) {
                        SnapFeedSkeletonCard(index)
                    } else {
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

            visibleSnaps.isEmpty() -> {
                item(span = StaggeredGridItemSpan.FullLine) {
                    SearchStateCard(
                        title = if (query.isBlank()) {
                            "표시할 스냅이 없어요"
                        } else {
                            "'${query.trim()}'에 대한 검색 결과가 없어요"
                        },
                        description = if (query.isBlank()) {
                            "새로운 스냅이 등록되면 이곳에서 확인할 수 있어요."
                        } else {
                            "다른 검색어로 다시 찾아보세요."
                        }
                    )
                }
            }

            else -> {
                items(visibleSnaps.size) { index ->
                    val snap = visibleSnaps[index]
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

        if (query.isBlank() && snaps.loadState.append is LoadState.Loading) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = StarSnapColor.brandActive,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        if (query.isBlank() && snaps.loadState.append is LoadState.Error) {
            item(span = StaggeredGridItemSpan.FullLine) {
                SearchStateCard(
                    title = "스냅을 더 불러오지 못했어요",
                    description = "잠시 후 다시 시도해 주세요.",
                    actionLabel = "다시 시도",
                    onAction = snaps::retry
                )
            }
        }
    }
}

@Composable
fun SnapSearchTopBar(
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit = {}
) {
    var text by remember { mutableStateOf("") }

    BasicTextField(
        value = text,
        onValueChange = { input ->
            if (input.length <= 100) {
                text = input
                onQueryChange(input)
            }
        },
        textStyle = StarSnapTypography.label.copy(color = StarSnapColor.text),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .background(StarSnapColor.surface)
            .border(1.dp, StarSnapColor.border, CircleShape),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = ImageVector.vectorResource(R.drawable.search_icon),
                    contentDescription = null,
                    tint = StarSnapColor.textMuted
                )
                Spacer(Modifier.size(12.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "스타, 유저, 스냅 검색",
                            style = StarSnapTypography.label,
                            color = StarSnapColor.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
                if (text.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            text = ""
                            onQueryChange("")
                            onClear()
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "검색어 지우기",
                            modifier = Modifier.size(18.dp),
                            tint = StarSnapColor.textMuted
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun SearchKeywordChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .clip(CircleShape)
            .background(StarSnapColor.surfaceSubtle)
            .border(1.dp, StarSnapColor.border, CircleShape)
            .clickableSingle(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = StarSnapTypography.label, color = StarSnapColor.textSubtle)
    }
}

@Composable
private fun SearchCategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .clip(CircleShape)
            .background(if (selected) StarSnapColor.text else StarSnapColor.surface)
            .border(
                width = 1.dp,
                color = if (selected) StarSnapColor.text else StarSnapColor.border,
                shape = CircleShape
            )
            .clickableSingle(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = StarSnapTypography.label,
            color = if (selected) StarSnapColor.surface else StarSnapColor.textSubtle
        )
    }
}

@Composable
private fun SearchStateCard(
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
        Text(title, style = StarSnapTypography.title, color = StarSnapColor.text)
        Spacer(Modifier.height(6.dp))
        Text(
            description,
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
                Text(actionLabel, style = StarSnapTypography.label, color = StarSnapColor.onBrand)
            }
        }
    }
}
