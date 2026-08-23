package com.photo.starsnap.network.snap.dto

data class SnapLikeToggleDto(
    val message: String,
    val status: Int,
    val linkState: String,
    val linked: Boolean
)
