package com.teamforce.thanksapp.presentation.adapter.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.teamforce.thanksapp.data.response.UserListItem.UserBean
import com.teamforce.thanksapp.databinding.ItemUserBinding
import com.teamforce.thanksapp.utils.branding.Branding


class UsersPagingAdapter: PagingDataAdapter<UserBean, UsersPagingAdapter.UserViewHolder>(DiffCallback) {

    var onSomeonesClicked: ((user: UserBean, clickedView: View) -> Unit)? = null

    companion object {

        object DiffCallback : DiffUtil.ItemCallback<UserBean>() {
            override fun areItemsTheSame(
                oldItem: UserBean,
                newItem: UserBean
            ): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(
                oldItem: UserBean,
                newItem: UserBean
            ): Boolean {
                return oldItem == newItem
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = getItem(position)
        if(item != null){
            holder.binding.userItemFl.transitionName = item.userId.toString()
            holder.binding.userItem.onUserClick =  {
                onSomeonesClicked?.invoke(item, holder.binding.userItemFl)
            }
            holder.bind(item)
        }

    }



    inner class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: UserBean){
            with(binding) {
                userItem.setUserData("${item.firstname} ${item.surname}", "${item.tgName}")
                userItem.setUserImage(item.photo, "${item.firstname} ${item.surname}", item.tgName)
                userItem.setThemeColor(Branding.brand.colorsJson)
            }
        }
    }
}