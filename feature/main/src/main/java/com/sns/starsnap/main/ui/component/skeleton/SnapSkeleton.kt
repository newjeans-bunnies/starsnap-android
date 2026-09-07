package com.sns.starsnap.main.ui.component.skeleton

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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.sns.starsnap.designsystem.StarSnapColor

@Composable
fun SnapSkeleton(modifier: Modifier = Modifier.fillMaxSize()) {
	Box(
		modifier = modifier
			.clearAndSetSemantics { }
			.background(StarSnapColor.surfaceSubtle),
	)
}

fun Modifier.loadingSemantics(label: String): Modifier = clearAndSetSemantics {
	contentDescription = label
	stateDescription = "불러오는 중"
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

@Composable
fun RelatedSnapSkeletonCard(modifier: Modifier = Modifier) {
	val cardShape = RoundedCornerShape(14.dp)

	Column(
		modifier = modifier
			.width(164.dp)
			.clip(cardShape)
			.background(StarSnapColor.surface)
			.border(1.dp, StarSnapColor.border, cardShape)
	) {
		SnapSkeleton(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(1f)
		)
		SnapSkeleton(
			modifier = Modifier
				.padding(start = 10.dp, top = 10.dp, end = 10.dp)
				.width(112.dp)
				.height(14.dp)
				.clip(RoundedCornerShape(7.dp))
		)
		SnapSkeleton(
			modifier = Modifier
				.padding(start = 10.dp, top = 6.dp, end = 10.dp, bottom = 10.dp)
				.width(76.dp)
				.height(10.dp)
				.clip(RoundedCornerShape(5.dp))
		)
	}
}

@Composable
fun ProfileHeaderSkeleton(
	modifier: Modifier = Modifier,
	actionRows: Int = 1,
) {
	val cardShape = RoundedCornerShape(16.dp)

	Column(
		modifier = modifier
			.fillMaxWidth()
			.clip(cardShape)
			.background(StarSnapColor.surface)
			.border(1.dp, StarSnapColor.border, cardShape)
			.padding(20.dp),
	) {
		SnapSkeleton(
			modifier = Modifier
				.size(80.dp)
				.clip(CircleShape),
		)
		Spacer(modifier = Modifier.height(16.dp))
		SnapSkeleton(
			modifier = Modifier
				.width(132.dp)
				.height(24.dp)
				.clip(RoundedCornerShape(10.dp)),
		)
		Spacer(modifier = Modifier.height(8.dp))
		SnapSkeleton(
			modifier = Modifier
				.width(96.dp)
				.height(14.dp)
				.clip(RoundedCornerShape(7.dp)),
		)
		Spacer(modifier = Modifier.height(6.dp))
		SnapSkeleton(
			modifier = Modifier
				.width(116.dp)
				.height(12.dp)
				.clip(RoundedCornerShape(6.dp)),
		)

		if (actionRows > 0) {
			Spacer(modifier = Modifier.height(18.dp))
			repeat(actionRows) { index ->
				SnapSkeleton(
					modifier = Modifier
						.fillMaxWidth()
						.height(44.dp)
						.clip(RoundedCornerShape(10.dp)),
				)
				if (index < actionRows - 1) {
					Spacer(modifier = Modifier.height(8.dp))
				}
			}
		}
	}
}

@Composable
fun ConnectedEntitiesSkeleton(modifier: Modifier = Modifier) {
	Column(modifier = modifier.fillMaxWidth()) {
		SnapSkeleton(
			modifier = Modifier
				.width(72.dp)
				.height(10.dp)
				.clip(RoundedCornerShape(5.dp)),
		)
		Spacer(modifier = Modifier.height(8.dp))
		Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
			repeat(4) { index ->
				Column(horizontalAlignment = Alignment.CenterHorizontally) {
					SnapSkeleton(
						modifier = Modifier
							.size(44.dp)
							.clip(CircleShape),
					)
					Spacer(modifier = Modifier.height(4.dp))
					SnapSkeleton(
						modifier = Modifier
							.width(if (index % 2 == 0) 52.dp else 42.dp)
							.height(10.dp)
							.clip(RoundedCornerShape(5.dp)),
					)
				}
			}
		}
	}
}
