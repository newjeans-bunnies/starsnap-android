package com.sns.starsnap.network.token

import com.sns.starsnap.network.auth.dto.rs.TokenDto
import javax.inject.Inject

class TokenApiRepositoryImpl @Inject constructor(
    private val tokenApi: TokenApi
): TokenRepository {
    override suspend fun reissueToken(): TokenDto {
        return tokenApi.reissueToken()
    }
}