package com.photo.starsnap.main.ui.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.photo.starsnap.designsystem.R
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.StarSnapTypography

@Composable
fun StarSnapAppBar(
    profileImageKey: String?,
    onHome: () -> Unit,
    onMessages: () -> Unit,
    onProfile: () -> Unit,
) {
    Surface(
        color = StarSnapColor.surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onHome,
                    modifier = Modifier.semantics {
                        contentDescription = "StarSnap 홈"
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.star_icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = StarSnapColor.brand,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onMessages,
                    modifier = Modifier.semantics {
                        contentDescription = "메시지"
                    },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = StarSnapColor.textSubtle,
                    )
                }

                IconButton(
                    onClick = onProfile,
                    modifier = Modifier.semantics {
                        contentDescription = "프로필"
                    },
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(StarSnapColor.surfaceSubtle)
                            .border(1.dp, StarSnapColor.border, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (profileImageKey.isNullOrBlank()) {
                            Icon(
                                painter = painterResource(id = R.drawable.user_icon),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = StarSnapColor.textMuted,
                            )
                        } else {
                            RoundProfileImage(
                                imageKey = profileImageKey,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape),
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = StarSnapColor.border)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: String,
    onBack: () -> Unit,
    titleContent: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null
) {
    Column {
        CenterAlignedTopAppBar(
            windowInsets = TopAppBarDefaults.windowInsets,
            title = {
                titleContent?.invoke() ?: Text(
                    text = title,
                    maxLines = 1,
                    style = StarSnapTypography.title,
                    textAlign = TextAlign.Center,
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = StarSnapColor.surface,
                titleContentColor = StarSnapColor.text,
                navigationIconContentColor = StarSnapColor.textSoft,
                actionIconContentColor = StarSnapColor.textSoft,
            ),
            navigationIcon = {
                IconButton(onClick = {
                    Log.e("TopAppBarBack", "click_back:title=$title")
                    onBack()
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.back_arrow_icon),
                        contentDescription = "뒤로 가기",
                    )
                }
            },
            actions = {
                actions?.invoke()
            },
        )
        HorizontalDivider(color = StarSnapColor.border)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickImageTopAppBar(
    onClose: () -> Unit,
    onNext: () -> Unit,
    nextEnabled: Boolean = true,
) {
    Column {
        CenterAlignedTopAppBar(
            windowInsets = TopAppBarDefaults.windowInsets,
            title = {
                Text(
                    text = "새로운 스냅",
                    maxLines = 1,
                    style = StarSnapTypography.title,
                    textAlign = TextAlign.Center,
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = StarSnapColor.surface,
                titleContentColor = StarSnapColor.text,
                navigationIconContentColor = StarSnapColor.textSoft,
                actionIconContentColor = StarSnapColor.textSoft,
            ),
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.close_icon),
                        contentDescription = "닫기",
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = onNext,
                    enabled = nextEnabled,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = StarSnapColor.text,
                        disabledContentColor = StarSnapColor.textMuted,
                    ),
                ) {
                    Text(
                        text = "다음",
                        style = StarSnapTypography.label.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            },
        )
        HorizontalDivider(color = StarSnapColor.border)
    }
}
