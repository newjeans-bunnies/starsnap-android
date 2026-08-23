package com.photo.starsnap.main.viewmodel.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photo.starsnap.network.report.ReportRepository
import com.photo.starsnap.network.report.dto.rq.UserReportCreateDto
import com.photo.starsnap.network.snap.SnapRepository
import com.photo.starsnap.network.snap.dto.SnapResponseDto
import com.photo.starsnap.network.user.UserRepository
import com.photo.starsnap.network.user.dto.Friend
import com.photo.starsnap.network.user.dto.GetUserRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val snapRepository: SnapRepository,
    private val reportRepository: ReportRepository
): ViewModel() {

    companion object {
        const val TAG = "UserViewModel"
    }

    private val _userData = MutableStateFlow<UserData>(UserData())
    val userData = _userData.asStateFlow()

    private val _userScreenState = MutableStateFlow(UserScreenState())
    val userScreenState = _userScreenState.asStateFlow()

    private val _ownProfileSnapsState = MutableStateFlow(OwnProfileSnapsState())
    val ownProfileSnapsState = _ownProfileSnapsState.asStateFlow()

    fun setUserData(user: UserData) {
        _userData.value = user
    }

    fun clearUserData() {
        _userData.value = UserData()
    }

    fun clearUserScreen() {
        _userScreenState.value = UserScreenState()
    }

    fun clearUserReportError() {
        _userScreenState.value = _userScreenState.value.copy(reportError = null)
    }

    fun getUserData() = viewModelScope.launch {
        runCatching {
            userRepository.getUserData()
        }.onSuccess {
            _userData.value = UserData(
                id = it.userId,
                username = it.username,
                email = it.email,
                authority = it.authority,
                friendCount = it.friendCount.toString(),
                profileImageUrl = it.profileImageUrl.toString(),
                isPrivate = it.isPrivate
            )
            Log.d(TAG, "User data fetched: ${_userData.value}")
        }.onFailure {
            Log.d(TAG, "Failed to fetch user data", it)
            clearUserData()
        }
    }

    fun changeAccountPrivacy(isPrivate: Boolean) = viewModelScope.launch {
        runCatching {
            userRepository.changePrivacy(isPrivate)
        }.onSuccess {
            _userData.value = _userData.value.copy(isPrivate = it.isPrivate)
        }.onFailure {
            Log.d(TAG, "Failed to change account privacy", it)
        }
    }

    fun loadOwnProfileSnaps(username: String) = viewModelScope.launch {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isBlank()) {
            _ownProfileSnapsState.value = OwnProfileSnapsState()
            return@launch
        }

        _ownProfileSnapsState.value = _ownProfileSnapsState.value.copy(
            loading = true,
            error = null
        )

        runCatching {
            loadAllSnapsCreatedBy(trimmedUsername)
        }.onSuccess { snaps ->
            _ownProfileSnapsState.value = OwnProfileSnapsState(snaps = snaps)
        }.onFailure {
            Log.d(TAG, "Failed to fetch own profile snaps", it)
            _ownProfileSnapsState.value = OwnProfileSnapsState(
                error = "프로필 스냅을 불러오지 못했습니다."
            )
        }
    }

    fun loadUserScreen(username: String) = viewModelScope.launch {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isBlank()) {
            clearUserScreen()
            return@launch
        }

        _userScreenState.value = _userScreenState.value.copy(
            loading = true,
            error = null,
            friendActionSubmitting = false,
            reportSubmitting = false,
            reportError = null,
            requestedUsername = trimmedUsername,
        )

        runCatching {
            val profile = userRepository.getUserByUsername(trimmedUsername)
            val friends = userRepository.getFriends(page = 0, size = 200).content
            val received = userRepository.getReceivedFriendRequests(page = 0, size = 200).content
            val sent = userRepository.getSentFriendRequests(page = 0, size = 200).content
            val snaps = loadAllSnapsCreatedBy(trimmedUsername)

            fun matches(item: Friend) = item.id == profile.userId || item.username == profile.username

            val relation = when {
                friends.any(::matches) -> FriendRelation.FRIEND
                received.any(::matches) -> FriendRelation.REQUEST_RECEIVED
                sent.any(::matches) -> FriendRelation.REQUEST_SENT
                else -> FriendRelation.NONE
            }

            UserScreenState(
                loading = false,
                requestedUsername = trimmedUsername,
                profile = profile,
                snaps = snaps,
                relation = relation,
                isSelf = profile.userId == _userData.value.id || profile.username == _userData.value.username,
            )
        }.onSuccess {
            _userScreenState.value = it
        }.onFailure {
            Log.d(TAG, "Failed to fetch user screen", it)
            _userScreenState.value = UserScreenState(
                loading = false,
                requestedUsername = trimmedUsername,
                error = "프로필 스냅을 불러오지 못했습니다."
            )
        }
    }

    fun handleFriendAction() = viewModelScope.launch {
        val currentState = _userScreenState.value
        val profile = currentState.profile ?: return@launch
        if (currentState.friendActionSubmitting || currentState.isSelf) return@launch

        _userScreenState.value = currentState.copy(friendActionSubmitting = true)

        runCatching {
            when (currentState.relation) {
                FriendRelation.NONE -> userRepository.sendFriendRequest(profile.userId)
                FriendRelation.REQUEST_SENT -> userRepository.cancelFriendRequest(profile.userId)
                FriendRelation.FRIEND -> userRepository.unfriend(profile.userId)
                FriendRelation.REQUEST_RECEIVED -> return@launch
            }
        }.onSuccess {
            val newRelation = when (currentState.relation) {
                FriendRelation.NONE -> FriendRelation.REQUEST_SENT
                FriendRelation.REQUEST_SENT -> FriendRelation.NONE
                FriendRelation.FRIEND -> FriendRelation.NONE
                FriendRelation.REQUEST_RECEIVED -> currentState.relation
            }
            val friendDelta = when (currentState.relation) {
                FriendRelation.FRIEND -> -1
                FriendRelation.NONE -> 0
                else -> 0
            }
            _userScreenState.value = currentState.copy(
                friendActionSubmitting = false,
                relation = newRelation,
                profile = profile.copy(friendCount = (profile.friendCount + friendDelta).coerceAtLeast(0))
            )
        }.onFailure {
            Log.d(TAG, "Failed to handle friend action", it)
            _userScreenState.value = currentState.copy(
                friendActionSubmitting = false,
                error = "친구 상태를 변경하지 못했습니다."
            )
        }
    }

    fun acceptFriendRequestOnUserScreen() = viewModelScope.launch {
        val currentState = _userScreenState.value
        val profile = currentState.profile ?: return@launch
        if (currentState.friendActionSubmitting || currentState.isSelf) return@launch
        if (currentState.relation != FriendRelation.REQUEST_RECEIVED) return@launch

        _userScreenState.value = currentState.copy(friendActionSubmitting = true)

        runCatching {
            userRepository.acceptFriendRequest(profile.userId)
        }.onSuccess {
            _userScreenState.value = currentState.copy(
                friendActionSubmitting = false,
                relation = FriendRelation.FRIEND,
                profile = profile.copy(friendCount = profile.friendCount + 1)
            )
        }.onFailure {
            Log.d(TAG, "Failed to accept friend request", it)
            _userScreenState.value = currentState.copy(
                friendActionSubmitting = false,
                error = "친구 요청을 수락하지 못했습니다."
            )
        }
    }

    fun rejectFriendRequestOnUserScreen() = viewModelScope.launch {
        val currentState = _userScreenState.value
        val profile = currentState.profile ?: return@launch
        if (currentState.friendActionSubmitting || currentState.isSelf) return@launch
        if (currentState.relation != FriendRelation.REQUEST_RECEIVED) return@launch

        _userScreenState.value = currentState.copy(friendActionSubmitting = true)

        runCatching {
            userRepository.rejectFriendRequest(profile.userId)
        }.onSuccess {
            _userScreenState.value = currentState.copy(
                friendActionSubmitting = false,
                relation = FriendRelation.NONE,
            )
        }.onFailure {
            Log.d(TAG, "Failed to reject friend request", it)
            _userScreenState.value = currentState.copy(
                friendActionSubmitting = false,
                error = "친구 요청을 거절하지 못했습니다."
            )
        }
    }

    fun reportUserOnUserScreen(explanation: String, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        val profile = _userScreenState.value.profile ?: return@launch
        if (_userScreenState.value.reportSubmitting || _userScreenState.value.isSelf || profile.userId.isBlank()) {
            return@launch
        }

        _userScreenState.value = _userScreenState.value.copy(
            reportSubmitting = true,
            reportError = null,
        )

        runCatching {
            reportRepository.userReport(
                UserReportCreateDto(
                    explanation = explanation,
                    userId = profile.userId,
                )
            )
        }.onSuccess {
            _userScreenState.value = _userScreenState.value.copy(
                reportSubmitting = false,
                reportError = null,
            )
            onSuccess()
        }.onFailure {
            Log.d(TAG, "Failed to report user", it)
            _userScreenState.value = _userScreenState.value.copy(
                reportSubmitting = false,
                reportError = "신고 접수에 실패했습니다. 잠시 후 다시 시도해주세요.",
            )
        }
    }

    private suspend fun loadAllSnapsCreatedBy(username: String): List<SnapResponseDto> {
        val collected = mutableListOf<SnapResponseDto>()
        var page = 0

        while (true) {
            val response = snapRepository.getFeedSnap(page = page, size = 100)
            collected += response.content.filter { it.createdUser.username == username }
            if (response.last) break
            page += 1
        }

        return collected
    }
}

data class UserData(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val authority: String = "",
    val friendCount: String = "",
    val profileImageUrl: String = "",
    val isPrivate: Boolean = false
)

data class OwnProfileSnapsState(
    val loading: Boolean = false,
    val error: String? = null,
    val snaps: List<SnapResponseDto> = emptyList()
)

enum class FriendRelation {
    NONE,
    REQUEST_SENT,
    REQUEST_RECEIVED,
    FRIEND,
}

data class UserScreenState(
    val loading: Boolean = false,
    val friendActionSubmitting: Boolean = false,
    val reportSubmitting: Boolean = false,
    val requestedUsername: String = "",
    val error: String? = null,
    val reportError: String? = null,
    val profile: GetUserRequest? = null,
    val snaps: List<SnapResponseDto> = emptyList(),
    val relation: FriendRelation = FriendRelation.NONE,
    val isSelf: Boolean = false,
)