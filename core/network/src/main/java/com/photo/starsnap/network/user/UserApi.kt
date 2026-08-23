package com.photo.starsnap.network.user

import com.photo.starsnap.network.dto.SliceResponseDto
import com.photo.starsnap.network.dto.StatusDto
import com.photo.starsnap.network.user.dto.Friend
import com.photo.starsnap.network.user.dto.GetUserRequest
import com.photo.starsnap.network.user.dto.UpdateFcmTokenRequest
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Body

interface UserApi {
    @POST("/api/user/friend/request") // 친구 요청 보내기
    suspend fun sendFriendRequest(@Query("user-id") userId: String): StatusDto

    @DELETE("/api/user/friend/request") // 내가 보낸 대기중 요청 취소
    suspend fun cancelFriendRequest(@Query("user-id") userId: String): StatusDto

    @POST("/api/user/friend/accept") // 내가 받은 요청 수락
    suspend fun acceptFriendRequest(@Query("user-id") userId: String): StatusDto

    @DELETE("/api/user/friend/reject") // 내가 받은 요청 거절
    suspend fun rejectFriendRequest(@Query("user-id") userId: String): StatusDto

    @DELETE("/api/user/friend") // 친구 관계 해제
    suspend fun unfriend(@Query("user-id") userId: String): StatusDto

    @GET("/api/user/friend") // 내 친구 목록
    suspend fun getFriends(@Query("page") page: Int, @Query("size") size: Int): SliceResponseDto<Friend>

    @GET("/api/user/friend/received") // 내가 받은 대기중 요청 목록
    suspend fun getReceivedFriendRequests(@Query("page") page: Int, @Query("size") size: Int): SliceResponseDto<Friend>

    @GET("/api/user/friend/sent") // 내가 보낸 대기중 요청 목록
    suspend fun getSentFriendRequests(@Query("page") page: Int, @Query("size") size: Int): SliceResponseDto<Friend>


    @PATCH("/api/user/change-username") // 유저 이름 변경
    suspend fun changeUsername(@Query("username") username: String): StatusDto

    @PATCH("/api/user/change-profile-image") // 유저 프로필 사진 변경
    suspend fun changeProfileImage(@Part("image") image: MultipartBody.Part): StatusDto

    @PATCH("/api/user/update/fcm-token")
    // Internal marker consumed by the auth interceptor before network I/O.
    @Headers("X-StarSnap-Session-Gate: held")
    suspend fun updateFcmToken(@Body request: UpdateFcmTokenRequest): StatusDto

    @PATCH("/api/user/update/privacy") // 비공개 계정 설정 변경
    suspend fun changePrivacy(@Query("is-private") isPrivate: Boolean): GetUserRequest

    @GET("/api/user/get") // 유저 정보 가져오기
    suspend fun getUserData(): GetUserRequest

    @GET("/api/user/by-username") // username 으로 유저 조회
    suspend fun getUserByUsername(@Query("username") username: String): GetUserRequest
}
