package com.sns.starsnap.network.auth.dto.rs

data class TokenDto(
    val expiredAt: String,
    val authority: String
)
