package com.teamforce.thanksapp.presentation.adapter.employees

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
import com.teamforce.thanksapp.databinding.ItemEmployeeBinding
import com.teamforce.thanksapp.domain.models.employees.EmployeeModel
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.visible

class EmployeesAdapter(
    private val onEmployeeClicked: (employeeId: Int) -> Unit
) : PagingDataAdapter<EmployeeModel, EmployeesAdapter.ViewHolder>(DiffCallback) {

    override fun onBindViewHolder(holder: EmployeesAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EmployeesAdapter.ViewHolder {
        val binding = ItemEmployeeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<EmployeeModel>() {
            override fun areItemsTheSame(
                oldItem: EmployeeModel,
                newItem: EmployeeModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: EmployeeModel,
                newItem: EmployeeModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(val binding: ItemEmployeeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EmployeeModel) {
            bindOffer(item)
        }

        private fun bindOffer(item: EmployeeModel) {
            with(binding) {
                setImage(item)
                setName(item)
                if (item.jobTitle.isNullOrEmpty()){
                    userPosition.invisible()
                }else{
                    userPosition.visible()
                    userPosition.text = item.jobTitle
                }
                userItem.setOnClickListener {
                    onEmployeeClicked(item.id)
                }
                Branding.traverseViews(binding.root)
            }
        }

        private fun setName(item: EmployeeModel){
            if(item.name?.trim().isNullOrEmpty()) binding.userNameLabelTv.text = item.tgName
            else binding.userNameLabelTv.text = item.name
        }

        private fun setImage(item: EmployeeModel) {
            binding.userAvatar.setAvatarImageOrInitials(item.photo, item.name)
        }
    }

}
