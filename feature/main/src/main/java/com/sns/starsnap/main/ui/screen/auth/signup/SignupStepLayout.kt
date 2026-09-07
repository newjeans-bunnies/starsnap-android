package com.sns.starsnap.main.ui.screen.auth.signup

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sns.starsnap.main.ui.screen.auth.AuthCardPage
import com.sns.starsnap.main.ui.screen.auth.AuthPrimaryButton
import com.sns.starsnap.main.ui.screen.auth.AuthSecondaryButton
import com.sns.starsnap.main.ui.screen.auth.SignupStepHeader

@Composable
internal fun SignupStepLayout(
    step: Int,
    label: String,
    title: String,
    description: String,
    primaryText: String,
    primaryEnabled: Boolean,
    onPrimary: () -> Unit,
    onBack: () -> Unit,
    backText: String = "이전",
    content: @Composable ColumnScope.() -> Unit,
) {
    AuthCardPage {
        SignupStepHeader(
            step = step,
            label = label,
            title = title,
            description = description,
        )
        Spacer(Modifier.height(28.dp))
        content()
        Spacer(Modifier.height(28.dp))
        AuthPrimaryButton(
            text = primaryText,
            enabled = primaryEnabled,
            onClick = onPrimary,
        )
        AuthSecondaryButton(text = backText, onClick = onBack)
    }
}
