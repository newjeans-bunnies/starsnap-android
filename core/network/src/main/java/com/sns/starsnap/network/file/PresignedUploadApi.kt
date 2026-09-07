package com.sns.starsnap.network.file

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.PUT
import retrofit2.http.Url

/** S3 presigned PUT only. This API must be backed by the isolated upload client. */
interface PresignedUploadApi {
    @PUT
    suspend fun uploadFile(
        @Url presignedUrl: String,
        @HeaderMap headers: Map<String, String>,
        @Body file: RequestBody
    )
}
