package com.photo.starsnap.network.user.dto

data class GetUserRequest(
    val userId: String,
    val username: String,
    val email: String,
    val profileImageUrl: String? = null,
    val authority: String,
    val friendCount: Int,
    val isPrivate: Boolean = false
)
