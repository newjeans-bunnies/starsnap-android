package com.photo.starsnap.main.ui.screen.main.star_hub

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import androidx.compose.runtime.collectAsState

@Composable
fun StarScreen(
    mainNavController: NavController,
    starViewModel: StarViewModel,
    snapViewModel: SnapViewModel,
) {
    val star = starViewModel.selectedStar.collectAsState().value
    val detail = starViewModel.starDetail.collectAsState().value
    var tab by remember { mutableStateOf("스냅") }

    LaunchedEffect(star?.id) {
        Log.d("화면", "StarScreen")
        val selectedStar = star ?: return@LaunchedEffect
        starViewModel.loadStarDetail(selectedStar)
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
                StarBanner(
                    name = star?.name ?: "",
                    groupName = star?.starGroup?.name ?: "-",
                    nickname = star?.nickname?.ifBlank { "-" } ?: "-",
                    birthday = star?.birthday,
                    imageKey = star?.imageKey,
                    snapCount = detail.snaps.size
                )
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        StarTab("스냅", tab == "스냅") { tab = "스냅" }
                        StarTab("정보", tab == "정보") { tab = "정보" }
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
                            text = "이 스타와 연결된 스냅이 없습니다.",
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
                    StarInfoSection(
                        explanation = star?.explanation?.ifBlank { "등록된 스타 소개가 없습니다." }
                            ?: "등록된 스타 소개가 없습니다."
                    )
                }
            }
        }
    }
}

@Composable
private fun StarBanner(
    name: String,
    groupName: String,
    nickname: String,
    birthday: String?,
    imageKey: String?,
    snapCount: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StarSnapColor.surface)
            .border(1.dp, StarSnapColor.border, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlideImage(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(StarSnapColor.surfaceSubtle, CircleShape),
                imageModel = { getImageUrl(imageKey) },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = StarSnapTypography.headingLarge,
                    color = StarSnapColor.text
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$groupName · $nickname",
                    style = StarSnapTypography.caption,
                    color = StarSnapColor.textSubtle
                )
                if (!birthday.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "생일 $birthday",
                        style = StarSnapTypography.caption,
                        color = StarSnapColor.textSubtle
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            StarStat(value = snapCount.toString(), label = "스냅", modifier = Modifier.weight(1f))
            StarStat(value = "-", label = "팬", modifier = Modifier.weight(1f))
            StarStat(value = "-", label = "좋아요", modifier = Modifier.weight(1f))
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
                    .border(1.dp, StarSnapColor.border, RoundedCornerShape(10.dp))
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
private fun StarTab(label: String, selected: Boolean, onClick: () -> Unit) {
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

@Composable
private fun StarInfoSection(
    explanation: String,
) {
    Text(
        text = explanation,
        style = StarSnapTypography.bodySmall,
        color = StarSnapColor.textSubtle,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun StarStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = StarSnapTypography.body.copy(fontWeight = FontWeight.SemiBold),
            color = StarSnapColor.text
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, style = StarSnapTypography.caption, color = StarSnapColor.textMuted)
    }
}
