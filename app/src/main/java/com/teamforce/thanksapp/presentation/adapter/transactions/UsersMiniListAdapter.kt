package com.teamforce.thanksapp.presentation.adapter.transactions

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.teamforce.thanksapp.data.response.UserListItem

import com.teamforce.thanksapp.databinding.ItemMiniUserForTransferBinding
import com.teamforce.thanksapp.databinding.ItemNewTransactionBtnBinding
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.branding.Branding

class UsersMiniListAdapter : ListAdapter<UserListItem, RecyclerView.ViewHolder>(DiffCallback) {

    var onSomeonesClicked: ((user: UserListItem.UserBean) -> Unit)? = null
    var onNewTransactionClicked: (() -> Unit)? = null

    companion object {

        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BUTTON = 2

        object DiffCallback : DiffUtil.ItemCallback<UserListItem>() {
            override fun areItemsTheSame(
                oldItem: UserListItem,
                newItem: UserListItem
            ): Boolean {
                return when {
                    oldItem is UserListItem.UserBean && newItem is UserListItem.UserBean -> oldItem.userId == newItem.userId
                    oldItem is UserListItem.NewTransactionBtn && newItem is UserListItem.NewTransactionBtn -> true
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: UserListItem,
                newItem: UserListItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val binding = ItemMiniUserForTransferBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                UserViewHolder(binding)
            }
            VIEW_TYPE_BUTTON -> {
                val binding = ItemNewTransactionBtnBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                ButtonViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_USER -> {
                holder as UserViewHolder
                val userItem = getItem(position) as UserListItem.UserBean
                holder.bind(userItem)
            }
            VIEW_TYPE_BUTTON -> {
                holder as ButtonViewHolder
                holder.bind()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is UserListItem.UserBean -> VIEW_TYPE_USER
            is UserListItem.NewTransactionBtn -> VIEW_TYPE_BUTTON
            else -> throw IllegalArgumentException("Unknown item type at position $position")
        }
    }

    inner class ButtonViewHolder(val binding: ItemNewTransactionBtnBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(){
            binding.imageBtn.setBackgroundColor(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
            binding.btnItem.setOnClickListener {
                onNewTransactionClicked?.invoke()
            }
        }
    }



    inner class UserViewHolder(val binding: ItemMiniUserForTransferBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: UserListItem.UserBean){
            with(binding) {
                setTextInfo(item, binding)
                userItem.tag = item
                userAvatar.setAvatarImageOrInitials(item.photo, "${item.firstname} ${item.surname}")
                userAvatar.invalidate()
                userItem.setOnClickListener {
                    onSomeonesClicked?.invoke(item)
                }
            }
            settingCustomColors()
        }

        private fun settingCustomColors(){
            binding.name.setTextColor(Color.parseColor(Branding.appTheme.generalContrastColor))
            binding.userAvatar.avatarInitialsBackgroundGradientColorStart = Color.parseColor(
                Branding.appTheme.mainBrandColor)
            binding.userAvatar.avatarInitialsBackgroundGradientColorEnd = Color.parseColor(Branding.appTheme.secondaryBrandColor)
        }

        private fun setTextInfo(item: UserListItem.UserBean, binding: ItemMiniUserForTransferBinding){
            if(item.surname.isNullOrEmpty()){
                binding.name.text = item.firstname
            }else{
                binding.name.text = "${item.firstname} ${item.surname.first()}."
            }
        }
    }
}