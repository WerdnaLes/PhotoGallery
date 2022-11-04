package com.bignerdranch.android.photogallery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bignerdranch.android.photogallery.api.GalleryItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PhotoGalleryViewModel : ViewModel() {
    private val photoRepository = PhotoRepository()
    private val preferencesRepository =
        PreferencesRepository.get()

    private val _galleryItems: MutableStateFlow<PagingData<GalleryItem>> =
        MutableStateFlow(PagingData.empty())
    val galleryItems = _galleryItems.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.storedQuery.collectLatest { storedQuery ->
                Log.d("ViewModel", "Start querying...")
                fetchGalleryItems(storedQuery).collectLatest { pagingData ->
                    _galleryItems.value = pagingData
                }
            }
        }
    }

    private fun fetchGalleryItems(query: String) =
        photoRepository.fetchPhotos(query)
            .cachedIn(viewModelScope)

    fun setQuery(query: String) {
        viewModelScope.launch {
            preferencesRepository.setStoredQuery(query)
        }
    }
}
