package com.photo.starsnap.main.ui.screen.main.star_hub

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.StarSnapTypography
import com.photo.starsnap.main.ui.component.TopAppBar
import com.photo.starsnap.main.ui.component.skeleton.SnapImageSkeleton
import com.photo.starsnap.main.utils.NavigationRoute.SNAP
import com.photo.starsnap.main.utils.clickableSingle
import com.photo.starsnap.main.utils.constant.Constant.getImageUrl
import com.photo.starsnap.main.viewmodel.main.SnapViewModel
import com.photo.starsnap.main.viewmodel.main.StarViewModel
import com.photo.starsnap.main.viewmodel.main.UserViewModel
import com.photo.starsnap.network.star.dto.StarGroupResponseDto
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun StarGroupScreen(
    mainNavController: NavController,
    starViewModel: StarViewModel,
    userViewModel: UserViewModel,
    snapViewModel: SnapViewModel,
) {
    val starGroup = starViewModel.selectedStarGroup.collectAsState().value
    val detail = starViewModel.starGroupDetail.collectAsState().value
    var tab by remember { mutableStateOf("스냅") }

    LaunchedEffect(starGroup?.id) {
        Log.d("화면", "StarGroupScreen")
        val id = starGroup?.id ?: return@LaunchedEffect
        starViewModel.loadStarGroupDetail(id)
    }

    Scaffold(
        containerColor = StarSnapColor.canvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "",
                onBack = { mainNavController.popBackStack() }
            )
        }
    ) { padding ->
        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                StarGroupBanner(starGroup)
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "멤버",
                        style = StarSnapTypography.title.copy(fontWeight = FontWeight.Bold),
                        color = StarSnapColor.text
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (detail.members.isEmpty()) {
                        Text(
                            text = "멤버 정보가 없습니다.",
                            style = StarSnapTypography.label,
                            color = StarSnapColor.textSubtle
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(28.dp)
                        ) {
                            detail.members.forEach { member ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickableSingle {
                                        starViewModel.selectStar(member)
                                        mainNavController.navigate("star")
                                    }
                                ) {
                                    GlideImage(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, StarSnapColor.border, CircleShape)
                                            .background(StarSnapColor.surfaceSubtle, CircleShape),
                                        imageModel = { getImageUrl(member.imageKey) },
                                        imageOptions = ImageOptions(
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.Center
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = member.name,
                                        style = StarSnapTypography.label,
                                        color = StarSnapColor.text
                                    )
                                    if (!member.nickname.isNullOrBlank()) {
                                        Text(
                                            text = member.nickname ?: "",
                                            style = StarSnapTypography.caption,
                                            color = StarSnapColor.textSubtle
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        StarGroupTab("스냅", tab == "스냅") { tab = "스냅" }
                        StarGroupTab("정보", tab == "정보") { tab = "정보" }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 1.dp, color = StarSnapColor.border)
                }
            }

            if (tab == "스냅") {
                if (detail.loading) {
                    items(8) { index ->
                        SnapImageSkeleton(index)
                    }
                } else if (detail.snaps.isEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Text(
                            text = "이 스타그룹에 연결된 스냅이 없습니다.",
                            style = StarSnapTypography.label,
                            color = StarSnapColor.textSubtle,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                } else {
                    items(detail.snaps.size) { index ->
                        val snap = detail.snaps[index]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(snap.snapData.imageAspectRatio ?: 1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(StarSnapColor.surfaceSubtle)
                                .clickableSingle {
                                    snapViewModel.selectSnap(snap)
                                    mainNavController.navigate(SNAP)
                                }
                        ) {
                            GlideImage(
                                modifier = Modifier.fillMaxSize(),
                                imageModel = { getImageUrl(snap.snapData.imageKey) },
                                imageOptions = ImageOptions(
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.Center
                                )
                            )
                        }
                    }
                }
            } else {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                        text = starGroup?.explanation?.ifBlank { "등록된 스타그룹 소개가 없습니다." }
                            ?: "등록된 스타그룹 소개가 없습니다.",
                        style = StarSnapTypography.bodySmall,
                        color = StarSnapColor.textSubtle,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StarGroupBanner(starGroup: StarGroupResponseDto?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(StarSnapColor.textMuted, StarSnapColor.textSubtle)
                )
            )
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlideImage(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .border(4.dp, StarSnapColor.surface.copy(alpha = 0.7f), CircleShape)
                    .background(StarSnapColor.surface.copy(alpha = 0.3f), CircleShape),
                imageModel = { getImageUrl(starGroup?.imageKey) },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = starGroup?.name ?: "",
                    style = StarSnapTypography.displayLarge,
                    color = StarSnapColor.surface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "데뷔 ${starGroup?.debutDate?.ifBlank { "-" } ?: "-"}",
                    style = StarSnapTypography.label,
                    color = StarSnapColor.surface.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StarSnapColor.brand)
                    .clickableSingle { /* fan toggle: 추후 서버 연동 */ },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "팬 추가",
                    style = StarSnapTypography.label.copy(fontWeight = FontWeight.Bold),
                    color = StarSnapColor.onBrand
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StarSnapColor.surface)
                    .clickableSingle { /* share */ },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "공유",
                    style = StarSnapTypography.label.copy(fontWeight = FontWeight.Bold),
                    color = StarSnapColor.text
                )
            }
        }
    }
}

@Composable
private fun StarGroupTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(64.dp)
            .height(44.dp)
            .clickableSingle(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = StarSnapTypography.label.copy(fontWeight = FontWeight.SemiBold),
            color = if (selected) StarSnapColor.text else StarSnapColor.textMuted
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(2.dp)
                .width(28.dp)
                .background(
                    if (selected) StarSnapColor.text
                    else StarSnapColor.surface.copy(alpha = 0f)
                )
        )
    }
}
