package com.teamforce.thanksapp.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teamforce.thanksapp.databinding.ItemGeneralImageBinding
import com.teamforce.thanksapp.utils.glide.setImage


class GeneralViewImageAdapter(
    private var photos: List<String?>,
    private val onImageClicked: (listOfPhotos: List<String?>, clickedView: View, position: Int) -> Unit
) : RecyclerView.Adapter<GeneralViewImageAdapter.ImageViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemGeneralImageBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        with(holder.binding) {
            if (photos[position] != null) {
                image.setImage(photos[position]!!)
                imageCard.setOnClickListener { view ->
                    onImageClicked.invoke(photos, view, position)
                }
            }

        }
    }


    override fun getItemCount(): Int {
        return photos.size
    }

    class ImageViewHolder(val binding: ItemGeneralImageBinding) :
        RecyclerView.ViewHolder(binding.root)
}