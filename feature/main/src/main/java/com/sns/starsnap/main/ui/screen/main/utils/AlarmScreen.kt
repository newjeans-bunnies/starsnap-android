package com.sns.starsnap.main.ui.screen.main.utils

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun AlarmScreen() {
    LaunchedEffect(Unit) {
        Log.d("화면", "AlarmScreen")
    }
}