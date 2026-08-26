package com.photo.starsnap.main.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.StarSnapTypography

@Composable
fun DataLoadErrorCard(
    title: String,
    description: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        color = StarSnapColor.dangerSoft,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, StarSnapColor.dangerBorder),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text(
                text = title,
                style = StarSnapTypography.title.copy(
                    color = StarSnapColor.danger,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = StarSnapTypography.bodySmall.copy(color = StarSnapColor.textSubtle),
            )
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(onClick = onRetry) {
                Text(
                    text = "다시 시도",
                    style = StarSnapTypography.label.copy(
                        color = StarSnapColor.brandActive,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}
