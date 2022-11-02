package com.bignerdranch.android.photogallery.api

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val API_KEY = "adec067543873d9a6e7c2c8d6959f8ae"

class PhotoInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()

        val newUrl: HttpUrl =
            originalRequest.url.newBuilder()
                .addQueryParameter("api_key", API_KEY)
                .addQueryParameter("format", "json")
                .addQueryParameter("nojsoncallback", "1")
                .addQueryParameter("extras", "url_s")
                .addQueryParameter("safesearch", "1")
                .build()

        val newRequest: Request =
            originalRequest.newBuilder()
                .url(newUrl)
                .build()

        return chain.proceed(newRequest)
    }
}