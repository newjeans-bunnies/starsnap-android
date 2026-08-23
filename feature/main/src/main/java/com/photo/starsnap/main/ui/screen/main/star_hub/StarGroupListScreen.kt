package com.photo.starsnap.main.ui.screen.main.star_hub

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.StarSnapTypography
import com.photo.starsnap.main.ui.component.skeleton.StarGroupSkeletonCard
import com.photo.starsnap.main.utils.clickableSingle
import com.photo.starsnap.main.utils.constant.Constant.getImageUrl
import com.photo.starsnap.main.viewmodel.main.StarViewModel
import com.photo.starsnap.network.star.dto.StarGroupResponseDto
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun StarGroupListScreen(
    rootNavController: NavController,
    starViewModel: StarViewModel,
    onNavigate: (String) -> Unit
) {
    val starGroupList = starViewModel.starGroupList.collectAsLazyPagingItems()
    LaunchedEffect(Unit) { Log.d("화면", "StarGroupListScreen") }

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            starGroupList.loadState.refresh is LoadState.Loading && starGroupList.itemCount == 0 -> {
                items(8) { StarGroupSkeletonCard(height = 192.dp) }
            }

            starGroupList.loadState.refresh is LoadState.Error && starGroupList.itemCount == 0 -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EntityListStateCard(
                        title = "스타그룹 목록을 불러오지 못했어요",
                        description = "네트워크 상태를 확인한 뒤 다시 시도해 주세요.",
                        actionLabel = "다시 시도",
                        onAction = starGroupList::retry
                    )
                }
            }

            starGroupList.itemCount == 0 -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EntityListStateCard(
                        title = "표시할 스타그룹이 없어요",
                        description = "다른 검색어로 다시 찾아보세요."
                    )
                }
            }

            else -> {
                items(starGroupList.itemCount) { index ->
                    val group = starGroupList[index]
                    if (group == null) {
                        StarGroupSkeletonCard(height = 192.dp)
                    } else {
                        StarGroupItem(group) {
                            starViewModel.selectStarGroup(group)
                            onNavigate("star_group")
                        }
                    }
                }
            }
        }

        if (starGroupList.loadState.append is LoadState.Loading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = StarSnapColor.brandActive,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

@Composable
fun StarGroupItem(starGroup: StarGroupResponseDto, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(192.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StarSnapColor.surfaceSubtle)
            .clickableSingle(onClick = onClick)
    ) {
        GlideImage(
            imageModel = { getImageUrl(starGroup.imageKey) },
            imageOptions = ImageOptions(
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            StarSnapColor.text.copy(alpha = 0f),
                            StarSnapColor.text.copy(alpha = 0.76f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = starGroup.name,
                style = StarSnapTypography.title.copy(fontWeight = FontWeight.Bold),
                color = StarSnapColor.surface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "데뷔 ${starGroup.debutDate?.ifBlank { "-" } ?: "-"}",
                style = StarSnapTypography.caption,
                color = StarSnapColor.surface.copy(alpha = 0.78f)
            )
        }
    }
}
