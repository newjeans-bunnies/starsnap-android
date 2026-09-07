package com.sns.starsnap.network.snap.dto

import com.google.gson.annotations.SerializedName

data class CreateSnapRequestDto(
    val title: String,
    val description: String,
    val source: String,
    val tags: List<String>,
    val photos: List<String>,
    val starIds: List<String>,
    val starGroupIds: List<String>,
    val commentState: Boolean
)

data class SnapDto(
    val snapId: String,
    val title: String,
    val createdAt: String,
    @SerializedName("imageKey")
    private val directImageKey: String? = null,
    val photos: List<SnapPhotoDto> = emptyList(),
    val tags: List<String>,
    val source: String,
    val type: String,
    val size: Long,
    val dateTaken: String,
    val comments: List<CommentDto>,
    val likeState: Boolean = false,
    val saveState: Boolean = false,
) {
    val imageKey: String?
        get() = directImageKey ?: photos.firstOrNull()?.fileKey

    val imageAspectRatio: Float?
        get() {
            val photo = photos.firstOrNull() ?: return null
            val width = photo.width ?: return null
            val height = photo.height ?: return null
            if (width <= 0 || height <= 0) return null
            return width.toFloat() / height.toFloat()
        }
}

data class SnapPhotoDto(
    val fileKey: String,
    val width: Int? = null,
    val height: Int? = null
)


data class SnapResponseDto(
    val createdUser: SnapUserDto,
    val snapData: SnapDto
)

data class SnapUserDto(
    val username: String,
    val imageKey: String?
)
