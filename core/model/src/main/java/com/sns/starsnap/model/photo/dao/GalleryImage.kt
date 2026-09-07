package com.sns.starsnap.model.photo.dao

import android.net.Uri

data class GalleryImage(
    val id: Long,
    val filepath: String,
    val uri: Uri,
    val size: String,
    val type: String,
)
