package com.sns.starsnap.main.utils.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.sns.starsnap.network.star.StarRepository
import com.sns.starsnap.network.star.dto.StarResponseDto

class StarPagingSource(
    private val starRepository: StarRepository,
    private val starName: String
) : PagingSource<Int, StarResponseDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StarResponseDto> {
        return try {
            val page = params.key ?: STARTING_PAGE_INDEX

            val data = starRepository.getStarList(
                size = PAGING_SIZE,
                page = page,
                starName = starName
            )
            Log.d(TAG, "size: ${PAGING_SIZE}, page: $page")

            val snapList = data.content
            val endOfPaginationReached = data.last || snapList.isEmpty()

            val prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1
            val nextKey = if (endOfPaginationReached) null else page + 1
            Log.d(TAG, "prevKey: $prevKey, nextKey: $nextKey")

            LoadResult.Page(
                data = snapList,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (exception: Exception) {
            Log.d(TAG, "error: ${exception.message}")
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, StarResponseDto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    companion object {
        const val TAG = "StarPagingSource"
        const val PAGING_SIZE = 10
        const val STARTING_PAGE_INDEX = 0
    }
}