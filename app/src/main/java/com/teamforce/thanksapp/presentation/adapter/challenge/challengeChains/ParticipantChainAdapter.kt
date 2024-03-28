package com.teamforce.thanksapp.presentation.adapter.challenge.challengeChains

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.teamforce.thanksapp.databinding.ItemParticipantChainBinding
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ParticipantChainModel
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.utils.branding.Branding



class ParticipantChainAdapter: PagingDataAdapter<ParticipantChainModel, ParticipantChainAdapter.ParticipantChainViewHolder>(DiffCallback) {

    var onSomeonesClicked: ((userId: Long, clickedView: View) -> Unit)? = null

    companion object {

        object DiffCallback : DiffUtil.ItemCallback<ParticipantChainModel>() {
            override fun areItemsTheSame(
                oldItem: ParticipantChainModel,
                newItem: ParticipantChainModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ParticipantChainModel,
                newItem: ParticipantChainModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantChainViewHolder {
        val binding = ItemParticipantChainBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return ParticipantChainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantChainViewHolder, position: Int) {
        val item = getItem(position)
        if(item != null){
            holder.bind(item)
        }

    }



    inner class ParticipantChainViewHolder(val binding: ItemParticipantChainBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: ParticipantChainModel){
            with(binding) {
                userNameLabelTv.text = item.name
                userItem.tag = item
                binding.userAvatar.setAvatarImageOrInitials(item.photo, item.name)
                userItem.setOnClickListener {
                    onSomeonesClicked?.invoke(item.id, it)
                }
            }
            settingCustomColors()
        }

        private fun settingCustomColors(){
            binding.userNameLabelTv.setTextColor(Color.parseColor(Branding.appTheme.generalContrastColor))
            binding.userAvatar.avatarInitialsBackgroundGradientColorStart = Color.parseColor(
                Branding.appTheme.mainBrandColor)
            binding.userAvatar.avatarInitialsBackgroundGradientColorEnd = Color.parseColor(Branding.appTheme.secondaryBrandColor)
        }
    }
}