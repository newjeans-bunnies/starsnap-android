package com.sns.starsnap.main.ui.screen.auth.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sns.starsnap.designsystem.R
import com.sns.starsnap.designsystem.StarSnapColor
import com.sns.starsnap.designsystem.text.StarSnapTypography
import com.sns.starsnap.main.utils.NavigationRoute.SIGNUP_LOADING
import com.sns.starsnap.main.viewmodel.auth.SignupViewModel
import com.sns.starsnap.main.viewmodel.auth.State

@Composable
fun ConsentSignupScreen(viewModel: SignupViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()
    var termsAccepted by rememberSaveable { mutableStateOf(false) }
    var privacyAccepted by rememberSaveable { mutableStateOf(false) }
    var marketingAccepted by rememberSaveable { mutableStateOf(false) }
    val allAccepted = termsAccepted && privacyAccepted && marketingAccepted
    val requiredAccepted = termsAccepted && privacyAccepted

    LaunchedEffect(uiState.signupState) {
        if (uiState.signupState == State.LOADING) {
            navController.navigate(SIGNUP_LOADING) { launchSingleTop = true }
        }
    }

    SignupStepLayout(
        step = 5,
        label = "약관",
        title = stringResource(R.string.signup_consent_screen_title),
        description = "필수 약관에 동의하면 StarSnap 가입이 완료됩니다.",
        primaryText = "회원가입",
        primaryEnabled = requiredAccepted,
        onPrimary = viewModel::signup,
        onBack = { navController.popBackStack() },
    ) {
        ConsentRow(
            title = "전체 동의",
            description = "선택 항목을 포함한 모든 약관에 동의합니다.",
            checked = allAccepted,
            emphasized = true,
            onCheckedChange = { checked ->
                termsAccepted = checked
                privacyAccepted = checked
                marketingAccepted = checked
            },
        )
        Spacer(Modifier.height(10.dp))
        ConsentRow(
            title = "[필수] 서비스 이용약관",
            checked = termsAccepted,
            onCheckedChange = { termsAccepted = it },
        )
        Spacer(Modifier.height(8.dp))
        ConsentRow(
            title = "[필수] 개인정보 처리방침",
            checked = privacyAccepted,
            onCheckedChange = { privacyAccepted = it },
        )
        Spacer(Modifier.height(8.dp))
        ConsentRow(
            title = "[선택] 소식 및 이벤트 알림",
            checked = marketingAccepted,
            onCheckedChange = { marketingAccepted = it },
        )
    }
}

@Composable
private fun ConsentRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String? = null,
    emphasized: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (emphasized) StarSnapColor.brandSoft else StarSnapColor.surfaceSubtle)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = StarSnapColor.brand,
                checkmarkColor = StarSnapColor.onBrand,
                uncheckedColor = StarSnapColor.borderStrong,
            ),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = StarSnapTypography.label.copy(
                    fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Medium,
                ),
            )
            if (description != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = StarSnapTypography.caption.copy(color = StarSnapColor.textSubtle),
                )
            }
        }
    }
}
