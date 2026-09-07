package com.sns.starsnap.main.ui.screen.main.message

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription

internal fun Modifier.messageLoadingSemantics(label: String): Modifier = clearAndSetSemantics {
    contentDescription = label
    stateDescription = "불러오는 중"
}
