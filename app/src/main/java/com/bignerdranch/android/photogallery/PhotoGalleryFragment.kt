package com.bignerdranch.android.photogallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.bignerdranch.android.photogallery.databinding.FragmentPhotoGalleryBinding
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
            binding.progress.isVisible = state.refresh == LoadState.Loading
        }
        binding.photoGrid.adapter = adapter?.withLoadStateHeaderAndFooter(
            header = PhotosLoaderStateAdapter(),
            footer = PhotosLoaderStateAdapter()
        )
    }

    private fun collectUIState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                photoGalleryViewModel.galleryItems.collectLatest { items ->
                    Log.d("PhotoGallery", "Items fetched")
                    adapter?.submitData(items)
                }
            }
        }
    }
}