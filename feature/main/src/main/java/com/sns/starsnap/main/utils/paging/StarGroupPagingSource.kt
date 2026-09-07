package com.sns.starsnap.main.utils.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.sns.starsnap.network.star.StarRepository
import com.sns.starsnap.network.star.dto.StarGroupResponseDto

class StarGroupPagingSource(
    private val starRepository: StarRepository,
    private val starGroupName: String
) : PagingSource<Int, StarGroupResponseDto>() {

    override fun getRefreshKey(state: PagingState<Int, StarGroupResponseDto>): Int? {
        return state.anchorPosition?.let { achorPosition ->
            state.closestPageToPosition(achorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(achorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StarGroupResponseDto> {
        return try {
            val page = params.key ?: 0

            val data = starRepository.getStarGroupList(
                size = PAGING_SIZE,
                page = page,
                starGroupName = starGroupName
            )

            val snapList = data.content
            val endOfPaginationReached = data.last

            val prevKey = if (page == 0) null else page - 1
            val nextKey = if (endOfPaginationReached) null else page + 1
            Log.d(TAG, "prevKey: $prevKey, nextKey: $nextKey")

            LoadResult.Page(
                data = snapList,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (exception: Exception) {
            Log.d(TAG, "load: $exception")
            LoadResult.Error(exception)
        }
    }

    companion object {
        const val TAG = "StarGroupPagingSource"
        const val PAGING_SIZE = 10
    }
}