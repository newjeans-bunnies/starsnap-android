package com.sns.starsnap.network.file

import com.sns.starsnap.network.file.dto.rq.UploadFileRequestDto
import com.sns.starsnap.network.file.dto.rs.UploadFileResponseDto
import okhttp3.RequestBody
import retrofit2.Response
import java.util.Locale
import javax.inject.Inject

class FileApiRepositoryImpl @Inject constructor(
    private val fileApi: FileApi,
    private val presignedUploadApi: PresignedUploadApi
) : FileRepository {
    override suspend fun createPhotoPresidentUrl(uploadRequest: UploadFileRequestDto): Response<UploadFileResponseDto> {
        return fileApi.createPhotoPresidentUrl(uploadRequest)
    }

    override suspend fun createVideoPresidentUrl(uploadRequest: UploadFileRequestDto): Response<UploadFileResponseDto> {
        return fileApi.createVideoPresidentUrl(uploadRequest)
    }

    override suspend fun uploadFile(
        presignedUrl: String,
        contentType: String,
        requiredHeaders: Map<String, String>,
        file: RequestBody
    ) {
        return presignedUploadApi.uploadFile(
            presignedUrl = presignedUrl,
            headers = buildPresignedUploadHeaders(contentType, requiredHeaders),
            file = file
        )
    }

}

internal fun buildPresignedUploadHeaders(
    contentType: String,
    requiredHeaders: Map<String, String>
): Map<String, String> = buildMap {
    requiredHeaders.forEach { (name, value) ->
        if (name.lowercase(Locale.ROOT) !in SENSITIVE_UPLOAD_HEADERS &&
            !name.equals("Content-Type", ignoreCase = true)
        ) {
            put(name, value)
        }
    }
    put("Content-Type", contentType)
}

private val SENSITIVE_UPLOAD_HEADERS = setOf(
    "authorization",
    "cookie",
    "proxy-authorization"
)
