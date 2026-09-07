package com.sns.starsnap.main.ui.screen.main.star_hub

import android.util.Log
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.sns.starsnap.designsystem.StarSnapColor
import com.sns.starsnap.designsystem.text.StarSnapTypography
import com.sns.starsnap.main.ui.component.skeleton.StarListSkeletonCard
import com.sns.starsnap.main.ui.component.skeleton.loadingSemantics
import com.sns.starsnap.main.utils.clickableSingle
import com.sns.starsnap.main.utils.constant.Constant.getImageUrl
import com.sns.starsnap.main.viewmodel.main.StarViewModel
import com.sns.starsnap.network.star.dto.StarResponseDto
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun StarListScreen(
    rootNavController: NavController,
    starViewModel: StarViewModel,
    onNavigate: (String) -> Unit
) {
    val starList = starViewModel.starList.collectAsLazyPagingItems()
    LaunchedEffect(Unit) { Log.d("화면", "StarListScreen") }

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(1),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            starList.loadState.refresh is LoadState.Loading && starList.itemCount == 0 -> {
                items(8) { index ->
                    StarListSkeletonCard(
                        modifier = if (index == 0) {
                            Modifier.loadingSemantics("스타 목록")
                        } else {
                            Modifier
                        },
                    )
                }
            }

            starList.loadState.refresh is LoadState.Error && starList.itemCount == 0 -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EntityListStateCard(
                        title = "스타 목록을 불러오지 못했어요",
                        description = "네트워크 상태를 확인한 뒤 다시 시도해 주세요.",
                        actionLabel = "다시 시도",
                        onAction = starList::retry
                    )
                }
            }

            starList.itemCount == 0 -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EntityListStateCard(
                        title = "표시할 스타가 없어요",
                        description = "다른 검색어로 다시 찾아보세요."
                    )
                }
            }

            else -> {
                items(starList.itemCount) { index ->
                    val star = starList[index]
                    if (star == null) {
                        StarListSkeletonCard()
                    } else {
                        StarItem(star) {
                            starViewModel.selectStar(star)
                            onNavigate("star")
                        }
                    }
                }
            }
        }

        if (starList.loadState.append is LoadState.Loading) {
            items(1) {
                StarListSkeletonCard(
                    modifier = Modifier.loadingSemantics("추가 스타"),
                )
            }
        }
    }
}

@Composable
fun StarItem(star: StarResponseDto, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(StarSnapColor.surface)
            .border(1.dp, StarSnapColor.border, shape)
            .clickableSingle(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlideImage(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(StarSnapColor.surfaceSubtle),
                imageModel = { getImageUrl(star.imageKey) },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = star.name,
                    style = StarSnapTypography.title.copy(fontWeight = FontWeight.Bold),
                    color = StarSnapColor.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${star.starGroup?.name ?: "-"} · ${star.nickname?.ifBlank { "-" } ?: "-"}",
                    style = StarSnapTypography.label,
                    color = StarSnapColor.textSubtle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (!star.birthday.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "생일 ${star.birthday}",
                style = StarSnapTypography.caption,
                color = StarSnapColor.textSubtle
            )
        }
    }
}

@Composable
fun EntityListStateCard(
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
