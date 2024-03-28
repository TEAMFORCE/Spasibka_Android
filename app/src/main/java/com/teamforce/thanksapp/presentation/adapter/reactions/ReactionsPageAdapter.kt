package com.teamforce.thanksapp.presentation.adapter.reactions

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.response.GetReactionsResponse
import com.teamforce.thanksapp.databinding.ItemReactionBinding
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.branding.Branding

class ReactionsPageAdapter:
    PagingDataAdapter<GetReactionsResponse.InnerInfoLike, ReactionsPageAdapter.ReactionViewHolder>(
        DiffCallback
    ) {

    var onReactionClicked: ((userId: Int) -> Unit)? = null

    companion object {

        object DiffCallback : DiffUtil.ItemCallback<GetReactionsResponse.InnerInfoLike>() {
            override fun areItemsTheSame(
                oldItem: GetReactionsResponse.InnerInfoLike,
                newItem: GetReactionsResponse.InnerInfoLike
            ): Boolean {
                return oldItem.user.id == newItem.user.id
            }

            override fun areContentsTheSame(
                oldItem: GetReactionsResponse.InnerInfoLike,
                newItem: GetReactionsResponse.InnerInfoLike
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionViewHolder {
        val binding = ItemReactionBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return ReactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReactionViewHolder, position: Int) {
        val item = getItem(position)
        if(item != null){
            holder.bind(item)
            holder.binding.mainCardView.setOnClickListener {
                onReactionClicked?.invoke(item.user.id)
            }
        }
    }


    class ReactionViewHolder(val binding: ItemReactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: GetReactionsResponse.InnerInfoLike) {
            with(binding){
                userFi.text = data.user.name
                binding.userAvatar.setAvatarImageOrInitials(data.user.avatar, data.user.name)
            }
        }

    }
}