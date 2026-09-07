package com.sns.starsnap.network.report

import com.sns.starsnap.network.auth.dto.rs.SnapReportDto
import com.sns.starsnap.network.auth.dto.rs.UserReportDto
import com.sns.starsnap.network.dto.SliceResponseDto
import com.sns.starsnap.network.report.dto.rq.SnapReportCreateDto
import com.sns.starsnap.network.report.dto.rq.UserReportCreateDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ReportApi {
    @POST("/api/report/snap") // snap 신고
    suspend fun snapReport(@Body snapReportCreateDto: SnapReportCreateDto)

    @POST("/api/report/user") // user 신고
    suspend fun userReport(@Body userReportCreateDto: UserReportCreateDto)

    @GET("/api/report/snap") // snap 신고 조회
    suspend fun getSnapReport(
        @Query("size") size: Int,
        @Query("page") page: Int
    ): SliceResponseDto<SnapReportDto>

    @GET("/api/report/user") // user 신고 조회
    suspend fun getUserReport(
        @Query("size") size: Int,
        @Query("page") page: Int
    ): SliceResponseDto<UserReportDto>
}