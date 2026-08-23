package com.photo.starsnap.main.viewmodel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.photo.starsnap.main.utils.constant.Constant.SNAP_SIZE
import com.photo.starsnap.main.utils.paging.SnapPagingSource
import com.photo.starsnap.network.snap.SnapRepository
import com.photo.starsnap.network.snap.dto.CommentDto
import com.photo.starsnap.network.snap.dto.SnapResponseDto
import com.photo.starsnap.network.star.StarRepository
import com.photo.starsnap.network.star.dto.StarGroupResponseDto
import com.photo.starsnap.network.star.dto.StarResponseDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class SnapViewModel @Inject constructor(
    private val snapRepository: SnapRepository,
    private val starRepository: StarRepository
) : ViewModel() {

    companion object {
        const val TAG = "SnapViewModel"
    }

    val snapList = Pager(
        config = PagingConfig(
            pageSize = SNAP_SIZE,
            enablePlaceholders = false // or false, 취향/요건에 따라
        ),
        pagingSourceFactory = { SnapPagingSource(snapRepository) }
    ).flow.cachedIn(viewModelScope)

    private val _snapState = MutableStateFlow(SnapState())
    val snapState: StateFlow<SnapState> get() = _snapState

    private val _connectedState = MutableStateFlow(ConnectedEntities())
    val connectedState: StateFlow<ConnectedEntities> get() = _connectedState

    private val _savedSnaps = MutableStateFlow<List<SnapResponseDto>>(emptyList())
    val savedSnaps: StateFlow<List<SnapResponseDto>> get() = _savedSnaps

    private val _savedSnapsLoading = MutableStateFlow(false)
    val savedSnapsLoading: StateFlow<Boolean> get() = _savedSnapsLoading

    fun selectSnap(snap: SnapResponseDto) {
        _snapState.value = _snapState.value.copy(selectSnap = snap)
    }

    fun createSnap(
        image: RequestBody,
        title: String,
        source: String,
        dateTaken: String,
        aiState: Boolean,
        tag: List<String>,
        starId: List<String>,
        starGroupId: List<String>
    ) = viewModelScope.launch {
        runCatching {
            snapRepository.createSnap(
                image = image,
                title = title,
                source = source,
                dateTaken = dateTaken,
                aiState = aiState,
                tag = tag,
                starId = starId,
                starGroupId = starGroupId
            )
        }.onSuccess {

        }.onFailure {

        }
    }

    fun toggleLike(
        snapId: String,
        onSuccess: (Boolean) -> Unit = {},
        onFailure: () -> Unit = {}
    ) = viewModelScope.launch {
        runCatching {
            snapRepository.toggleSnapLike(snapId)
        }.onSuccess {
            onSuccess(it.linked)
        }.onFailure {
            onFailure()
        }
    }

    // web 기준: 저장/저장취소 토글 (POST /api/snap/save, DELETE /api/snap/un-save)
    fun toggleSave(
        snapId: String,
        currentlySaved: Boolean,
        onSuccess: (Boolean) -> Unit = {},
        onFailure: () -> Unit = {}
    ) = viewModelScope.launch {
        runCatching {
            if (currentlySaved) snapRepository.unSaveSnap(snapId)
            else snapRepository.saveSnap(snapId)
        }.onSuccess {
            onSuccess(!currentlySaved)
        }.onFailure {
            onFailure()
        }
    }

    // web 기준: 스냅 삭제 (소프트 삭제, PATCH /api/snap/delete)
    fun deleteSnap(
        snapId: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) = viewModelScope.launch {
        runCatching {
            snapRepository.deleteSnap(snapId)
        }.onSuccess {
            onSuccess()
        }.onFailure {
            onFailure()
        }
    }

    // web 기준: 댓글 작성 (POST /api/snap/comment/create)
    fun createComment(
        snapId: String,
        content: String,
        onSuccess: (CommentDto) -> Unit = {},
        onFailure: () -> Unit = {}
    ) = viewModelScope.launch {
        runCatching {
            snapRepository.createComment(snapId, content)
        }.onSuccess {
            onSuccess(it)
        }.onFailure {
            onFailure()
        }
    }

    // web 기준: 저장한 snap 목록 (GET /api/snap/saved)
    fun loadSavedSnaps() = viewModelScope.launch {
        _savedSnapsLoading.value = true
        runCatching {
            snapRepository.getSavedSnaps()
        }.onSuccess {
            _savedSnaps.value = it
            _savedSnapsLoading.value = false
        }.onFailure {
            _savedSnaps.value = emptyList()
            _savedSnapsLoading.value = false
        }
    }

    // web 기준: 스냅의 태그를 스타/스타그룹 목록과 매칭해 연결된 스타/그룹을 찾는다.
    fun resolveConnected(tags: List<String>) = viewModelScope.launch {
        val normalizedTags = tags
            .map { it.removePrefix("#").trim() }
            .filter { it.isNotBlank() }
            .map { normalizeText(it) }
            .toSet()

        if (normalizedTags.isEmpty()) {
            _connectedState.value = ConnectedEntities()
            return@launch
        }

        runCatching {
            val stars = starRepository.getStarList(size = 500, page = 0, starName = "").content
            val groups = starRepository.getStarGroupList(size = 500, page = 0, starGroupName = "").content

            val matchedStars = stars.filter { star ->
                normalizeText(star.name) in normalizedTags ||
                    (!star.nickname.isNullOrBlank() && normalizeText(star.nickname ?: "") in normalizedTags)
            }.distinctBy { it.id }

            val matchedGroups = groups.filter { group ->
                normalizeText(group.name) in normalizedTags
            }.distinctBy { it.id }

            ConnectedEntities(stars = matchedStars, starGroups = matchedGroups)
        }.onSuccess {
            _connectedState.value = it
        }.onFailure {
            _connectedState.value = ConnectedEntities()
        }
    }

    private fun normalizeText(value: String): String =
        value.replace("\\s".toRegex(), "").lowercase()
}

data class ConnectedEntities(
    val stars: List<StarResponseDto> = emptyList(),
    val starGroups: List<StarGroupResponseDto> = emptyList()
)

data class SnapState(
    val refreshSnapLoading: Boolean = false, // Snap 새로고침
    val snapListLoading: Boolean = false, // Snap 리스트 로딩
    val selectSnap: SnapResponseDto? = null
)