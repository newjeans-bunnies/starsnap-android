package com.sns.starsnap.main.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.sns.starsnap.designsystem.text.CustomTextStyle.hint1
import com.sns.starsnap.designsystem.CustomColor.light_black
import java.util.Locale

@Composable
fun VerifyCodeTimer(time: Long) {
    val formattedTime = remember(time) {
        val minutes = time / 60
        val seconds = time % 60
        String.format(Locale.KOREA,"%02d:%02d", minutes, seconds)
    }

    Text(formattedTime, style = hint1, color = light_black, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
}