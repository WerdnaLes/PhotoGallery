package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.app.PendingIntent
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
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.COLOR_SCHEME_DARK
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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

    // Scheduling a WorkRequest (procedure isn't compatible with my Paging (it was in onCreate())):
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.UNMETERED)
//            .build()
//        val workRequest =
//            OneTimeWorkRequestBuilder<PollWorker>()
//                .setConstraints(constraints)
//                .build()
//        WorkManager.getInstance(requireContext())


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
        // Open selected image in a browser:
        adapter = MyPagingAdapter { photoPageUri ->
            // Open selected image in a browser:
//            val intent =
//                Intent(Intent.ACTION_VIEW, photoPageUri)
//            startActivity(intent)
            // Open selected image via WebView:
//            findNavController().navigate(
//                PhotoGalleryFragmentDirections.showPhoto(photoPageUri)
//            )
            // Open selected image via CustomTabs:
            val darkParams = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(
                    requireActivity()
                        .getColor(androidx.appcompat.R.color.primary_dark_material_dark)
                )
                .build()
            CustomTabsIntent.Builder()
                .setColorSchemeParams(COLOR_SCHEME_DARK, darkParams)
                .setShowTitle(true)
                .build()
                .launchUrl(requireContext(), photoPageUri)
        }
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
        photoGalleryViewModel.setQuery("")
    }

    // Notification Builder example:
    private fun notifyUser() {
        val intent = MainActivity.newIntent(requireContext())
        val pendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val resources = requireContext().resources
        val notification = NotificationCompat
            .Builder(requireContext(), NOTIFICATION_CHANNEL_ID)
            .setTicker(resources.getString(R.string.new_pictures_title))
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .setContentTitle(resources.getString(R.string.new_pictures_title))
            .setContentText(resources.getString(R.string.new_pictures_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(requireContext()).notify(0, notification)
    }
}