package com.sns.starsnap.network.snap.dto

data class CommentDto(
    val profileKey: String?,
    val username: String,
    val content: String,
    val createdAt: String?,
    val modifiedAt: String?
)

data class CreateCommentRequestDto(
    val content: String,
    val snapId: String
)
