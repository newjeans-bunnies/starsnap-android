package com.sns.starsnap.network.report

import com.sns.starsnap.network.auth.dto.rs.SnapReportDto
import com.sns.starsnap.network.auth.dto.rs.UserReportDto
import com.sns.starsnap.network.dto.SliceResponseDto
import com.sns.starsnap.network.report.dto.rq.SnapReportCreateDto
import com.sns.starsnap.network.report.dto.rq.UserReportCreateDto

interface ReportRepository {
    suspend fun snapReport(snapReportCreateDto: SnapReportCreateDto)
    suspend fun userReport(userReportCreateDto: UserReportCreateDto)

    suspend fun getSnapReport(size: Int, page: Int): SliceResponseDto<SnapReportDto>
    suspend fun getUserReport(size: Int, page: Int): SliceResponseDto<UserReportDto>
}