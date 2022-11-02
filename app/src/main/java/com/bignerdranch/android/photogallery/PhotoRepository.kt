package com.bignerdranch.android.photogallery

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.api.NETWORK_PAGE_SIZE
import com.bignerdranch.android.photogallery.api.PhotoInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class PhotoRepository {
    private val flickrApi: FlickrApi

    init {
        // Adding an interceptor
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(PhotoInterceptor())
            .build()
        // Using the Retrofit object to create an instance of the API
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttpClient)
            .build()

        flickrApi =
            retrofit.create()
    }

    fun fetchPhotos(query: String) =
        Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                PhotosPagingSource(flickrApi, query)
            }
        ).flow
}