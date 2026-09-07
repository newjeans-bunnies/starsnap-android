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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.sns.starsnap.designsystem.R
import com.sns.starsnap.designsystem.StarSnapColor
import com.sns.starsnap.designsystem.text.StarSnapTypography
import com.sns.starsnap.main.ui.component.skeleton.StarPickerSkeletonCard
import com.sns.starsnap.main.ui.component.skeleton.loadingSemantics
import com.sns.starsnap.main.utils.NavigationRoute
import com.sns.starsnap.main.utils.clickableSingle
import com.sns.starsnap.main.utils.constant.Constant.getImageUrl
import com.sns.starsnap.main.viewmodel.main.UploadViewModel
import com.sns.starsnap.network.star.dto.StarResponseDto
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun PickStarScreen(navController: NavController, uploadViewModel: UploadViewModel) {
    var searchStarName by remember { mutableStateOf("") }
    val selectedStars by uploadViewModel.selectedStars.collectAsStateWithLifecycle()
    val starList = uploadViewModel.starList(searchStarName).collectAsLazyPagingItems()
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
                        text = "스타 선택",
                        style = StarSnapTypography.heading,
                        color = StarSnapColor.text,
                        modifier = Modifier.weight(1f)
                    )
                    PickerDoneButton(label = "확인 (${selectedStars.size})") {
                        navController.navigate(NavigationRoute.SET_SNAP) {
                            launchSingleTop = true
                            popUpTo(NavigationRoute.PICK_STAR) { inclusive = true }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                SearchTextField("스타 또는 그룹 검색") { searchStarName = it }
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
                starList.loadState.refresh is LoadState.Loading -> {
                    items(8) { index ->
                        StarPickerSkeletonCard(
                            height = 150.dp,
                            modifier = if (index == 0) {
                                Modifier.loadingSemantics("스타 선택 목록")
                            } else {
                                Modifier
                            },
                        )
                    }
                }

                starList.loadState.refresh is LoadState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        PickerStateCard(
                            title = "스타 목록을 불러오지 못했어요",
                            actionLabel = "다시 시도",
                            onAction = starList::retry
                        )
                    }
                }

                starList.itemCount == 0 -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        PickerStateCard(title = "표시할 스타가 없어요")
                    }
                }

                else -> {
                    items(starList.itemCount) { index ->
                        val star = starList[index]
                        if (star == null) {
                            StarPickerSkeletonCard(height = 150.dp)
                        } else {
                            StarItem(
                                star = star,
                                selected = selectedStars.any { it.id == star.id },
                                onClick = { uploadViewModel.selectedStar(star) }
                            )
                        }
                    }
                }
            }

            if (starList.loadState.append is LoadState.Loading) {
                items(2) { index ->
                    StarPickerSkeletonCard(
                        height = 150.dp,
                        modifier = if (index == 0) {
                            Modifier.loadingSemantics("추가 스타 선택 항목")
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
fun StarItem(
    star: StarResponseDto,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
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
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(StarSnapColor.surfaceSubtle),
                imageModel = { getImageUrl(star.imageKey) },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = star.name,
                style = StarSnapTypography.body.copy(color = StarSnapColor.text),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = star.nickname?.ifBlank { "-" } ?: "-",
                style = StarSnapTypography.caption.copy(color = StarSnapColor.textMuted),
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

@Composable
fun SearchTextField(hint: String, inputText: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    BasicTextField(
        value = text,
        textStyle = StarSnapTypography.label.copy(color = StarSnapColor.text),
        onValueChange = { input ->
            if (input.length <= 100) {
                text = input
                inputText(input)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(StarSnapColor.surface)
            .border(1.dp, StarSnapColor.border, RoundedCornerShape(12.dp)),
        singleLine = true,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = ImageVector.vectorResource(R.drawable.search_icon),
                    contentDescription = null,
                    tint = StarSnapColor.textMuted
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = hint,
                            style = StarSnapTypography.label,
                            color = StarSnapColor.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
fun PickerDoneButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(StarSnapColor.brand)
            .clickableSingle(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = StarSnapTypography.label, color = StarSnapColor.onBrand)
    }
}

@Composable
fun PickerStateCard(
    title: String,
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
        Text(title, style = StarSnapTypography.label, color = StarSnapColor.textSubtle)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(12.dp))
            PickerDoneButton(actionLabel, onAction)
        }
    }
}
