package com.sns.starsnap.main.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sns.starsnap.designsystem.R
import com.sns.starsnap.main.utils.constant.Constant.getImageUrl
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun AppIcon(modifier: Modifier) {
    Image(
        modifier = modifier.size(100.dp),
        painter = painterResource(id = R.drawable.app_icon),
        contentDescription = stringResource(id = R.string.app_icon)
    )
}

@Composable
fun RoundProfileImage(imageKey: String?, modifier: Modifier = Modifier) {
    GlideImage(
        modifier = modifier,
        imageModel = { getImageUrl(imageKey) },
        imageOptions = ImageOptions(
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )
    )
}