package com.sns.starsnap.network.auth.dto.rs

data class ChangePasswordDto(
    val userId: String,
    val password: String,
    val newPassword: String
)