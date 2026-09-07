package com.sns.starsnap.main.ui.screen.main.setting

import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sns.starsnap.designsystem.R
import com.sns.starsnap.designsystem.StarSnapColor
import com.sns.starsnap.designsystem.text.StarSnapTypography
import com.sns.starsnap.main.ui.component.TopAppBar
import com.sns.starsnap.main.ui.component.skeleton.SnapSkeleton

@Composable
internal fun SettingPageScaffold(
    title: String,
    onBack: () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    Scaffold(
        containerColor = StarSnapColor.canvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = title,
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

@Composable
internal fun SettingSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = StarSnapTypography.caption.copy(
                color = StarSnapColor.textSubtle,
                fontWeight = FontWeight.SemiBold,
            ),
            modifier = Modifier
                .padding(start = 4.dp, bottom = 8.dp)
                .semantics { heading() },
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = StarSnapColor.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, StarSnapColor.border),
        ) {
            Column(content = content)
        }
    }
}

@Composable
internal fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = StarSnapColor.border,
        thickness = 1.dp,
    )
}

@Composable
internal fun SettingRow(
    label: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    description: String? = null,
    danger: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val interactionModifier = if (onClick == null) {
        Modifier
    } else {
        Modifier.clickable(onClick = onClick)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(interactionModifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = StarSnapTypography.bodySmall.copy(
                    color = if (danger) StarSnapColor.danger else StarSnapColor.text,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = StarSnapTypography.caption.copy(color = StarSnapColor.textMuted),
                )
            }
        }

        if (!value.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = value,
                style = StarSnapTypography.label.copy(color = StarSnapColor.textSubtle),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (onClick != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                painter = painterResource(R.drawable.chevron_right_icon),
                contentDescription = null,
                tint = StarSnapColor.textMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
internal fun SettingRowSkeleton(
    modifier: Modifier = Modifier,
    showValue: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SnapSkeleton(
            modifier = Modifier
                .width(104.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp)),
        )
        Spacer(modifier = Modifier.weight(1f))
        if (showValue) {
            SnapSkeleton(
                modifier = Modifier
                    .width(76.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp)),
            )
        }
    }
}

@Composable
internal fun SettingSwitchRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SnapSkeleton(
                modifier = Modifier
                    .width(112.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp)),
            )
            SnapSkeleton(
                modifier = Modifier
                    .fillMaxWidth(0.76f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        SnapSkeleton(
            modifier = Modifier
                .width(52.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp)),
        )
    }
}

@Composable
internal fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = StarSnapTypography.bodySmall.copy(
                    color = if (enabled) StarSnapColor.text else StarSnapColor.textMuted,
                    fontWeight = FontWeight.Medium,
                ),
            )
            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = StarSnapTypography.caption.copy(color = StarSnapColor.textMuted),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = StarSnapColor.surface,
                checkedTrackColor = StarSnapColor.brandActive,
                checkedBorderColor = StarSnapColor.brandActive,
                uncheckedThumbColor = StarSnapColor.surface,
                uncheckedTrackColor = StarSnapColor.borderStrong,
                uncheckedBorderColor = StarSnapColor.borderStrong,
                disabledCheckedThumbColor = StarSnapColor.surface,
                disabledCheckedTrackColor = StarSnapColor.brandSoft,
            ),
        )
    }
}

@Composable
internal fun SettingEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = StarSnapColor.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, StarSnapColor.border),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(1.dp, StarSnapColor.border, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.star_icon),
                    contentDescription = null,
                    tint = StarSnapColor.brandActive,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = StarSnapTypography.title,
                color = StarSnapColor.text,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = StarSnapTypography.bodySmall,
                color = StarSnapColor.textSubtle,
            )
        }
    }
}

@Composable
internal fun SettingInfoBanner(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = StarSnapColor.brandSoft,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            text = text,
            style = StarSnapTypography.bodySmall.copy(color = StarSnapColor.textSubtle),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}

@Composable
internal fun SettingPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = StarSnapColor.brand,
            contentColor = StarSnapColor.onBrand,
            disabledContainerColor = StarSnapColor.brandSoft,
            disabledContentColor = StarSnapColor.textMuted,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Text(
            text = text,
            style = StarSnapTypography.label.copy(fontWeight = FontWeight.Bold),
        )
    }
}
