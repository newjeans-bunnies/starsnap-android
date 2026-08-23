package com.photo.starsnap.main.viewmodel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.photo.starsnap.main.utils.constant.Constant.STAR_GROUP_SIZE
import com.photo.starsnap.main.utils.paging.StarGroupPagingSource
import com.photo.starsnap.main.utils.paging.StarPagingSource
import com.photo.starsnap.network.snap.SnapRepository
import com.photo.starsnap.network.snap.dto.SnapResponseDto
import com.photo.starsnap.network.star.StarRepository
import com.photo.starsnap.network.star.dto.StarGroupResponseDto
import com.photo.starsnap.network.star.dto.StarResponseDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StarViewModel @Inject constructor(
    private val starRepository: StarRepository,
    private val snapRepository: SnapRepository,
) : ViewModel() {

    private val _searchStarName = MutableStateFlow("")
    private val _searchStarGroupName = MutableStateFlow("")

    private val _starGroupDetail = MutableStateFlow(StarGroupDetail())
    val starGroupDetail: StateFlow<StarGroupDetail>
        get() = _starGroupDetail

    private val _starDetail = MutableStateFlow(StarDetail())
    val starDetail: StateFlow<StarDetail>
        get() = _starDetail

    private val _selectedStarGroup = MutableStateFlow<StarGroupResponseDto?>(null)
    val selectedStarGroup: StateFlow<StarGroupResponseDto?>
        get() = _selectedStarGroup

    private val _selectedStar = MutableStateFlow<StarResponseDto?>(null)
    val selectedStar: StateFlow<StarResponseDto?>
        get() = _selectedStar

    fun selectStarGroup(starGroup: StarGroupResponseDto) {
        _selectedStarGroup.value = starGroup
    }

    fun selectStar(star: StarResponseDto) {
        _selectedStar.value = star
    }

    // web StarGroupDetail 기준: 그룹 멤버(스타 목록 필터) + 연결된 스냅 로드
    fun loadStarGroupDetail(groupId: String) = viewModelScope.launch {
        _starGroupDetail.value = StarGroupDetail(loading = true)
        runCatching {
            val members = starRepository.getStarList(500, 0, "").content
                .filter { it.starGroup?.id == groupId }
            val snaps = snapRepository.getSnapsByStarGroup(groupId, 0, 100).content
            StarGroupDetail(members = members, snaps = snaps)
        }.onSuccess {
            _starGroupDetail.value = it
        }.onFailure {
            _starGroupDetail.value = StarGroupDetail()
        }
    }

    // web StarDetail 기준: 연결된 스냅 로드(태그 매칭)
    fun loadStarDetail(star: StarResponseDto) = viewModelScope.launch {
        _starDetail.value = StarDetail(loading = true)
        runCatching {
            val keywords = listOf(star.name, star.nickname ?: "")
                .map { normalizeText(it) }
                .filter { it.isNotBlank() }
                .toSet()

            if (keywords.isEmpty()) {
                StarDetail()
            } else {
                val snaps = snapRepository.getFeedSnap(page = 0, size = 100).content
                    .filter { snap ->
                        snap.snapData.tags.any { tag ->
                            normalizeText(tag.removePrefix("#")) in keywords
                        }
                    }
                StarDetail(snaps = snaps)
            }
        }.onSuccess {
            _starDetail.value = it
        }.onFailure {
            _starDetail.value = StarDetail()
        }
    }

    fun setSearchStarName(name: String) {
        _searchStarName.value = name
    }

    fun setSearchStarGroupName(name: String) {
        _searchStarGroupName.value = name
    }

    val starGroupList = Pager(
        config = PagingConfig(
            pageSize = STAR_GROUP_SIZE,
            initialLoadSize = STAR_GROUP_SIZE,
            enablePlaceholders = false
        ), pagingSourceFactory = {
            StarGroupPagingSource(
                starRepository = starRepository, starGroupName = _searchStarGroupName.value
            )
        }).flow.cachedIn(viewModelScope)

    val starList = Pager(
        config = PagingConfig(
            pageSize = STAR_GROUP_SIZE,
            initialLoadSize = STAR_GROUP_SIZE,
            enablePlaceholders = false
        ), pagingSourceFactory = {
            StarPagingSource(
                starRepository = starRepository, starName = _searchStarName.value
            )
        }).flow.cachedIn(viewModelScope)

    private fun normalizeText(value: String): String =
        value.replace("\\s".toRegex(), "").lowercase()
}

data class StarGroupDetail(
    val members: List<StarResponseDto> = emptyList(),
    val snaps: List<SnapResponseDto> = emptyList(),
    val loading: Boolean = false
)

data class StarDetail(
    val snaps: List<SnapResponseDto> = emptyList(),
    val loading: Boolean = false
)
