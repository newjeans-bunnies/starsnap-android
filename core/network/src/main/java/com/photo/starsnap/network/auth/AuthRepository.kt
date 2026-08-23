package com.photo.starsnap.network.auth

import com.photo.starsnap.network.auth.dto.rq.LoginDto
import com.photo.starsnap.network.auth.dto.rq.SignupDto
import com.photo.starsnap.network.auth.dto.rq.VerifyEmailRequestDto
import com.photo.starsnap.network.auth.dto.rs.ChangePasswordDto
import com.photo.starsnap.network.auth.dto.rs.TokenDto
import com.photo.starsnap.network.auth.dto.rs.VerifyEmailResponseDto
import com.photo.starsnap.network.dto.StatusDto

interface AuthRepository {
    suspend fun send(email: String): StatusDto
    suspend fun verify(verifyEmailRequestDto: VerifyEmailRequestDto): VerifyEmailResponseDto

    // ----------------------------------------------------------------

    suspend fun login(loginDto: LoginDto): TokenDto
    suspend fun logout()
    suspend fun signup(signupDto: SignupDto): StatusDto
    suspend fun setPassword(password: String): StatusDto
    suspend fun deleteUser(): StatusDto
    suspend fun userRollback(loginDto: LoginDto): TokenDto
    suspend fun changePassword(changePasswordDto: ChangePasswordDto): StatusDto

    // ----------------------------------------------------------------

    suspend fun validUsername(username: String): StatusDto
    suspend fun validEmail(email: String): StatusDto
}
