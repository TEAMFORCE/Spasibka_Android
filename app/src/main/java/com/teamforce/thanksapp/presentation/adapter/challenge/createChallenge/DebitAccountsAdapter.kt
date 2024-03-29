package com.teamforce.thanksapp.presentation.adapter.challenge.createChallenge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ItemDebitAccountBinding
import com.teamforce.thanksapp.domain.models.challenge.createChallenge.DebitAccountModel

class DebitAccountsAdapter(
    private val onAccountClicked: (account: DebitAccountModel) -> Unit
) : ListAdapter<DebitAccountModel, DebitAccountsAdapter.AccountViewHolder>(DiffCallback) {

    companion object {

        object DiffCallback : DiffUtil.ItemCallback<DebitAccountModel>() {
            override fun areItemsTheSame(
                oldItem: DebitAccountModel,
                newItem: DebitAccountModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: DebitAccountModel,
                newItem: DebitAccountModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemDebitAccountBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        with(holder.binding) {
            if(currentList[position].myAccount){
                accountNameTv.text = root.context.getString(R.string.personalAccount)
            }else{
                accountNameTv.text = currentList[position].organizationName
            }
            accountAmountTv.text = currentList[position].amount.toString()
            accountItem.setOnClickListener {
                onAccountClicked(currentList[position])
            }
        }

    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    class AccountViewHolder(val binding: ItemDebitAccountBinding) : RecyclerView.ViewHolder(binding.root)
}
