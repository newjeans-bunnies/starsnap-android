package com.photo.starsnap.network.token.dto.rs

data class TokenDto(
    val expiredAt: String,
    val authority: String
)
