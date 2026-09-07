package com.sns.starsnap.network.auth.dto.rq

data class VerifyEmailRequestDto(
    val email: String,
    val verifyCode: String
)
