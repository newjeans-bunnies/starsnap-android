package com.photo.starsnap.main.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.StarSnapTypography
import com.photo.starsnap.main.utils.EditTextType
import com.photo.starsnap.main.utils.getKeyboardType
import com.photo.starsnap.main.utils.maxLangth
import com.photo.starsnap.main.viewmodel.auth.SignupViewModel

@Composable
fun BaseEditText(
    defaultText: String = "",
    hint: String,
    inputText: (String) -> Unit,
    editTextType: EditTextType
) {
    var text by remember(defaultText) { mutableStateOf(defaultText) }
    val keyboardType = getKeyboardType(editTextType)
    var checkState by remember { mutableStateOf(false) }
    val maxLangth = maxLangth(editTextType)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val visualTransformation =
        if (!checkState && editTextType == EditTextType.Password) PasswordVisualTransformation() else VisualTransformation.None
    BasicTextField(
        value = text,
        textStyle = StarSnapTypography.label.copy(color = StarSnapColor.text),
        onValueChange = { input ->
            if (maxLangth > input.length) {
                text = input
                inputText(input)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(StarSnapColor.surface, shape = RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (isFocused) StarSnapColor.brand else StarSnapColor.border,
                shape = RoundedCornerShape(12.dp),
            ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(StarSnapColor.brandActive),
        singleLine = true,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.weight(1F)
                ) {
                    innerTextField()
                    if (text.isEmpty()) {
                        TextEditHint(hint)
                    }
                }
                if (editTextType == EditTextType.Password) {
                    Spacer(Modifier.width(8.dp))
                    PasswordEyeCheckBox { checkState = it }
                }
            }
        },
    )
}

@Composable
fun EditText(hint: String, username: (String) -> Unit) {
    BaseEditText(hint = hint, inputText = username, editTextType = EditTextType.Text)
}

@Composable
fun PasswordEditText(hint: String, password: (String) -> Unit) {
    BaseEditText(hint = hint, inputText = password, editTextType = EditTextType.Password)
}

@Composable
fun VerifyCodeEditText(viewModel: SignupViewModel) {
    var code by remember { mutableStateOf("") }

    val timerUiState by viewModel.timerUiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicTextField(
            value = code,
            onValueChange = { input ->
                // 최대 글자수 4개로 제한
                if (input.length <= 4 && timerUiState.isTimerRunning) {
                    code = input
                    viewModel.verifyCode = input
                }
            },
            modifier = Modifier
                .height(72.dp)
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = VisualTransformation.None,
            textStyle = StarSnapTypography.headingLarge.copy(
                color = StarSnapColor.text,
                textAlign = TextAlign.Center,
            ),
            cursorBrush = SolidColor(StarSnapColor.brandActive),
            singleLine = true,
            decorationBox = {
                // 기본 입력창은 숨기고, 커스텀 UI로 각 자리 박스를 그립니다.
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { index ->
                            Spacer(Modifier.width(if (index == 0) 0.dp else 13.dp))
                            VerifyCodeEditText(code.getOrNull(index)?.toString() ?: "")
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun VerifyCodeEditText(code: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(72.dp)
            .width(60.dp)
            .background(StarSnapColor.surface, shape = RoundedCornerShape(12.dp))
            .border(1.dp, StarSnapColor.border, shape = RoundedCornerShape(12.dp))
    ) {
        Text(
            text = code,
            style = StarSnapTypography.headingLarge.copy(
                color = StarSnapColor.text,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun BaseEditTextPreview() {
    BaseEditText(hint = "아이디", inputText = {}, editTextType = EditTextType.Text)
}
