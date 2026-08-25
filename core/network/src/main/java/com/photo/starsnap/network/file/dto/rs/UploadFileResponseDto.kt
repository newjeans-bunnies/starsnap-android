package com.photo.starsnap.network.file.dto.rs

data class UploadFileResponseDto(
    val presignedUrl: String,
    val requiredHeaders: Map<String, String> = emptyMap()
)
