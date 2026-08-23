package com.photo.starsnap.network.user

import com.photo.starsnap.network.dto.SliceResponseDto
import com.photo.starsnap.network.dto.StatusDto
import com.photo.starsnap.network.user.dto.Friend
import com.photo.starsnap.network.user.dto.GetUserRequest
import com.photo.starsnap.network.user.dto.UpdateFcmTokenRequest
import okhttp3.MultipartBody

interface UserRepository {
    suspend fun sendFriendRequest(userId: String): StatusDto
    suspend fun cancelFriendRequest(userId: String): StatusDto
    suspend fun acceptFriendRequest(userId: String): StatusDto
    suspend fun rejectFriendRequest(userId: String): StatusDto
    suspend fun unfriend(userId: String): StatusDto

    suspend fun getFriends(page: Int, size: Int): SliceResponseDto<Friend>
    suspend fun getReceivedFriendRequests(page: Int, size: Int): SliceResponseDto<Friend>
    suspend fun getSentFriendRequests(page: Int, size: Int): SliceResponseDto<Friend>

    suspend fun changeUsername(username: String): StatusDto
    suspend fun changeProfileImage(image: MultipartBody.Part): StatusDto
    suspend fun updateFcmToken(request: UpdateFcmTokenRequest): StatusDto
    suspend fun changePrivacy(isPrivate: Boolean): GetUserRequest

    suspend fun getUserData(): GetUserRequest
    suspend fun getUserByUsername(username: String): GetUserRequest
}