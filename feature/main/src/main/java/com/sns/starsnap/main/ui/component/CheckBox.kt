package com.sns.starsnap.main.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.sns.starsnap.designsystem.R
import com.sns.starsnap.main.utils.clickableSingle

@Composable
fun CustomCheckBox(
    onPainter: Painter,
    offPainter: Painter,
    onCheckedChange: (Boolean) -> Unit,
) {
    var checkState by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.clickableSingle(
            onClick = {
                checkState = !checkState // 상태 변경
                onCheckedChange(checkState) // 변경된 상태 전달
            }
        )
    ) {
        Image(
            painter = if (checkState) onPainter else offPainter,
            contentDescription = if (checkState) "Checked" else "Unchecked"
        )
    }
}


@Composable
fun PasswordEyeCheckBox(checkState: (Boolean) -> Unit) {
    CustomCheckBox(
        onPainter = painterResource(R.drawable.eye_open_icon),
        offPainter = painterResource(R.drawable.eye_close_icon),
        onCheckedChange = checkState
    )
}

@Composable
fun AutoLoginCheckBox(checkState: (Boolean) -> Unit) {
    CustomCheckBox(
        onPainter = painterResource(R.drawable.check_box_true_icon),
        offPainter = painterResource(R.drawable.check_box_false_icon),
        onCheckedChange = checkState
    )
}