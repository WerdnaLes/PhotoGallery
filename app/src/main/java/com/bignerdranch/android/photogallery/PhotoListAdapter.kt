package com.bignerdranch.android.photogallery

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import coil.transform.CircleCropTransformation
import com.bignerdranch.android.photogallery.api.GalleryItem
import com.bignerdranch.android.photogallery.databinding.ListItemGalleryBinding

class PhotoViewHolder(
    private val binding: ListItemGalleryBinding
) : ViewHolder(binding.root) {
    fun bind(galleryItem: GalleryItem?, onItemClicked: (Uri) -> Unit) {
        // Adapt the image with help of Coil:
        binding.itemImageView.load(galleryItem?.url) {
            crossfade(true)
            placeholder(ColorDrawable(Color.TRANSPARENT))
        }
        binding.root.setOnClickListener {
            galleryItem?.photoPageUri?.let { item -> onItemClicked(item) }
        }
    }
}

//class PhotoListAdapter(private val galleryItems: List<GalleryItem>) :
//    RecyclerView.Adapter<PhotoViewHolder>() {
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        val binding =
//            ListItemGalleryBinding.inflate(inflater, parent, false)
//        return PhotoViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
//        val item = galleryItems[position]
//        holder.bind(item)
//    }
//
//    override fun getItemCount() = galleryItems.size
//}

class MyPagingAdapter(private val onItemClicked: (Uri) -> Unit) :
    PagingDataAdapter<GalleryItem, PhotoViewHolder>(PhotoDiffCallback()) {
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClicked)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
            ListItemGalleryBinding.inflate(inflater, parent, false)
        return PhotoViewHolder(binding)
    }

}

class PhotoDiffCallback : DiffUtil.ItemCallback<GalleryItem>() {
    override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
        return oldItem == newItem
    }
}