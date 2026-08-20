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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.photo.starsnap.designsystem.CustomColor
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.CustomTextStyle.SignupTitle
import com.photo.starsnap.designsystem.text.CustomTextStyle.TitleMedium
import com.photo.starsnap.designsystem.text.CustomTextStyle.title5
import com.photo.starsnap.designsystem.text.CustomTextStyle.title7
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
        containerColor = CustomColor.background,
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

            if (detail.members.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text("멤버", style = TitleMedium.copy(color = CustomColor.light_black))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(CustomColor.placeholder, CircleShape),
                                        imageModel = { getImageUrl(member.imageKey) },
                                        imageOptions = ImageOptions(
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.Center
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = member.name,
                                        style = title7.copy(color = CustomColor.light_black)
                                    )
                                    if (!member.nickname.isNullOrBlank()) {
                                        Text(
                                            text = member.nickname ?: "",
                                            style = title7.copy(color = CustomColor.sub_title)
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
                    HorizontalDivider(thickness = 1.dp, color = CustomColor.line)
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
                            text = "이 그룹과 연결된 스냅이 없습니다.",
                            style = title5.copy(color = CustomColor.sub_title),
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
                                .background(CustomColor.placeholder)
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
                        style = title5.copy(color = CustomColor.sub_title),
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
            .padding(22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlideImage(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                    .background(Color.White.copy(alpha = 0.3f), CircleShape),
                imageModel = { getImageUrl(starGroup?.imageKey) },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = starGroup?.name ?: "",
                    style = SignupTitle.copy(color = Color.White)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "데뷔 ${starGroup?.debutDate?.ifBlank { "-" } ?: "-"}",
                    style = title7.copy(color = Color.White.copy(alpha = 0.9f))
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
                    .background(CustomColor.brand)
                    .clickableSingle { /* fan toggle: 추후 서버 연동 */ },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "팬 추가", style = title5.copy(color = CustomColor.light_black))
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .clickableSingle { /* share */ },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "공유", style = title5.copy(color = CustomColor.light_black))
            }
        }
    }
}

@Composable
private fun StarGroupTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickableSingle { onClick() }
    ) {
        Text(
            text = label,
            style = title5.copy(
                color = if (selected) CustomColor.light_black else CustomColor.muted
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(28.dp)
                .background(if (selected) CustomColor.light_black else Color.Transparent)
        )
    }
}
