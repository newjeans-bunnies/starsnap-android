package com.sns.starsnap.main.viewmodel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.sns.starsnap.main.utils.constant.Constant.SNAP_SIZE
import com.sns.starsnap.main.utils.paging.SnapPagingSource
import com.sns.starsnap.network.snap.SnapRepository
import com.sns.starsnap.network.snap.dto.CommentDto
import com.sns.starsnap.network.snap.dto.SnapResponseDto
import com.sns.starsnap.network.star.StarRepository
import com.sns.starsnap.network.star.dto.StarGroupResponseDto
import com.sns.starsnap.network.star.dto.StarResponseDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@HiltViewModel
class SnapViewModel @Inject constructor(
    private val snapRepository: SnapRepository,
    private val starRepository: StarRepository
) : ViewModel() {

    companion object {
        const val TAG = "SnapViewModel"
        private const val RELATED_SNAP_SIZE = 12
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

    private val _savedSnapsLoading = MutableStateFlow(true)
    val savedSnapsLoading: StateFlow<Boolean> get() = _savedSnapsLoading

    private val _savedSnapsError = MutableStateFlow<String?>(null)
    val savedSnapsError: StateFlow<String?> get() = _savedSnapsError

    private val _relatedSnapsState = MutableStateFlow(RelatedSnapsState())
    val relatedSnapsState: StateFlow<RelatedSnapsState> get() = _relatedSnapsState

    private var relatedSnapsJob: Job? = null
    private var connectedEntitiesJob: Job? = null
    private var savedSnapsJob: Job? = null
    private val connectedEntitiesGeneration = AtomicLong(0)
    private val savedSnapsGeneration = AtomicLong(0)

    fun selectSnap(snap: SnapResponseDto) {
        connectedEntitiesJob?.cancel()
        connectedEntitiesGeneration.incrementAndGet()
        _snapState.value = _snapState.value.copy(selectSnap = snap)
        _connectedState.value = ConnectedEntities(loading = snap.snapData.tags.isNotEmpty())
        _relatedSnapsState.value = RelatedSnapsState(
            snapId = snap.snapData.snapId,
            loading = snap.snapData.snapId.isNotBlank(),
        )
    }

    fun loadRelatedSnaps(snapId: String) {
        relatedSnapsJob?.cancel()

        if (snapId.isBlank()) {
            _relatedSnapsState.value = RelatedSnapsState()
            return
        }

        _relatedSnapsState.value = RelatedSnapsState(snapId = snapId, loading = true)
        relatedSnapsJob = viewModelScope.launch {
            try {
                val related = snapRepository.getRelatedSnaps(
                    snapId = snapId,
                    page = 0,
                    size = RELATED_SNAP_SIZE
                ).content
                    .filterNot { it.snapData.snapId == snapId }
                    .distinctBy { it.snapData.snapId }

                if (_relatedSnapsState.value.snapId == snapId) {
                    _relatedSnapsState.value = RelatedSnapsState(
                        snapId = snapId,
                        snaps = related
                    )
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                if (_relatedSnapsState.value.snapId == snapId) {
                    _relatedSnapsState.value = RelatedSnapsState(
                        snapId = snapId,
                        errorMessage = "비슷한 얼굴의 스냅을 불러오지 못했어요."
                    )
                }
            }
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
    fun loadSavedSnaps() {
        savedSnapsJob?.cancel()
        val generation = savedSnapsGeneration.incrementAndGet()
        _savedSnapsLoading.value = true
        _savedSnapsError.value = null
        savedSnapsJob = viewModelScope.launch {
            try {
                val savedSnaps = snapRepository.getSavedSnaps()
                if (generation == savedSnapsGeneration.get()) {
                    _savedSnaps.value = savedSnaps
                    _savedSnapsError.value = null
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                if (generation == savedSnapsGeneration.get()) {
                    _savedSnapsError.value = "저장한 스냅을 불러오지 못했어요."
                }
            } finally {
                if (generation == savedSnapsGeneration.get()) {
                    _savedSnapsLoading.value = false
                    savedSnapsJob = null
                }
            }
        }
    }

    // web 기준: 스냅의 태그를 스타/스타그룹 목록과 매칭해 연결된 스타/그룹을 찾는다.
    fun resolveConnected(tags: List<String>) {
        connectedEntitiesJob?.cancel()
        val generation = connectedEntitiesGeneration.incrementAndGet()
        val selectedSnapId = _snapState.value.selectSnap?.snapData?.snapId
        val normalizedTags = tags
            .map { it.removePrefix("#").trim() }
            .filter { it.isNotBlank() }
            .map { normalizeText(it) }
            .toSet()

        if (normalizedTags.isEmpty()) {
            if (isCurrentConnectedRequest(generation, selectedSnapId)) {
                _connectedState.value = ConnectedEntities()
            }
            return
        }

        _connectedState.value = _connectedState.value.copy(
            loading = true,
            errorMessage = null,
        )

        connectedEntitiesJob = viewModelScope.launch {
            try {
                val stars = starRepository.getStarList(size = 500, page = 0, starName = "").content
                val groups = starRepository.getStarGroupList(size = 500, page = 0, starGroupName = "").content

                val matchedStars = stars.filter { star ->
                    normalizeText(star.name) in normalizedTags ||
                        (!star.nickname.isNullOrBlank() && normalizeText(star.nickname ?: "") in normalizedTags)
                }.distinctBy { it.id }

                val matchedGroups = groups.filter { group ->
                    normalizeText(group.name) in normalizedTags
                }.distinctBy { it.id }

                if (isCurrentConnectedRequest(generation, selectedSnapId)) {
                    _connectedState.value = ConnectedEntities(
                        stars = matchedStars,
                        starGroups = matchedGroups,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                if (isCurrentConnectedRequest(generation, selectedSnapId)) {
                    _connectedState.value = _connectedState.value.copy(
                        loading = false,
                        errorMessage = "연결된 스타와 스타그룹을 불러오지 못했어요.",
                    )
                }
            } finally {
                if (generation == connectedEntitiesGeneration.get()) {
                    connectedEntitiesJob = null
                }
            }
        }
    }

    private fun isCurrentConnectedRequest(generation: Long, snapId: String?): Boolean =
        generation == connectedEntitiesGeneration.get() &&
            _snapState.value.selectSnap?.snapData?.snapId == snapId

    private fun normalizeText(value: String): String =
        value.replace("\\s".toRegex(), "").lowercase()
}

data class ConnectedEntities(
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val stars: List<StarResponseDto> = emptyList(),
    val starGroups: List<StarGroupResponseDto> = emptyList()
)

data class SnapState(
    val refreshSnapLoading: Boolean = false, // Snap 새로고침
    val snapListLoading: Boolean = false, // Snap 리스트 로딩
    val selectSnap: SnapResponseDto? = null
)

data class RelatedSnapsState(
    val snapId: String? = null,
    val snaps: List<SnapResponseDto> = emptyList(),
    val loading: Boolean = false,
    val errorMessage: String? = null
)
