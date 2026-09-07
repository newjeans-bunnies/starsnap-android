package com.sns.starsnap.main.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sns.starsnap.designsystem.StarSnapColor
import com.sns.starsnap.designsystem.text.StarSnapTypography

private val AuthCardShape = RoundedCornerShape(24.dp)

@Composable
internal fun AuthCardPage(
    maxWidth: androidx.compose.ui.unit.Dp = 460.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        StarSnapColor.brandSoft,
                        StarSnapColor.canvas,
                        StarSnapColor.canvas,
                    )
                )
            )
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth)
                .shadow(8.dp, AuthCardShape, clip = false)
                .clip(AuthCardShape)
                .background(StarSnapColor.surface)
                .border(1.dp, StarSnapColor.border, AuthCardShape)
                .padding(24.dp),
            content = content,
        )
    }
}

@Composable
internal fun AuthBrandHeader(subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(StarSnapColor.brandSoft)
                .semantics { contentDescription = "StarSnap 로고" },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "★", style = StarSnapTypography.headingLarge)
        }
        Spacer(Modifier.height(12.dp))
        Text(text = "StarSnap", style = StarSnapTypography.headingLarge)
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = StarSnapTypography.bodySmall.copy(color = StarSnapColor.textSubtle),
        )
    }
}

@Composable
internal fun AuthFieldLabel(text: String) {
    Text(
        text = text,
        style = StarSnapTypography.label.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
internal fun AuthPrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = StarSnapColor.brand,
            contentColor = StarSnapColor.onBrand,
            disabledContainerColor = StarSnapColor.brandSoft,
            disabledContentColor = StarSnapColor.textMuted,
        ),
    ) {
        Text(
            text = text,
            style = StarSnapTypography.label.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
internal fun AuthSecondaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = StarSnapColor.textSubtle),
    ) {
        Text(text = text, style = StarSnapTypography.label)
    }
}

@Composable
internal fun SignupStepHeader(
    step: Int,
    label: String,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "회원가입 $step 단계, 전체 5단계" },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index < step) StarSnapColor.brand else StarSnapColor.surfaceSubtle
                    )
            )
        }
    }
    Spacer(Modifier.height(20.dp))
    Text(
        text = "STEP $step / 5 · $label",
        style = StarSnapTypography.caption.copy(color = StarSnapColor.textMuted),
    )
    Spacer(Modifier.height(8.dp))
    Text(text = title, style = StarSnapTypography.headingLarge)
    Spacer(Modifier.height(8.dp))
    Text(
        text = description,
        style = StarSnapTypography.bodySmall.copy(color = StarSnapColor.textSubtle),
    )
}
