package com.bignerdranch.android.photogallery

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bignerdranch.android.photogallery.api.GalleryItem
import kotlinx.coroutines.flow.*

const val CURRENT_QUERY_KEY = "CURRENT_QUERY_KEY"

class PhotoGalleryViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val photoRepository = PhotoRepository()

    var queryText: String
        get() = savedStateHandle[CURRENT_QUERY_KEY] ?: ""
        set(value) = savedStateHandle.set(CURRENT_QUERY_KEY, value)

    var galleryItems: StateFlow<PagingData<GalleryItem>> = queryCall()

    fun fetchPhotos(query: String = queryText) {
        Log.d("ViewModel", "Querying fetch")
        galleryItems = queryCall(query)
    }

    private fun queryCall(query: String = queryText): StateFlow<PagingData<GalleryItem>> {
        Log.d("ViewModel", "Querying call")
        return photoRepository.fetchPhotos(query)
            .cachedIn(viewModelScope)
            .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())
    }
}