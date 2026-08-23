package com.photo.starsnap.network.star.dto

import java.time.LocalDate
import java.time.LocalDateTime


data class CreateStarRequestDto(
    val name: String, // 이름
    val gender: GenderType, // 성별
    val birthday: LocalDateTime, // 생일
    val nickname: String, // 닉네임
    val explanation: String? // 설명
)

data class StarResponseDto(
    val id: String,
    val name: String,
    val gender: GenderType,
    val birthday: String?,
    val nickname: String?,
    val explanation: String?,
    val imageKey: String?,
    val createdAt: String?,
    val starGroup: StarGroupResponseDto?
)

data class UpdateStarRequestDto(
    val id: String,
    val name: String, // 이름
    val gender: GenderType, // 성별
    val birthday: LocalDate, // 생일
    val nickname: String, // 닉네임
    val explanation: String? // 설명
)


data class StarImageResponseDto(
    val imageKey: String
)

enum class GenderType {
    MALE, FEMALE
}