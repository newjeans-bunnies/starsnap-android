package com.sns.starsnap.network.file

import com.sns.starsnap.network.file.dto.rq.UploadFileRequestDto
import com.sns.starsnap.network.file.dto.rs.UploadFileResponseDto
import okhttp3.RequestBody
import retrofit2.Response


interface FileRepository {
    suspend fun createPhotoPresidentUrl(
        uploadRequest: UploadFileRequestDto
    ): Response<UploadFileResponseDto>

    suspend fun createVideoPresidentUrl(
        uploadRequest: UploadFileRequestDto
    ): Response<UploadFileResponseDto>

    suspend fun uploadFile(
        presignedUrl: String,
        contentType: String,
        requiredHeaders: Map<String, String>,
        file: RequestBody
    )
}
