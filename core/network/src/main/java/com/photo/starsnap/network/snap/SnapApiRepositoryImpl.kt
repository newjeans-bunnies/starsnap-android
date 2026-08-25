package com.photo.starsnap.network.snap

import com.photo.starsnap.network.dto.SliceResponseDto
import com.photo.starsnap.network.dto.StatusDto
import com.photo.starsnap.network.snap.dto.CommentDto
import com.photo.starsnap.network.snap.dto.CreateCommentRequestDto
import com.photo.starsnap.network.snap.dto.CreateSnapRequestDto
import com.photo.starsnap.network.snap.dto.SnapDto
import com.photo.starsnap.network.snap.dto.SnapLikeToggleDto
import com.photo.starsnap.network.snap.dto.SnapResponseDto
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class SnapApiRepositoryImpl @Inject constructor(
    private val snapApi: SnapApi
) : SnapRepository {
    override suspend fun createSnap(request: CreateSnapRequestDto) {
        return snapApi.createSnap(request)
    }

    override suspend fun sendSnap(size: Int, page: Int) {
        return snapApi.sendSnap(size, page)
    }

    override suspend fun fixSnap(
        snapId: String,
        image: RequestBody?,
        title: String,
        source: String,
        dateTaken: String,
        aiState: Boolean,
        tag: List<String>,
        starId: List<String>,
        starGroupId: List<String>,
    ): SnapDto {
        return snapApi.fixSnap(
            snapId.toRequestBody("text/plain".toMediaType()),
            image,
            title.toRequestBody("text/plain".toMediaType()),
            source.toRequestBody("text/plain".toMediaType()),
            dateTaken.toRequestBody("text/plain".toMediaType()),
            aiState.toString().toRequestBody("text/plain".toMediaType()),
            tag.map { it.toRequestBody("text/plain".toMediaType()) },
            starId.map { it.toRequestBody("text/plain".toMediaType()) },
            starGroupId.map { it.toRequestBody("text/plain".toMediaType()) }
        )
    }

    override suspend fun deleteSnap(snapId: String): StatusDto {
        return snapApi.deleteSnap(snapId)
    }

    override suspend fun getSnap(
        size: Int,
        page: Int,
        tag: List<String>,
        title: String,
        userId: String,
        starId: List<String>,
        starGroupId: List<String>
    ): SliceResponseDto<SnapResponseDto> {
        return snapApi.getSnap(size, page, tag, title, userId, starId, starGroupId)
    }

    override suspend fun getFeedSnap(
        page: Int,
        size: Int
    ): SliceResponseDto<SnapResponseDto> {
        return snapApi.getFeedSnap(page, size)
    }

    override suspend fun getRelatedSnaps(
        snapId: String,
        page: Int,
        size: Int
    ): SliceResponseDto<SnapResponseDto> {
        return snapApi.getRelatedSnaps(snapId, page, size)
    }

    override suspend fun toggleSnapLike(snapId: String): SnapLikeToggleDto {
        return snapApi.toggleSnapLike(snapId)
    }

    override suspend fun saveSnap(snapId: String): StatusDto {
        return snapApi.saveSnap(snapId)
    }

    override suspend fun unSaveSnap(snapId: String): StatusDto {
        return snapApi.unSaveSnap(snapId)
    }

    override suspend fun getSavedSnaps(): List<SnapResponseDto> {
        return snapApi.getSavedSnaps()
    }

    override suspend fun getSnapsByStarGroup(starGroupId: String, page: Int, size: Int): SliceResponseDto<SnapResponseDto> {
        return snapApi.getSnapsByStarGroup(starGroupId, page, size)
    }

    override suspend fun createComment(snapId: String, content: String): CommentDto {
        return snapApi.createComment(CreateCommentRequestDto(content = content, snapId = snapId))
    }
}
