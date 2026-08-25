package com.photo.starsnap.network.file

import com.photo.starsnap.network.file.dto.rq.UploadFileRequestDto
import com.photo.starsnap.network.file.dto.rs.UploadFileResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FileApi {
    @POST("/api/file/photo")
    suspend fun createPhotoPresidentUrl(
        @Body uploadRequest: UploadFileRequestDto
    ): Response<UploadFileResponseDto>

    @POST("/api/file/video")
    suspend fun createVideoPresidentUrl(
        @Body uploadRequest: UploadFileRequestDto
    ): Response<UploadFileResponseDto>
}
