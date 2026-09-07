package com.sns.starsnap.network.star

import com.sns.starsnap.network.dto.SliceResponseDto
import com.sns.starsnap.network.star.dto.StarGroupResponseDto
import com.sns.starsnap.network.star.dto.StarResponseDto
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface StarApi {
    @GET("/api/star")
    suspend fun getStarList(
        @Query("size") size: Int,
        @Query("page") page: Int,
        @Query("star-name") starName: String
    ): SliceResponseDto<StarResponseDto>


    @GET("/api/star-group")
    suspend fun getStarGroupList(
        @Query("size") size: Int,
        @Query("page") page: Int,
        @Query("star-group-name") starGroupName: String
    ): SliceResponseDto<StarGroupResponseDto>
}