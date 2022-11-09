package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnSuggestionListener
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.bignerdranch.android.photogallery.databinding.FragmentPhotoGalleryBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PhotoGalleryFragment : Fragment() {
    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels()

    private var _binding: FragmentPhotoGalleryBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private var adapter: MyPagingAdapter? = null
    private var searchView: SearchView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentPhotoGalleryBinding.inflate(inflater, container, false)
        // Set the recyclerView in Grid:
        binding.photoGrid.layoutManager =
            GridLayoutManager(context, 3)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMenuHost()
        initView()
        collectUIState()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter = null
        _binding = null
    }

    private fun initView() {
        adapter = MyPagingAdapter()
        adapter?.addLoadStateListener { state ->
            val refreshState = state.refresh
            binding.progress.isVisible = refreshState == LoadState.Loading
            if (refreshState is LoadState.Error) {
                Snackbar.make(
                    binding.root,
                    refreshState.error.localizedMessage ?: "",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        binding.photoGrid.adapter = adapter?.withLoadStateHeaderAndFooter(
            header = PhotosLoaderStateAdapter(),
            footer = PhotosLoaderStateAdapter()
        )
    }

    private fun collectUIState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                photoGalleryViewModel.uiState.collectLatest { state ->
                    Log.d("PhotoGallery", "Items fetched")
                    searchView?.setQuery(state.query, false)
                    saveRecentQuery(state.query)
                    adapter?.submitData(state.images)
                }
            }
        }
    }

    private fun initMenuHost() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_photo_gallery, menu)
                val searchItem: MenuItem =
                    menu.findItem(R.id.menu_item_search)
                searchView = searchItem.actionView as? SearchView
                val searchManager =
                    requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
                searchView?.apply {
                    queryHint = resources.getString(R.string.search_hint)
                    setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
                    setIconifiedByDefault(false)
                }


                // Submitting query in a search View
                searchView?.setOnQueryTextListener(object :
                    SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        Log.d("PhotoGalleryFragment", " QueryTextSubmit: $query")
                        photoGalleryViewModel.setQuery(query ?: "")
                        searchView?.clearFocus()
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        Log.d("PhotoGalleryFragment", "QueryTextChange: $newText")
                        return false
                    }
                })

                // Clicking on suggestion
                searchView?.setOnSuggestionListener(object : OnSuggestionListener {
                    override fun onSuggestionSelect(position: Int): Boolean {
                        return false
                    }

                    @SuppressLint("Range")
                    override fun onSuggestionClick(position: Int): Boolean {
                        val cursor = searchView?.suggestionsAdapter?.getItem(position) as Cursor
                        val selection =
                            cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
                        searchView?.setQuery(selection, true)
                        return true
                    }
                })
            }


            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    // Return to Home page
                    R.id.menu_item_clear -> {
                        Log.d("PhotoGalleryFragment", "Launching home page")
                        photoGalleryViewModel.setQuery("")
                        true
                    }
                    // Clear search history after confirmation dialog
                    R.id.clear_history -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle(R.string.clear_history_confirmation)
                            .setPositiveButton(
                                resources.getString(R.string.clear_history_positive)
                            ) { _, _ ->
                                clearSearchHistory()
                            }
                            // Open app settings to grant permissions:
                            .setNegativeButton(resources.getString(R.string.clear_history_negative)) { _, _ ->
                                // Do nothing
                            }
                            .create().show()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun saveRecentQuery(query: String) {
        SearchRecentSuggestions(
            requireContext(),
            MySuggestionsProvider.AUTHORITY,
            MySuggestionsProvider.MODE
        )
            .saveRecentQuery(query, null)
    }

    private fun clearSearchHistory() {
        SearchRecentSuggestions(
            requireContext(),
            MySuggestionsProvider.AUTHORITY,
            MySuggestionsProvider.MODE
        ).clearHistory()
    }
}