package com.photo.starsnap.network.token


import com.photo.starsnap.network.auth.dto.rs.TokenDto

interface TokenRepository {
    // 쿠키 기반으로 변경: 서버의 쿠키(예: refresh cookie)를 사용하므로
    // 재발급은 별도 토큰 인자 없이 호출합니다.
    suspend fun reissueToken(): TokenDto
}