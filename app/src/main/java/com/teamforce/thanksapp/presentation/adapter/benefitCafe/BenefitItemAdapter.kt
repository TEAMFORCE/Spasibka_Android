package com.teamforce.thanksapp.presentation.adapter.benefitCafe

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.teamforce.thanksapp.databinding.ItemBenefitBinding
import com.teamforce.thanksapp.domain.models.benefit.BenefitModel
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.glide.setImage


class BenefitItemAdapter(
    private val onOfferClicked: (offerId: Int) -> Unit
) : PagingDataAdapter<BenefitModel, BenefitItemAdapter.ViewHolder>(DiffCallback) {

    override fun onBindViewHolder(holder: BenefitItemAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BenefitItemAdapter.ViewHolder {
        val binding = ItemBenefitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<BenefitModel>() {
            override fun areItemsTheSame(
                oldItem: BenefitModel,
                newItem: BenefitModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: BenefitModel,
                newItem: BenefitModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(val binding: ItemBenefitBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BenefitModel) {
            bindOffer(item)
        }

        private fun bindOffer(item: BenefitModel) {
            with(binding) {
                mainCard.setOnClickListener {
                    onOfferClicked(item.id)
                }
                offerNameTv.text = item.name
                offerPriceTv.text = item.price?.priceInThanks.toString()
                setImage(item)
            }
            theming()
        }

        private fun setImage(item: BenefitModel){
            if (!item.images.isNullOrEmpty()) {
                item.images.forEach { image ->
                    if(image.forShowcase){
                        binding.image.setImage(image.link?: "")
                        return
                    }else{
                        binding.image.setImage(item.images[0].link?: "")
                    }
                }
            } else {
                binding.image.setBackgroundColor(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
            }
        }

        private fun theming(){

        }
    }
}