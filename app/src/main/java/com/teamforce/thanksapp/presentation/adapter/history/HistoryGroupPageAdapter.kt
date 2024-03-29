package com.teamforce.thanksapp.presentation.adapter.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ItemTransferGroupBinding
import com.teamforce.thanksapp.databinding.SeparatorDateTimeBinding
import com.teamforce.thanksapp.domain.models.history.HistoryItemModel.UserTransactionsModel.TransactionClass.*
import com.teamforce.thanksapp.domain.models.history.UserTransactionsGroupModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Extensions.setImage
import com.teamforce.thanksapp.utils.branding.Branding

class HistoryGroupPageAdapter(
    private val onUserClicked: (userId: Long) -> Unit
): PagingDataAdapter<UserTransactionsGroupModel, RecyclerView.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_transfer_group -> {
                val binding = ItemTransferGroupBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                UserGroupViewHolder(binding)
            }
            else -> {
                val binding = SeparatorDateTimeBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SeparatorViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            when (item) {
                is UserTransactionsGroupModel.DateTimeSeparator -> {
                    val viewHolder = holder as SeparatorViewHolder
                    viewHolder.bind(item)
                }
                is UserTransactionsGroupModel.UserGroupModel -> {
                    val viewHolder = holder as UserGroupViewHolder
                    viewHolder.bind(
                        item,
                        onUserClicked
                    )
                }

            }
        }
    }

    class SeparatorViewHolder(
        private val binding: SeparatorDateTimeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: UserTransactionsGroupModel.DateTimeSeparator) {
            binding.apply {
                separatorText.text = data.date
            }
        }
    }

    inner class UserGroupViewHolder(
        private val binding: ItemTransferGroupBinding,
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(
            data: UserTransactionsGroupModel.UserGroupModel,
            onUserClicked: (userId: Long) -> Unit
        ) {
            when(data.type){
                THANKS -> {
                    setTextInfo(data)
                    if(data.userPhoto != null){
                        binding.userAvatar.setImage(data.userPhoto)
                    }
                    binding.userItem.setOnClickListener {
                        onUserClicked.invoke(data.userId)
                    }
                }
                else -> {
                    binding.name.text = data.typeName
                    binding.userAvatar.avatarInitials = data.typeName
                }
//                CONTRIBUTION_TO_CHALLENGE -> TODO()
//                REWARD_FOR_CHALLENGE -> TODO()
//                REFUND_FROM_CHALLENGE -> TODO()
//                THANKS_FROM_SYSTEM -> TODO()
//                FIRED_THANKS -> TODO()
//                REFUND_FROM_BENEFIT -> TODO()
            }
            theming()
            binding.sendTv.text = if(data.ready != 0 ) "-${data.ready}" else data.ready.toString()
            binding.receivedTv.text = if(data.received != 0 ) "+${data.received}" else data.received.toString()
        }

        private fun theming(){
            Branding.traverseViews(binding.root)
        }

        private fun setTextInfo(item: UserTransactionsGroupModel.UserGroupModel){

            binding.name.text = item.name.takeIf { it.isNotNullOrEmptyMy() } ?:
                    item.nickname.takeIf { it.isNotNullOrEmptyMy() }
                    ?: item.tgName.takeIf { it.isNotNullOrEmptyMy() }
                    ?: ""

            binding.userAvatar.avatarInitials = item.name.takeIf { it.isNotNullOrEmptyMy() }
                ?: item.nickname.takeIf { it.isNotNullOrEmptyMy() }
                        ?: item.tgName.takeIf { it.isNotNullOrEmptyMy() }
                        ?: ""

        }
    }




    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is UserTransactionsGroupModel.UserGroupModel -> R.layout.item_transfer_group
            is UserTransactionsGroupModel.DateTimeSeparator -> R.layout.separator_date_time
            null -> throw UnsupportedOperationException("Unknown view")
        }
    }

    companion object {
        const val TAG = "HistoryGroupPageAdapter"

        private val DiffCallback = object : DiffUtil.ItemCallback<UserTransactionsGroupModel>() {
            override fun areItemsTheSame(
                oldItem: UserTransactionsGroupModel,
                newItem: UserTransactionsGroupModel
            ): Boolean {
                return (oldItem is UserTransactionsGroupModel.UserGroupModel &&
                        newItem is UserTransactionsGroupModel.UserGroupModel &&
                        oldItem.userId == newItem.userId) ||
                        (oldItem is UserTransactionsGroupModel.DateTimeSeparator &&
                                newItem is UserTransactionsGroupModel.DateTimeSeparator &&
                                oldItem.uuid == newItem.uuid)
            }

            override fun areContentsTheSame(
                oldItem: UserTransactionsGroupModel,
                newItem: UserTransactionsGroupModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}