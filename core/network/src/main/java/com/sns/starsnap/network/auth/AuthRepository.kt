package com.sns.starsnap.network.auth

import com.sns.starsnap.network.auth.dto.rq.LoginDto
import com.sns.starsnap.network.auth.dto.rq.SignupDto
import com.sns.starsnap.network.auth.dto.rq.VerifyEmailRequestDto
import com.sns.starsnap.network.auth.dto.PasswordResetCodeRequestDto
import com.sns.starsnap.network.auth.dto.PasswordResetConfirmRequestDto
import com.sns.starsnap.network.auth.dto.PasswordResetEmailRequestDto
import com.sns.starsnap.network.auth.dto.PasswordResetRequestResponseDto
import com.sns.starsnap.network.auth.dto.PasswordResetVerifyResponseDto
import com.sns.starsnap.network.auth.dto.rs.ChangePasswordDto
import com.sns.starsnap.network.auth.dto.rs.TokenDto
import com.sns.starsnap.network.auth.dto.rs.VerifyEmailResponseDto
import com.sns.starsnap.network.dto.StatusDto

interface AuthRepository {
    suspend fun requestPasswordReset(
        request: PasswordResetEmailRequestDto,
    ): PasswordResetRequestResponseDto
    suspend fun verifyPasswordReset(
        request: PasswordResetCodeRequestDto,
    ): PasswordResetVerifyResponseDto
    suspend fun confirmPasswordReset(request: PasswordResetConfirmRequestDto)

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
