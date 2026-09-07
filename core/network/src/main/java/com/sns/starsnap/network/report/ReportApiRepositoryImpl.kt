package com.sns.starsnap.network.report

import com.sns.starsnap.network.auth.dto.rs.SnapReportDto
import com.sns.starsnap.network.auth.dto.rs.UserReportDto
import com.sns.starsnap.network.dto.SliceResponseDto
import com.sns.starsnap.network.report.dto.rq.SnapReportCreateDto
import com.sns.starsnap.network.report.dto.rq.UserReportCreateDto
import javax.inject.Inject

class ReportApiRepositoryImpl @Inject constructor(
    val reportApi: ReportApi
): ReportRepository {
    override suspend fun snapReport(snapReportCreateDto: SnapReportCreateDto) {
        return reportApi.snapReport(snapReportCreateDto)
    }

    override suspend fun userReport(userReportCreateDto: UserReportCreateDto) {
        return reportApi.userReport(userReportCreateDto)
    }

    override suspend fun getSnapReport(
        size: Int,
        page: Int,
    ): SliceResponseDto<SnapReportDto> {
        return reportApi.getSnapReport(size, page)
    }

    override suspend fun getUserReport(
        size: Int,
        page: Int,
    ): SliceResponseDto<UserReportDto> {
        return reportApi.getUserReport(size, page)
    }
}