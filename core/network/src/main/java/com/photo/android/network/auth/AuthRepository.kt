package com.photo.android.network.auth

import com.photo.android.network.auth.dto.rq.LoginDto
import com.photo.android.network.auth.dto.rq.SignupDto
import com.photo.android.network.auth.dto.rq.VerifyEmailRequestDto
import com.photo.android.network.auth.dto.rs.ChangePasswordDto
import com.photo.android.network.auth.dto.rs.TokenDto
import com.photo.android.network.auth.dto.rs.VerifyEmailResponseDto
import com.photo.android.network.dto.StatusDto

interface AuthRepository {
    fun send(email: String): StatusDto
    fun verify(verifyEmailRequestDto: VerifyEmailRequestDto): VerifyEmailResponseDto
    fun login(loginDto: LoginDto): TokenDto
    fun signup(signupDto: SignupDto): StatusDto
    fun delete(): StatusDto
    fun reissueToken(refreshToken: String, accessToken: String): retrofit2.Response<TokenDto>
    fun changePassword(changePasswordDto: ChangePasswordDto): StatusDto
}