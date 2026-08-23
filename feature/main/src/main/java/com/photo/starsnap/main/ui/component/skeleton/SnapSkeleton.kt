package com.photo.starsnap.main.ui.component.skeleton

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import com.photo.starsnap.designsystem.StarSnapColor

@Composable
fun SnapSkeleton(modifier: Modifier = Modifier.fillMaxSize()) {
	Box(modifier = modifier.background(StarSnapColor.surfaceSubtle))
}

@Composable
fun SnapFeedSkeletonCard(index: Int, modifier: Modifier = Modifier) {
	val imageAspectRatio = when (index % 4) {
		0 -> 0.78f
		1 -> 1.08f
		2 -> 0.9f
		else -> 1.22f
	}
	val cardShape = RoundedCornerShape(16.dp)

	Column(
		modifier = modifier
			.fillMaxWidth()
			.clip(cardShape)
			.background(StarSnapColor.surface)
			.border(1.dp, StarSnapColor.border, cardShape)
	) {
		SnapSkeleton(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(imageAspectRatio)
		)
		Row(
			modifier = Modifier.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			SnapSkeleton(
				modifier = Modifier
					.size(28.dp)
					.clip(CircleShape)
			)
			Spacer(Modifier.width(8.dp))
			Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
				SnapSkeleton(
					modifier = Modifier
						.width(96.dp)
						.height(12.dp)
						.clip(RoundedCornerShape(6.dp))
				)
				SnapSkeleton(
					modifier = Modifier
						.width(64.dp)
						.height(10.dp)
						.clip(RoundedCornerShape(5.dp))
				)
			}
		}
	}
}

@Composable
fun SnapImageSkeleton(index: Int, modifier: Modifier = Modifier) {
	val imageAspectRatio = when (index % 4) {
		0 -> 0.78f
		1 -> 1.08f
		2 -> 0.9f
		else -> 1.22f
	}

	SnapSkeleton(
		modifier = modifier
			.fillMaxWidth()
			.aspectRatio(imageAspectRatio)
			.clip(RoundedCornerShape(12.dp))
	)
}
