package com.photo.starsnap.main.viewmodel.main

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.photo.starsnap.main.utils.constant.Constant.GALLERY_PHOTO_SIZE
import com.photo.starsnap.main.utils.constant.Constant.STAR_GROUP_SIZE
import com.photo.starsnap.main.utils.constant.Constant.STAR_SIZE
import com.photo.starsnap.main.utils.paging.CustomGalleryPagingSource
import com.photo.starsnap.main.utils.paging.StarGroupPagingSource
import com.photo.starsnap.main.utils.paging.StarPagingSource
import com.photo.starsnap.model.photo.PhotoRepository
import com.photo.starsnap.network.snap.SnapRepository
import com.photo.starsnap.network.star.StarRepository
import com.photo.starsnap.network.star.dto.StarGroupResponseDto
import com.photo.starsnap.network.star.dto.StarResponseDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val snapRepository: SnapRepository,
    private val starRepository: StarRepository,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    companion object {
        const val TAG = "UploadViewModel"
    }

    val photoList = Pager(
        config = PagingConfig(
            pageSize = GALLERY_PHOTO_SIZE,
            initialLoadSize = GALLERY_PHOTO_SIZE,
            enablePlaceholders = false
        ), pagingSourceFactory = {
            CustomGalleryPagingSource(
                photoRepository = photoRepository, currentLocation = "" // 모든 위치의 사진 가져오기
            )
        }).flow.cachedIn(viewModelScope)


    fun starGroupList(starGroupName: String) = Pager(
        config = PagingConfig(
            pageSize = STAR_GROUP_SIZE,
            initialLoadSize = STAR_GROUP_SIZE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            StarGroupPagingSource(
                starRepository = starRepository,
                starGroupName = starGroupName
            )
        }
    ).flow.cachedIn(viewModelScope)


    fun starList(starName: String) = Pager(
        config = PagingConfig(
            pageSize = STAR_GROUP_SIZE,
            initialLoadSize = STAR_GROUP_SIZE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            StarPagingSource(
                starRepository = starRepository,
                starName = starName
            )
        }
    ).flow.cachedIn(viewModelScope)

    private val _selectedImages = MutableStateFlow<List<CroppingImage>>(emptyList())
    val selectedPhotos: StateFlow<List<CroppingImage>>
        get() = _selectedImages


    private val _selectedStars = MutableStateFlow<List<StarResponseDto>>(emptyList())
    val selectedStars: StateFlow<List<StarResponseDto>>
        get() = _selectedStars


    private val _selectedStarGroups = MutableStateFlow<List<StarGroupResponseDto>>(emptyList())
    val selectedStarGroups: StateFlow<List<StarGroupResponseDto>>
        get() = _selectedStarGroups

    // 사진 선택
    fun selectedImage(id: Long, imageUri: Uri) {
        val current = _selectedImages.value
        val exists = current.any { it.id == id }
        _selectedImages.value = if (exists) {
            current.filterNot { it.id == id }
        } else {
            current + CroppingImage(id = id, imageUri = imageUri)
        }
        Log.d(
            "UploadViewModel", "image id: $id, ${if (exists) "remove" else "select"}"
        )
    }

    fun removeSelectImage() {
        Log.d(TAG, "선택된 사진 해제됨")
        _selectedImages.value = listOf()
    }

    // star 선택
    fun selectedStar(star: StarResponseDto) {
        val current = _selectedStars.value
        val exists = current.any { it.id == star.id }
        _selectedStars.value = if (exists) {
            current.filterNot { it.id == star.id }
        } else {
            current + star
        }
        Log.d(
            "UploadViewModel", "selected star: ${star.name}"
        )
    }

    fun removeSelectedStar() {
        _selectedStars.value = listOf()
    }

    // star group 선택
    fun selectedStarGroup(starGroup: StarGroupResponseDto) {
        val current = _selectedStarGroups.value
        val exists = current.any { it.id == starGroup.id }
        _selectedStarGroups.value = if (exists) {
            current.filterNot { it.id == starGroup.id }
        } else {
            current + starGroup
        }
        Log.d(
            "UploadViewModel", "selected star-group: ${starGroup.name}"
        )
    }

    fun removeSelectedStarGroup() {
        _selectedStarGroups.value = listOf()
    }

    fun uploadSnap(
        context: Context,
        title: String,
        tag: List<String>,
        source: String,
        dateTaken: String,
        aiState: Boolean,
        commentsEnabled: Boolean
    ) = viewModelScope.launch {
        try {
            if (_selectedImages.value.isEmpty()) {
                Log.e(TAG, "No images selected")
                return@launch
            }

            // 첫 번째 선택 이미지 사용
            val firstImage = _selectedImages.value.first()
            val requestBody = getRequestBodyFromUri(context, firstImage.imageUri)

            if (requestBody == null) {
                Log.e(TAG, "Failed to convert URI to RequestBody")
                return@launch
            }

            // Star ID와 StarGroup ID 추출
            val starIds = _selectedStars.value.map { it.id.toString() }
            val starGroupIds = _selectedStarGroups.value.map { it.id.toString() }

            // 서버 API 호출
            snapRepository.createSnap(
                image = requestBody,
                title = title,
                source = source,
                dateTaken = dateTaken,
                aiState = aiState,
                tag = tag,
                starId = starIds,
                starGroupId = starGroupIds
            )

            Log.d(TAG, "Snap created successfully")
            // 여기에 성공 콜백이나 네비게이션 로직 추가 가능
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading snap: ${e.message}", e)
        }
    }

    // Uri로부터 RequestBody를 생성하는 헬퍼 함수 (Scoped Storage 호환)
    private fun getRequestBodyFromUri(context: Context, uri: Uri): okhttp3.RequestBody? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val bytes = inputStream.readBytes()
                inputStream.close()
                bytes.toRequestBody("image/jpeg".toMediaType())
            } else {
                Log.e(TAG, "Could not open input stream for URI: $uri")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to RequestBody: ${e.message}", e)
            null
        }
    }
}

data class CroppingImage(
    val id: Long,
    val imageUri: Uri,
)
