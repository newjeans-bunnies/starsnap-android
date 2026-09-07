package com.sns.starsnap.network.user.dto

data class Friend(
    val id: String,
    val username: String,
    val profileImageUrl: String? = null
)
