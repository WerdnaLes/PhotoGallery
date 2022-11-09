package com.bignerdranch.android.photogallery

import android.content.SearchRecentSuggestionsProvider

class MySuggestionsProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.bignerdranch.android.photogallery.MySuggestionProvider"
        const val MODE: Int = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }
}