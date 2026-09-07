package com.sns.starsnap.main.utils

import androidx.compose.ui.text.input.KeyboardType

enum class EditTextType {
    Password,
    Text,
    Number,
    Email
}

fun getKeyboardType(editTextType: EditTextType) =
    when (editTextType) {
        EditTextType.Password -> KeyboardType.Password
        EditTextType.Text -> KeyboardType.Text
        EditTextType.Number -> KeyboardType.Number
        EditTextType.Email -> KeyboardType.Email
    }

fun maxLangth(editTextType: EditTextType) =
    when (editTextType) {
        EditTextType.Password -> 50
        EditTextType.Email -> 320
        EditTextType.Text -> 50
        EditTextType.Number -> 4
    }