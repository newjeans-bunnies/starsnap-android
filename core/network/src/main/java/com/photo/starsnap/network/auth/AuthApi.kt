package com.photo.starsnap.network.auth

import com.photo.starsnap.network.auth.dto.rq.LoginDto
import com.photo.starsnap.network.auth.dto.rq.SignupDto
import com.photo.starsnap.network.auth.dto.rq.VerifyEmailRequestDto
import com.photo.starsnap.network.auth.dto.rs.ChangePasswordDto
import com.photo.starsnap.network.auth.dto.rs.TokenDto
import com.photo.starsnap.network.auth.dto.rs.VerifyEmailResponseDto
import com.photo.starsnap.network.dto.StatusDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("/api/auth/email/send") // 이메일 인증번호 전송
    @Headers("Auth: false")
    suspend fun send(@Query("email") email: String): StatusDto

    @POST("/api/auth/email/verify") // 인증번호 확인
    @Headers("Auth: false")
    suspend fun verify(@Body verifyEmailRequestDto: VerifyEmailRequestDto): VerifyEmailResponseDto

    // ----------------------------------------------------------------

    @POST("/api/auth/login") // 로그인
    @Headers("Auth: false")
    suspend fun login(@Body loginDto: LoginDto): TokenDto

    @POST("/api/auth/logout")
    @Headers("Auth: false")
    suspend fun logout()

    @POST("/api/auth/signup") // 회원가입
    @Headers("Auth: false")
    suspend fun signup(@Body signupDto: SignupDto): StatusDto

    @PATCH("/api/auth/pw-set")
    suspend fun setPassword(@Query("password") password: String): StatusDto

    @PATCH("/api/auth/secession") // 유저 삭제
    suspend fun deleteUser(): StatusDto

    @POST("/api/auth/user/rollback")
    @Headers("Auth: false")
    suspend fun userRollback(@Body loginDto: LoginDto): TokenDto

    @PATCH("/api/auth/pw-change") // 비밀번호 변경
    @Headers("Auth: false")
    suspend fun changePassword(
        @Body changePasswordDto: ChangePasswordDto,
    ): StatusDto

    // ----------------------------------------------------------------

    @GET("/api/auth/valid/username") // 닉네임 사용 가능 한지 확인
    @Headers("Auth: false")
    suspend fun validUsername(
        @Query("username") username: String
    ): StatusDto


    @GET("/api/auth/valid/email") // 이메일 사용 가능 한지 확인
    @Headers("Auth: false")
    suspend fun validEmail(
        @Query("email") email: String
    ): StatusDto
}
