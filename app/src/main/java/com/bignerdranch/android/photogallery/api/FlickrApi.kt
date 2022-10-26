package com.bignerdranch.android.photogallery.api

import retrofit2.http.GET

private const val API_KEY = "adec067543873d9a6e7c2c8d6959f8ae"

// Defining the "fetch recent interesting photos" request:
interface FlickrApi {
    @GET(
        "services/rest/?" +
                "method=flickr.interestingness.getList" +
                "&api_key=$API_KEY" +
                "&format=json" +
                "&nojsoncallback=1" +
                "&extras=url_s"
    )
    suspend fun fetchPhotos(): FlickrResponse
}