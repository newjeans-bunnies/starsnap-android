package com.sns.starsnap.network.snap.dto

import java.time.LocalDateTime

data class StarDto(
    val name: String,
    val gender: String,
    val birthday: LocalDateTime,
    val nickname: String,
    val explanation: String?
)