package com.photo.starsnap.network.token

import com.photo.starsnap.network.auth.dto.rs.TokenDto
import javax.inject.Inject

class TokenApiRepositoryImpl @Inject constructor(
    private val tokenApi: TokenApi
): TokenRepository {
    override suspend fun reissueToken(): TokenDto {
        return tokenApi.reissueToken()
    }
}