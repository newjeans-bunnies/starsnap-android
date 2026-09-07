package com.sns.starsnap.main.ui.screen.main.upload

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.sns.starsnap.designsystem.StarSnapColor
import com.sns.starsnap.designsystem.text.StarSnapTypography
import com.sns.starsnap.main.ui.component.skeleton.StarGroupSkeletonCard
import com.sns.starsnap.main.ui.component.skeleton.loadingSemantics
import com.sns.starsnap.main.utils.NavigationRoute
import com.sns.starsnap.main.utils.clickableSingle
import com.sns.starsnap.main.utils.constant.Constant.getImageUrl
import com.sns.starsnap.main.viewmodel.main.UploadViewModel
import com.sns.starsnap.network.star.dto.StarGroupResponseDto
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun PickStarGroupScreen(navController: NavController, uploadViewModel: UploadViewModel) {
    var searchStarGroupName by remember { mutableStateOf("") }
    val selectedStarGroups by uploadViewModel.selectedStarGroups.collectAsStateWithLifecycle()
    val starGroupList = uploadViewModel
        .starGroupList(searchStarGroupName)
        .collectAsLazyPagingItems()
    val gridState = rememberLazyGridState()

    Scaffold(
        containerColor = StarSnapColor.canvas,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StarSnapColor.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "스타그룹 선택",
                        style = StarSnapTypography.heading,
                        color = StarSnapColor.text,
                        modifier = Modifier.weight(1f)
                    )
                    PickerDoneButton(label = "확인 (${selectedStarGroups.size})") {
                        navController.navigate(NavigationRoute.SET_SNAP) {
                            launchSingleTop = true
                            popUpTo(NavigationRoute.PICK_STAR_GROUP) { inclusive = true }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                SearchTextField("스타그룹 검색") { searchStarGroupName = it }
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            state = gridState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                starGroupList.loadState.refresh is LoadState.Loading -> {
                    items(8) { index ->
                        StarGroupSkeletonCard(
                            height = 132.dp,
                            modifier = if (index == 0) {
                                Modifier.loadingSemantics("스타그룹 선택 목록")
                            } else {
                                Modifier
                            },
                        )
                    }
                }

                starGroupList.loadState.refresh is LoadState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        PickerStateCard(
                            title = "스타그룹 목록을 불러오지 못했어요",
                            actionLabel = "다시 시도",
                            onAction = starGroupList::retry
                        )
                    }
                }

                starGroupList.itemCount == 0 -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        PickerStateCard(title = "표시할 스타그룹이 없어요")
                    }
                }

                else -> {
                    items(starGroupList.itemCount) { index ->
                        val group = starGroupList[index]
                        if (group == null) {
                            StarGroupSkeletonCard(height = 132.dp)
                        } else {
                            StarGroupItem(
                                starGroup = group,
                                selected = selectedStarGroups.any { it.id == group.id },
                                onClick = { uploadViewModel.selectedStarGroup(group) }
                            )
                        }
                    }
                }
            }

            if (starGroupList.loadState.append is LoadState.Loading) {
                items(2) { index ->
                    StarGroupSkeletonCard(
                        height = 132.dp,
                        modifier = if (index == 0) {
                            Modifier.loadingSemantics("추가 스타그룹 선택 항목")
                        } else {
                            Modifier
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun StarGroupItem(
    starGroup: StarGroupResponseDto,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(shape)
            .background(if (selected) StarSnapColor.brandSoft else StarSnapColor.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) StarSnapColor.brandActive else StarSnapColor.border,
                shape = shape
            )
            .clickableSingle(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlideImage(
                modifier = Modifier
                    .size(width = 72.dp, height = 64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StarSnapColor.surfaceSubtle),
                imageModel = { getImageUrl(starGroup.imageKey) },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = starGroup.name,
                style = StarSnapTypography.body.copy(color = StarSnapColor.text),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(StarSnapColor.brand),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "선택됨",
                    modifier = Modifier.size(15.dp),
                    tint = StarSnapColor.onBrand
                )
            }
        }
    }
}
