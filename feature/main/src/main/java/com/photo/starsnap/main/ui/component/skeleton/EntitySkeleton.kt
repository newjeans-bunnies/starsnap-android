package com.photo.starsnap.main.ui.component.skeleton

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.photo.starsnap.designsystem.StarSnapColor

@Composable
fun StarListSkeletonCard(modifier: Modifier = Modifier) {
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(StarSnapColor.surface)
            .border(1.dp, StarSnapColor.border, cardShape)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SnapSkeleton(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SnapSkeleton(
                    modifier = Modifier
                        .width(112.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                SnapSkeleton(
                    modifier = Modifier
                        .width(152.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        SnapSkeleton(
            modifier = Modifier
                .width(88.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
        )
    }
}

@Composable
fun StarGroupSkeletonCard(
    height: Dp = 180.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(StarSnapColor.surfaceSubtle)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SnapSkeleton(
                modifier = Modifier
                    .width(112.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            SnapSkeleton(
                modifier = Modifier
                    .width(76.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        }
    }
}

@Composable
fun StarPickerSkeletonCard(
    height: Dp = 150.dp,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(cardShape)
            .background(StarSnapColor.surface)
            .border(1.dp, StarSnapColor.border, cardShape)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SnapSkeleton(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            SnapSkeleton(
                modifier = Modifier
                    .width(88.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            SnapSkeleton(
                modifier = Modifier
                    .width(64.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        }
    }
}
