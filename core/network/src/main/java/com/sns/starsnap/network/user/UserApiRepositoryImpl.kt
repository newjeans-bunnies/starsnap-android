package com.sns.starsnap.network.user

import com.sns.starsnap.network.dto.SliceResponseDto
import com.sns.starsnap.network.dto.StatusDto
import com.sns.starsnap.network.user.dto.Friend
import com.sns.starsnap.network.user.dto.GetUserRequest
import com.sns.starsnap.network.user.dto.UpdateFcmTokenRequest
import okhttp3.MultipartBody
import javax.inject.Inject

class UserApiRepositoryImpl @Inject constructor(
    private val userApi: UserApi
): UserRepository {
    override suspend fun sendFriendRequest(userId: String): StatusDto {
        return userApi.sendFriendRequest(userId)
    }

    override suspend fun cancelFriendRequest(userId: String): StatusDto {
        return userApi.cancelFriendRequest(userId)
    }

    override suspend fun acceptFriendRequest(userId: String): StatusDto {
        return userApi.acceptFriendRequest(userId)
    }

    override suspend fun rejectFriendRequest(userId: String): StatusDto {
        return userApi.rejectFriendRequest(userId)
    }

    override suspend fun unfriend(userId: String): StatusDto {
        return userApi.unfriend(userId)
    }

    override suspend fun getFriends(
        page: Int,
        size: Int,
    ): SliceResponseDto<Friend> {
        return userApi.getFriends(page, size)
    }

    override suspend fun getReceivedFriendRequests(
        page: Int,
        size: Int,
    ): SliceResponseDto<Friend> {
        return userApi.getReceivedFriendRequests(page, size)
    }

    override suspend fun getSentFriendRequests(
        page: Int,
        size: Int,
    ): SliceResponseDto<Friend> {
        return userApi.getSentFriendRequests(page, size)
    }

    override suspend fun changeUsername(username: String): StatusDto {
        return userApi.changeUsername(username)
    }

    override suspend fun changeProfileImage(image: MultipartBody.Part): StatusDto {
        return userApi.changeProfileImage(image)
    }

    override suspend fun updateFcmToken(request: UpdateFcmTokenRequest): StatusDto {
        return userApi.updateFcmToken(request)
    }

    override suspend fun changePrivacy(isPrivate: Boolean): GetUserRequest {
        return userApi.changePrivacy(isPrivate)
    }

    override suspend fun getUserData(): GetUserRequest {
        return userApi.getUserData()
    }

    override suspend fun getUserByUsername(username: String): GetUserRequest {
        return userApi.getUserByUsername(username)
    }
}