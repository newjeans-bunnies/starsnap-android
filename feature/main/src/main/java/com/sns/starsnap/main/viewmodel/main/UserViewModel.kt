package com.sns.starsnap.main.viewmodel.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sns.starsnap.network.report.ReportRepository
import com.sns.starsnap.network.report.dto.rq.UserReportCreateDto
import com.sns.starsnap.network.snap.SnapRepository
import com.sns.starsnap.network.snap.dto.SnapResponseDto
import com.sns.starsnap.network.user.UserRepository
import com.sns.starsnap.network.user.dto.Friend
import com.sns.starsnap.network.user.dto.GetUserRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
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

    private val _userDataLoading = MutableStateFlow(true)
    val userDataLoading = _userDataLoading.asStateFlow()

    private val _userDataError = MutableStateFlow<String?>(null)
    val userDataError = _userDataError.asStateFlow()

    private val userDataLoadGeneration = AtomicLong(0)
    private val userScreenLoadGeneration = AtomicLong(0)
    private val ownProfileSnapsLoadGeneration = AtomicLong(0)
    private var userDataLoadJob: Job? = null
    private var userScreenLoadJob: Job? = null
    private var ownProfileSnapsLoadJob: Job? = null

    private val _userScreenState = MutableStateFlow(UserScreenState())
    val userScreenState = _userScreenState.asStateFlow()

    private val _ownProfileSnapsState = MutableStateFlow(OwnProfileSnapsState())
    val ownProfileSnapsState = _ownProfileSnapsState.asStateFlow()

    fun setUserData(user: UserData) {
        userDataLoadJob?.cancel()
        userDataLoadGeneration.incrementAndGet()
        _userData.value = user
        _userDataError.value = null
        _userDataLoading.value = false
    }

    fun clearUserData() {
        userDataLoadJob?.cancel()
        userDataLoadGeneration.incrementAndGet()
        _userData.value = UserData()
        _userDataError.value = null
        _userDataLoading.value = false
    }

    fun clearUserScreen() {
        userScreenLoadJob?.cancel()
        userScreenLoadGeneration.incrementAndGet()
        _userScreenState.value = UserScreenState()
    }

    fun clearUserReportError() {
        _userScreenState.value = _userScreenState.value.copy(reportError = null)
    }

    fun getUserData() {
        userDataLoadJob?.cancel()
        val generation = userDataLoadGeneration.incrementAndGet()
        _userDataLoading.value = true
        _userDataError.value = null
        userDataLoadJob = viewModelScope.launch {
            try {
                val response = userRepository.getUserData()
                if (generation == userDataLoadGeneration.get()) {
                    _userData.value = UserData(
                        id = response.userId,
                        username = response.username,
                        email = response.email,
                        authority = response.authority,
                        friendCount = response.friendCount.toString(),
                        profileImageUrl = response.profileImageUrl.toString(),
                        isPrivate = response.isPrivate,
                    )
                    _userDataError.value = null
                    Log.d(TAG, "User data fetched: ${_userData.value}")
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (generation == userDataLoadGeneration.get()) {
                    Log.d(TAG, "Failed to fetch user data", error)
                    _userDataError.value = "프로필 정보를 불러오지 못했어요."
                }
            } finally {
                if (generation == userDataLoadGeneration.get()) {
                    _userDataLoading.value = false
                    userDataLoadJob = null
                }
            }
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

    fun loadOwnProfileSnaps(username: String) {
        val trimmedUsername = username.trim()
        ownProfileSnapsLoadJob?.cancel()
        val generation = ownProfileSnapsLoadGeneration.incrementAndGet()
        if (trimmedUsername.isBlank()) {
            _ownProfileSnapsState.value = OwnProfileSnapsState()
            return
        }

        val cachedSnaps = _ownProfileSnapsState.value
            .takeIf { it.requestedUsername == trimmedUsername }
            ?.snaps
            .orEmpty()
        _ownProfileSnapsState.value = OwnProfileSnapsState(
            loading = true,
            requestedUsername = trimmedUsername,
            snaps = cachedSnaps,
        )

        ownProfileSnapsLoadJob = viewModelScope.launch {
            try {
                val snaps = loadAllSnapsCreatedBy(trimmedUsername)
                if (isCurrentOwnProfileSnapsRequest(generation, trimmedUsername)) {
                    _ownProfileSnapsState.value = OwnProfileSnapsState(
                        requestedUsername = trimmedUsername,
                        snaps = snaps,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (isCurrentOwnProfileSnapsRequest(generation, trimmedUsername)) {
                    Log.d(TAG, "Failed to fetch own profile snaps", error)
                    _ownProfileSnapsState.value = _ownProfileSnapsState.value.copy(
                        loading = false,
                        error = "프로필 스냅을 불러오지 못했습니다.",
                    )
                }
            } finally {
                if (generation == ownProfileSnapsLoadGeneration.get()) {
                    ownProfileSnapsLoadJob = null
                }
            }
        }
    }

    private fun isCurrentOwnProfileSnapsRequest(generation: Long, username: String): Boolean =
        generation == ownProfileSnapsLoadGeneration.get() &&
            _ownProfileSnapsState.value.requestedUsername == username

    fun loadUserScreen(username: String) {
        val trimmedUsername = username.trim()
        userScreenLoadJob?.cancel()
        val generation = userScreenLoadGeneration.incrementAndGet()
        if (trimmedUsername.isBlank()) {
            _userScreenState.value = UserScreenState()
            return
        }

        _userScreenState.value = UserScreenState(
            loading = true,
            requestedUsername = trimmedUsername,
        )

        userScreenLoadJob = viewModelScope.launch {
            try {
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

                if (isCurrentUserScreenRequest(generation, trimmedUsername)) {
                    _userScreenState.value = UserScreenState(
                        loading = false,
                        requestedUsername = trimmedUsername,
                        profile = profile,
                        snaps = snaps,
                        relation = relation,
                        isSelf = profile.userId == _userData.value.id ||
                            profile.username == _userData.value.username,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (isCurrentUserScreenRequest(generation, trimmedUsername)) {
                    Log.d(TAG, "Failed to fetch user screen", error)
                    _userScreenState.value = UserScreenState(
                        loading = false,
                        requestedUsername = trimmedUsername,
                        error = "프로필 스냅을 불러오지 못했습니다.",
                    )
                }
            } finally {
                if (generation == userScreenLoadGeneration.get()) {
                    userScreenLoadJob = null
                }
            }
        }
    }

    private fun isCurrentUserScreenRequest(generation: Long, username: String): Boolean =
        generation == userScreenLoadGeneration.get() &&
            _userScreenState.value.requestedUsername == username

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
    val requestedUsername: String = "",
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
