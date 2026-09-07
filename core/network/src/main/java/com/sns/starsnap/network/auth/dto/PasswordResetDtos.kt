package com.sns.starsnap.network.auth.dto

data class PasswordResetEmailRequestDto(
    val email: String,
)

data class PasswordResetCodeRequestDto(
    val email: String,
    val code: String,
)

data class PasswordResetConfirmRequestDto(
    val resetToken: String,
    val newPassword: String,
)

data class PasswordResetRequestResponseDto(
    val message: String,
    val expiresInSeconds: Long,
    val resendAfterSeconds: Long,
)

data class PasswordResetVerifyResponseDto(
    val resetToken: String,
    val expiresInSeconds: Long,
)
