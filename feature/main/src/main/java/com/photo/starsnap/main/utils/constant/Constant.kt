package com.photo.starsnap.main.utils.constant

object Constant {
    const val SNAP_SIZE = 50
    const val GALLERY_PHOTO_SIZE = 50
    const val STAR_SIZE = 50
    const val STAR_GROUP_SIZE = 50
    private const val IMAGE_BASE_URL = "https://starsnap.kr"

    fun getImageUrl(imageKey: String?): String? {
        val normalizedKey = imageKey
            ?.trim()
            ?.takeIf { it.isNotEmpty() && it.lowercase() != "null" }
            ?.trimStart('/')
            ?: return null

        if (normalizedKey.startsWith("http://") || normalizedKey.startsWith("https://")) {
            return normalizedKey
        }

        return "$IMAGE_BASE_URL/$normalizedKey"
    }
}