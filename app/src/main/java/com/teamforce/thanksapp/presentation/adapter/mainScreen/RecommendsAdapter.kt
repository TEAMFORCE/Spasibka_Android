package com.teamforce.thanksapp.presentation.adapter.mainScreen

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ItemRecommendBinding
import com.teamforce.thanksapp.domain.models.recommendations.RecommendModel
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.glide.setImage
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.visible


class RecommendsAdapter(
    private val onItemClicked: (itemId: Long, type: RecommendModel.RecommendObjectType, marketId: Int?) -> Unit
) : ListAdapter<RecommendModel, RecommendsAdapter.RecommendViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendViewHolder {
        val binding = ItemRecommendBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return RecommendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) holder.bind(item, position)
    }



    override fun getItemCount(): Int {
        return currentList.size
    }

    companion object {
        const val TAG = "RecommendsAdapter"

        object DiffCallback : DiffUtil.ItemCallback<RecommendModel>() {
            override fun areItemsTheSame(
                oldItem: RecommendModel,
                newItem: RecommendModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: RecommendModel,
                newItem: RecommendModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }


    inner class RecommendViewHolder(val binding: ItemRecommendBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecommendModel, position: Int) {
            binding.headerTv.text = item.header
            binding.titleTv.text = item.name
            if(item.photo != null){
                binding.topFrame.background = null
                binding.imageBackground.visible()
                binding.imageBackground.setImage(item.photo)
            }else{
                binding.imageBackground.invisible()
                binding.topFrame.background = getThemableStandardBackground()
            }
            binding.newBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
            binding.newBadge.visibility = if(item.isNew) View.VISIBLE else View.GONE
            Branding.traverseViews(binding.root)
            binding.mainCard.setOnClickListener {
                onItemClicked(item.id, item.type, item.marketplaceId)
            }
        }

        private fun getThemableStandardBackground(): Drawable{
            val gradientDrawable = ResourcesCompat.getDrawable(
                binding.root.resources,
                R.drawable.gradient_for_recommend_item,
                null
            ) as GradientDrawable
            return gradientDrawable.apply {
                colors = intArrayOf(
                    Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor),
                    Color.parseColor(Branding.brand.colorsJson.mainBrandColor)
                )
            }
        }


    }

}