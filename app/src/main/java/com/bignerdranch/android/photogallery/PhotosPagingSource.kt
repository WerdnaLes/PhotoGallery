package com.bignerdranch.android.photogallery

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.api.GalleryItem
import okio.IOException
import retrofit2.HttpException

class PhotosPagingSource(
    private val serviceApi: FlickrApi,
) : PagingSource<Int, GalleryItem>() {
    override fun getRefreshKey(state: PagingState<Int, GalleryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryItem> {
        val pageIndex = params.key ?: 1
        val pages: Int
        return try {
            val response = serviceApi.fetchPhotos(pageIndex)
                .photos.let { response ->
                    pages = response.pages
                    response.galleryItems
                }
            val nextKey =
                if (pageIndex > pages) null else pageIndex + 1
            val prevKey =
                if (pageIndex == 1) null else pageIndex - 1

            LoadResult.Page(
                data = if (nextKey != null) response else emptyList(),
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }
}