package com.sns.starsnap.main.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sns.starsnap.designsystem.R
import com.sns.starsnap.main.utils.clickableSingle

@Composable
fun AutoLoginOption(checkState: (Boolean) -> Unit) {
    var isChecked by remember { mutableStateOf(false) } // 상태 관리

    Row(
        modifier = Modifier.clickableSingle(
            onClick = {
                isChecked = !isChecked // 상태 토글
                checkState(isChecked) // 상태 전달
            }
        ).padding(start = 5.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Image(
                painter = if (isChecked) painterResource(R.drawable.check_box_true_icon) else painterResource(
                    R.drawable.check_box_false_icon
                ),
                contentDescription = if (isChecked) "Checked" else "Unchecked"
            )
        }
        Text(
            text = stringResource(R.string.auto_login),
            modifier = Modifier.padding(start = 8.dp) // 텍스트와 체크박스 간 간격
        )
    }
}