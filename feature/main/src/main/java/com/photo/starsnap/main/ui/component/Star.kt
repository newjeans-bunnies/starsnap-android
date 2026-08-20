package com.photo.starsnap.main.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.photo.starsnap.designsystem.CustomColor
import com.photo.starsnap.designsystem.text.CustomTextStyle.title8
import com.photo.starsnap.designsystem.text.TextFont.pretendard
import com.photo.starsnap.network.snap.dto.StarDto

@Composable
fun SmallStar(starDto: StarDto) {
    Column(
        modifier = Modifier
            .height(65.dp)
            .width(50.dp)
    ) {
        RoundProfileImage(imageKey = null, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = starDto.nickname,
            style = title8.copy(color = CustomColor.light_black),
        )
    }
}

@Composable
fun SmallStars(stars: List<StarDto>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp) // 별 사이 간격 설정
    ) {
        itemsIndexed(stars) { _, star ->
            SmallStar(star)
        }
    }
}
