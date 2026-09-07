package com.sns.starsnap.main.utils.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.sns.starsnap.main.utils.constant.Constant.GALLERY_PHOTO_SIZE
import com.sns.starsnap.model.photo.dao.GalleryImage
import com.sns.starsnap.model.photo.PhotoRepository

class CustomGalleryPagingSource(
    private val photoRepository: PhotoRepository,
    private val currentLocation: String?
) : PagingSource<Int, GalleryImage>() {
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, GalleryImage> {
        return try {
            val position = params.key ?: STARTING_PAGE_INDEX
            val data = photoRepository.getAllPhotos(
                page = position,
                loadSize = params.loadSize,
                currentLocation = currentLocation,
            )
            Log.d(TAG, "size: ${params.loadSize}, page: $position")
            val endOfPaginationReached = data.isEmpty()
            val prevKey = if (position == STARTING_PAGE_INDEX) null else position - 1
            val nextKey = if (endOfPaginationReached) null else position + 1
            LoadResult.Page(data, prevKey, nextKey)
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, GalleryImage>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    companion object {
        const val TAG = "CustomGalleryPagingSource"
        const val STARTING_PAGE_INDEX = 0
    }
}