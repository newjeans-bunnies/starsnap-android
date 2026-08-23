package com.photo.starsnap.main.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.StarSnapTypography
import com.photo.starsnap.network.snap.dto.StarDto

@Composable
fun SmallStar(starDto: StarDto) {
    Column(
        modifier = Modifier
            .height(72.dp)
            .width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RoundProfileImage(
            imageKey = null,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(StarSnapColor.surfaceSubtle),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = starDto.nickname,
            style = StarSnapTypography.caption.copy(color = StarSnapColor.textSubtle),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun SmallStars(stars: List<StarDto>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(stars) { _, star ->
            SmallStar(star)
        }
    }
}
