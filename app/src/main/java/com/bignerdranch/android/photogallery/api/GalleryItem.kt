package com.bignerdranch.android.photogallery.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Fetch the detailed information about specific photo:
@JsonClass(generateAdapter = true)
data class GalleryItem(
    val title: String,
    val id: String,
    @Json(name = "url_s") val url: String
)