package com.bignerdranch.android.photogallery.api

import retrofit2.http.GET
import retrofit2.http.Query

const val NETWORK_PAGE_SIZE = 80

// Defining the "fetch recent interesting photos" request:
interface FlickrApi {
    @GET("services/rest/?method=flickr.interestingness.getList")
    suspend fun fetchPhotos(
        @Query("page") page: Int,
        @Query("perPage") pageSize: Int
    ): FlickrResponse

    // Search for Photos by its name
    @GET("services/rest?method=flickr.photos.search")
    suspend fun searchPhotos(
        @Query("text") query: String,
        @Query("page") page: Int,
        @Query("perPage") pageSize: Int
    ): FlickrResponse
}