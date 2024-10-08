package com.photo.android.network.report

import com.photo.android.network.auth.dto.rs.SnapReportDto
import com.photo.android.network.auth.dto.rs.UserReportDto
import com.photo.android.network.dto.SliceResponseDto
import com.photo.android.network.report.dto.rq.SnapReportCreateDto
import com.photo.android.network.report.dto.rq.UserReportCreateDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ReportApi {
    @POST("/report/snap") // snap 신고
    fun snapReport(@Body snapReportCreateDto: SnapReportCreateDto)

    @POST("/report/user") // user 신고
    fun userReport(@Body userReportCreateDto: UserReportCreateDto)

    @GET("/report/snap") // snap 신고 조회
    fun getSnapReport(
        @Query("size") size: Int,
        @Query("page") page: Int
    ): SliceResponseDto<SnapReportDto>

    @GET("/report/user") // user 신고 조회
    fun getUserReport(
        @Query("size") size: Int,
        @Query("page") page: Int
    ): SliceResponseDto<UserReportDto>
}