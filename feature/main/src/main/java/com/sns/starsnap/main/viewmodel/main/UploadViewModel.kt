package com.sns.starsnap.main.viewmodel.main

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.sns.starsnap.main.utils.constant.Constant.GALLERY_PHOTO_SIZE
import com.sns.starsnap.main.utils.constant.Constant.STAR_GROUP_SIZE
import com.sns.starsnap.main.utils.constant.Constant.STAR_SIZE
import com.sns.starsnap.main.utils.paging.CustomGalleryPagingSource
import com.sns.starsnap.main.utils.paging.StarGroupPagingSource
import com.sns.starsnap.main.utils.paging.StarPagingSource
import com.sns.starsnap.model.photo.PhotoRepository
import com.sns.starsnap.network.file.FileRepository
import com.sns.starsnap.network.file.dto.rq.UploadFileRequestDto
import com.sns.starsnap.network.snap.SnapRepository
import com.sns.starsnap.network.snap.dto.CreateSnapRequestDto
import com.sns.starsnap.network.star.StarRepository
import com.sns.starsnap.network.star.dto.StarGroupResponseDto
import com.sns.starsnap.network.star.dto.StarResponseDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val snapRepository: SnapRepository,
    private val fileRepository: FileRepository,
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

    private val _uploadState = MutableStateFlow(UploadSnapState())
    val uploadState: StateFlow<UploadSnapState> get() = _uploadState

    private val uploadGuard = SingleUploadGuard()
    private var uploadJob: Job? = null

    // 사진 선택
    fun selectedImage(id: Long, imageUri: Uri) {
        resetCompletedUploadForNewDraft()
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
        resetCompletedUploadForNewDraft()
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
    ) {
        val selectedImages = _selectedImages.value
        if (selectedImages.isEmpty()) {
            _uploadState.value = UploadSnapState(errorMessage = "사진을 한 장 이상 선택해 주세요.")
            return
        }
        if (_uploadState.value.isComplete || uploadJob?.isActive == true || !uploadGuard.tryStart()) {
            return
        }

        val starIds = _selectedStars.value.map { it.id }
        val starGroupIds = _selectedStarGroups.value.map { it.id }
        _uploadState.value = UploadSnapState(isUploading = true)

        uploadJob = viewModelScope.launch {
            try {
                val contentResolver = context.applicationContext.contentResolver
                val preparedPhotos = withContext(Dispatchers.IO) {
                    selectedImages.map { selectedImage ->
                        contentResolver.createSnapPhotoRequestBody(selectedImage.imageUri)
                    }
                }

                val photoKeys = preparedPhotos.map { (photo, requestBody) ->
                    val presignResponse = fileRepository.createPhotoPresidentUrl(
                        UploadFileRequestDto(
                            aiState = aiState,
                            dateTaken = dateTaken,
                            source = source,
                            contentType = photo.contentType,
                            fileSize = photo.sizeBytes,
                        )
                    )
                    if (!presignResponse.isSuccessful) {
                        throw IOException("사진 업로드 URL 생성 실패 (${presignResponse.code()})")
                    }
                    val upload = presignResponse.body()
                        ?: throw IOException("사진 업로드 URL 응답이 비어 있습니다.")

                    fileRepository.uploadFile(
                        presignedUrl = upload.presignedUrl,
                        contentType = photo.contentType,
                        requiredHeaders = upload.requiredHeaders,
                        file = requestBody
                    )
                    extractPhotoFileKey(upload.presignedUrl)
                }

                snapRepository.createSnap(
                    CreateSnapRequestDto(
                        title = title.trim(),
                        description = "",
                        source = source,
                        tags = tag,
                        photos = photoKeys,
                        starIds = starIds,
                        starGroupIds = starGroupIds,
                        commentState = commentsEnabled
                    )
                )

                _uploadState.value = UploadSnapState(isComplete = true)
                Log.d(TAG, "Snap created successfully")
            } catch (error: CancellationException) {
                _uploadState.value = UploadSnapState()
                throw error
            } catch (error: Exception) {
                Log.e(TAG, "Snap upload failed: ${error::class.java.simpleName}")
                _uploadState.value = UploadSnapState(
                    errorMessage = if (error is UploadInputException) {
                        error.message
                    } else {
                        "스냅을 게시하지 못했어요. 잠시 후 다시 시도해 주세요."
                    }
                )
            } finally {
                uploadGuard.finish()
            }
        }
    }

    private fun resetCompletedUploadForNewDraft() {
        if (_uploadState.value.isComplete) {
            _uploadState.value = UploadSnapState()
        }
    }
}

data class UploadSnapState(
    val isUploading: Boolean = false,
    val isComplete: Boolean = false,
    val errorMessage: String? = null
)

internal fun extractPhotoFileKey(presignedUrl: String): String {
    val pathSegments = presignedUrl.toHttpUrl().pathSegments
    val photoIndex = pathSegments.indexOf("photo")
    require(photoIndex >= 0 && photoIndex < pathSegments.lastIndex) {
        "Presigned URL does not contain a photo object key"
    }
    return pathSegments.drop(photoIndex).joinToString("/")
}

data class CroppingImage(
    val id: Long,
    val imageUri: Uri,
)
