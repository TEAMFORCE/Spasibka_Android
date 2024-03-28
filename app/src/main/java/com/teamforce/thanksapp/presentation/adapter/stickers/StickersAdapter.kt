package com.teamforce.thanksapp.presentation.adapter.stickers

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.entities.Stickers.StickerEntity
import com.teamforce.thanksapp.databinding.ItemHolidayStickerBinding
import com.teamforce.thanksapp.presentation.viewmodel.CheckedSticker
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.addBaseUrl


class StickersAdapter(
    private val onStickerClicked: (stickerId: CheckedSticker) -> Unit
) : ListAdapter<StickerEntity, StickersAdapter.StickerViewHolder>(DiffCallback) {

    companion object{

        object DiffCallback : DiffUtil.ItemCallback<StickerEntity>() {
            override fun areItemsTheSame(
                oldItem: StickerEntity,
                newItem: StickerEntity
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: StickerEntity,
                newItem: StickerEntity
            ): Boolean {
                return oldItem == newItem
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerViewHolder {
        val binding = ItemHolidayStickerBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return StickerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
        with(holder.binding){

            Glide.with(root.context)
                .load(currentList[position].image.addBaseUrl().toUri())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(createShimmerDrawable(shapeImageView.context))
                .error(R.drawable.ic_anon_avatar)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(shapeImageView)


            shapeImageView.setOnClickListener {
                onStickerClicked.invoke(CheckedSticker(currentList[position].id, currentList[position].image))
            }
        }

    }

    private fun createShimmerDrawable(context: Context): ShimmerDrawable{
        val shimmer = Shimmer.ColorHighlightBuilder() // The attributes for a ShimmerDrawable is set by this builder
            .setBaseColor(context.getColor(R.color.shimmerColor))
            .setDuration(500) // how long the shimmering animation takes to do one full sweep
            .setBaseAlpha(0.7f) //the alpha of the underlying children
            .setHighlightAlpha(0.7f) // the shimmer alpha amount
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .setAutoStart(true)
            .build()

// This is the placeholder for the imageView
        val shimmerDrawable = ShimmerDrawable().apply {
            setShimmer(shimmer)
            startShimmer()
        }
        return shimmerDrawable

    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    class StickerViewHolder(val binding: ItemHolidayStickerBinding) : RecyclerView.ViewHolder(binding.root)
}