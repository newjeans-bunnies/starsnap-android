package com.sns.starsnap.main.ui.screen.main.setting

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sns.starsnap.designsystem.StarSnapColor
import com.sns.starsnap.designsystem.text.StarSnapTypography
import com.sns.starsnap.main.ui.component.DataLoadErrorCard
import com.sns.starsnap.main.ui.component.RoundProfileImage
import com.sns.starsnap.main.ui.component.skeleton.SnapSkeleton
import com.sns.starsnap.main.ui.component.skeleton.loadingSemantics
import com.sns.starsnap.main.viewmodel.main.UserViewModel

@Composable
fun FixProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    profileSettingViewModel: ProfileSettingViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        Log.d("화면", "FixProfileScreen")
        userViewModel.getUserData()
    }

    val userData by userViewModel.userData.collectAsStateWithLifecycle()
    val userDataLoading by userViewModel.userDataLoading.collectAsStateWithLifecycle()
    val userDataError by userViewModel.userDataError.collectAsStateWithLifecycle()
    val state by profileSettingViewModel.state.collectAsStateWithLifecycle()
    var username by rememberSaveable { mutableStateOf("") }
    var initialized by rememberSaveable { mutableStateOf(false) }
    var showPhotoNotice by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(userData.username) {
        if (!initialized && userData.username.isNotBlank()) {
            username = userData.username
            initialized = true
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            userViewModel.getUserData()
            profileSettingViewModel.consumeSaved()
            navController.popBackStack()
        }
    }

    val availabilityMessage = when (state.availability) {
        UsernameAvailability.Unchanged -> "4~12자의 영문/숫자만 입력 가능합니다."
        UsernameAvailability.Invalid -> "닉네임은 4~12자의 영문/숫자만 가능합니다."
        UsernameAvailability.Checking -> "닉네임 중복 확인 중..."
        UsernameAvailability.Available -> "사용 가능한 닉네임입니다."
        UsernameAvailability.Taken -> "이미 사용 중인 닉네임입니다."
        UsernameAvailability.Error -> "닉네임을 확인하지 못했습니다. 다시 입력해주세요."
    }
    val availabilityColor = when (state.availability) {
        UsernameAvailability.Available -> StarSnapColor.success
        UsernameAvailability.Invalid,
        UsernameAvailability.Taken,
        UsernameAvailability.Error -> StarSnapColor.danger
        else -> StarSnapColor.textMuted
    }
    val usernameChanged = username.trim() != userData.username
    val saveEnabled = userData.username.isNotBlank() && !state.saving && (
        !usernameChanged || state.availability == UsernameAvailability.Available
    )
    val userDataUnavailable = !userDataLoading &&
        !userDataError.isNullOrBlank() &&
        userData.username.isBlank()

    SettingPageScaffold(
        title = "프로필 수정",
        onBack = { navController.popBackStack() },
    ) {
        if (!userDataError.isNullOrBlank()) {
            item {
                DataLoadErrorCard(
                    title = "프로필 정보를 불러오지 못했어요.",
                    description = "네트워크 상태를 확인한 뒤 다시 시도해 주세요.",
                    onRetry = userViewModel::getUserData,
                )
            }
        }
        item {
            SettingSection(title = "프로필 정보") {
                if (userDataLoading && userData.username.isBlank()) {
                    ProfileEditorSkeleton(
                        modifier = Modifier.loadingSemantics("프로필 편집 정보"),
                    )
                } else if (userDataUnavailable) {
                    SettingInfoBanner(
                        text = "프로필 정보를 불러온 뒤 수정할 수 있어요.",
                        modifier = Modifier.padding(16.dp),
                    )
                } else {
                    Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        RoundProfileImage(
                            imageKey = userData.profileImageUrl,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(StarSnapColor.surfaceSubtle)
                                .border(1.dp, StarSnapColor.border, CircleShape),
                        )
                        OutlinedButton(
                            onClick = { showPhotoNotice = !showPhotoNotice },
                            modifier = Modifier.height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = StarSnapColor.textSubtle,
                            ),
                        ) {
                            Text(
                                text = "사진 변경",
                                style = StarSnapTypography.label.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }

                    if (showPhotoNotice) {
                        Spacer(modifier = Modifier.height(14.dp))
                        SettingInfoBanner(
                            text = "프로필 사진 변경은 현재 StarSnap 웹에서 이용할 수 있어요.",
                            backgroundColor = StarSnapColor.surfaceSubtle,
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "닉네임",
                        style = StarSnapTypography.label.copy(fontWeight = FontWeight.Bold),
                        color = StarSnapColor.text,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { value ->
                            val next = value.take(12)
                            username = next
                            profileSettingViewModel.checkUsername(next, userData.username)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "닉네임 입력",
                                style = StarSnapTypography.bodySmall,
                            )
                        },
                        textStyle = StarSnapTypography.bodySmall,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StarSnapColor.brandActive,
                            unfocusedBorderColor = StarSnapColor.border,
                            errorBorderColor = StarSnapColor.danger,
                            focusedContainerColor = StarSnapColor.surface,
                            unfocusedContainerColor = StarSnapColor.surface,
                        ),
                        isError = state.availability in setOf(
                            UsernameAvailability.Invalid,
                            UsernameAvailability.Taken,
                            UsernameAvailability.Error,
                        ),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = availabilityMessage,
                        style = StarSnapTypography.caption,
                        color = availabilityColor,
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "이메일",
                        style = StarSnapTypography.label.copy(fontWeight = FontWeight.Bold),
                        color = StarSnapColor.text,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = userData.email,
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = StarSnapTypography.bodySmall,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = StarSnapColor.textSubtle,
                            disabledBorderColor = StarSnapColor.border,
                            disabledContainerColor = StarSnapColor.surfaceSubtle,
                        ),
                    )

                    if (!state.errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = state.errorMessage.orEmpty(),
                            style = StarSnapTypography.bodySmall,
                            color = StarSnapColor.danger,
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            enabled = !state.saving,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = StarSnapColor.textSubtle,
                            ),
                        ) {
                            Text(
                                text = "취소",
                                style = StarSnapTypography.label.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                        Button(
                            onClick = {
                                profileSettingViewModel.saveUsername(username, userData.username)
                            },
                            enabled = saveEnabled,
                            modifier = Modifier
                                .weight(1f)
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
                                text = if (state.saving) "저장 중..." else "저장",
                                style = StarSnapTypography.label.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileEditorSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SnapSkeleton(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape),
            )
            SnapSkeleton(
                modifier = Modifier
                    .width(112.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        repeat(2) { index ->
            SnapSkeleton(
                modifier = Modifier
                    .width(if (index == 0) 64.dp else 48.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp)),
            )
            Spacer(modifier = Modifier.height(8.dp))
            SnapSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
            if (index == 0) {
                Spacer(modifier = Modifier.height(8.dp))
                SnapSkeleton(
                    modifier = Modifier
                        .width(184.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(2) {
                SnapSkeleton(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }
        }
    }
}
