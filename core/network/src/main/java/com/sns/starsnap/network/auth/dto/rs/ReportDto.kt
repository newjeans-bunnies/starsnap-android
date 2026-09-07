package com.sns.starsnap.network.auth.dto.rs

data class SnapReportDto(
    val id: String,
    val createdAt: String,
    var explanation: String,
    val reporterId: String,
    val reporterUsername: String,
    val reporterEmail: String,
    val reporterAuthority: String,
    val snapId: String,
    val snapTitle: String,
    val snapCreatedAt: String,
    val snapSize: Long,
    val snapType: String,
    val snapSource: String,
    val snapImageKey: String,
    val snapDateTaken: String,
    val snapImageWidth: Int,
    val snapImageHeight: Int
)

data class UserReportDto(
    val id: String,
    val reporterId: String,
    val reporterUsername: String,
    val reporterEmail: String,
    val reporterAuthority: String,
    val defendantId: String,
    val defendantUsername: String,
    val defendantEmail: String,
    val defendantAuthority: String,
    val createdAt: String
)