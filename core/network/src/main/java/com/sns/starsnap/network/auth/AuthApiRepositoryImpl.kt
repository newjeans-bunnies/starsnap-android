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
import javax.inject.Inject


class AuthApiRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
): AuthRepository {
    override suspend fun requestPasswordReset(
        request: PasswordResetEmailRequestDto,
    ): PasswordResetRequestResponseDto = authApi.requestPasswordReset(request)

    override suspend fun verifyPasswordReset(
        request: PasswordResetCodeRequestDto,
    ): PasswordResetVerifyResponseDto = authApi.verifyPasswordReset(request)

    override suspend fun confirmPasswordReset(request: PasswordResetConfirmRequestDto) {
        authApi.confirmPasswordReset(request)
    }

    override suspend fun send(email: String): StatusDto {
        return authApi.send(email)
    }

    override suspend fun verify(verifyEmailRequestDto: VerifyEmailRequestDto): VerifyEmailResponseDto {
        return authApi.verify(verifyEmailRequestDto)
    }

    // ----------------------------------------------------------------

    override suspend fun login(loginDto: LoginDto): TokenDto {
        return authApi.login(loginDto)
    }

    override suspend fun logout() {
        authApi.logout()
    }

    override suspend fun signup(signupDto: SignupDto): StatusDto {
        return authApi.signup(signupDto)
    }

    override suspend fun setPassword(password: String): StatusDto {
        return authApi.setPassword(password)
    }

    override suspend fun deleteUser(): StatusDto {
        return authApi.deleteUser()
    }

    override suspend fun userRollback(loginDto: LoginDto): TokenDto {
        return authApi.userRollback(loginDto)
    }

    override suspend fun changePassword(changePasswordDto: ChangePasswordDto): StatusDto {
        return authApi.changePassword(changePasswordDto)
    }

    override suspend fun validUsername(username: String): StatusDto {
        return authApi.validUsername(username)
    }

    override suspend fun validEmail(email: String): StatusDto {
        return authApi.validEmail(email)
    }
}
