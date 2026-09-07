package com.sns.starsnap.network.star

import com.sns.starsnap.network.dto.SliceResponseDto
import com.sns.starsnap.network.star.dto.StarGroupResponseDto
import com.sns.starsnap.network.star.dto.StarResponseDto

interface StarRepository {
    suspend fun getStarList(size: Int, page: Int, starName: String): SliceResponseDto<StarResponseDto>
    suspend fun getStarGroupList(size: Int, page: Int, starGroupName: String): SliceResponseDto<StarGroupResponseDto>
}