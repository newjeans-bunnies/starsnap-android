package com.sns.starsnap.network.auth.dto.rq

data class SignupDto(
    val username: String,
    val password: String,
    val email: String,
    val token: String
)
