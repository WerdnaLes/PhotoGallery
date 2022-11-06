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

    data class PhotoGalleryUiState(
        val images: PagingData<GalleryItem> = PagingData.empty(),
        val query: String = ""
    )

    private val _uiState: MutableStateFlow<PhotoGalleryUiState> =
        MutableStateFlow(PhotoGalleryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.storedQuery.collectLatest { storedQuery ->
                Log.d("ViewModel", "Start querying...")
                fetchGalleryItems(storedQuery).collectLatest { pagingData ->
                    _uiState.update { oldState ->
                        oldState.copy(
                            images = pagingData,
                            query = storedQuery
                        )
                    }
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
