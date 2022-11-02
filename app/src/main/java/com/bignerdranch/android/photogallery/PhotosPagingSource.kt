package com.bignerdranch.android.photogallery

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.api.FlickrResponse
import com.bignerdranch.android.photogallery.api.GalleryItem
import com.bignerdranch.android.photogallery.api.NETWORK_PAGE_SIZE
import okio.IOException
import retrofit2.HttpException

class PhotosPagingSource(
    private val serviceApi: FlickrApi,
    private val query: String
) : PagingSource<Int, GalleryItem>() {
    override fun getRefreshKey(state: PagingState<Int, GalleryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryItem> {
        val pageIndex = params.key ?: 1
        return try {
            val response: FlickrResponse
            val data: List<GalleryItem>
            val pages: Int
            val nextKey: Int?
            val pageSize = params.loadSize.coerceAtMost(NETWORK_PAGE_SIZE)
            if (query.isNotEmpty()) {
                response = serviceApi.searchPhotos(query, pageIndex, pageSize)
                data = response.photos.galleryItems
                pages = response.photos.pages
                nextKey = nextKey(pageIndex, pages)
            } else {
                response = serviceApi.fetchPhotos(pageIndex, pageSize)
                data = response.photos.galleryItems
                pages = response.photos.pages
                nextKey = nextKey(pageIndex, pages)

            }
            LoadResult.Page(
                data = if (nextKey != null) data else emptyList(),
                prevKey = prevKey(pageIndex),
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    private fun nextKey(pageIndex: Int, pages: Int) =
        if (pageIndex > pages) null else pageIndex + 1

    private fun prevKey(pageIndex: Int) =
        if (pageIndex == 1) null else pageIndex - 1
}