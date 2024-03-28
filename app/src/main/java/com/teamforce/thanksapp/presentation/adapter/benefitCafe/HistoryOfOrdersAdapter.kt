package com.teamforce.thanksapp.presentation.adapter.benefitCafe

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.entities.benefit.Order
import com.teamforce.thanksapp.databinding.ItemHistoryOfOfferBinding
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.glide.setImageRounded

class HistoryOfOrdersAdapter (
    private val onOrderClicked: (offerId: Int) -> Unit
) : PagingDataAdapter<Order, HistoryOfOrdersAdapter.ViewHolder>(DiffCallback) {

    override fun onBindViewHolder(holder: HistoryOfOrdersAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryOfOrdersAdapter.ViewHolder {
        val binding = ItemHistoryOfOfferBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(
                oldItem: Order,
                newItem: Order
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: Order,
                newItem: Order
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(val binding: ItemHistoryOfOfferBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Order) {
            bindOffer(item)
        }

        private fun bindOffer(item: Order) {
            with(binding) {
                nameOfferTv.text = item.name
                if(item.total != null){
                    cardPrice.visible()
                    offerPriceTv.text = item.total.toString()
                }else{
                    cardPrice.invisible()
                }
                dateOfferTv.text = root.context.getString(
                    R.string.dateCreatedOffer,
                    item.id,
                    parseDateTimeAnotherOutput(dateTime = item.createdAt, root.context)
                )
                descriptionOfferTv.text = item.description
                if (!item.images.isNullOrEmpty()) {
                    image.setImageRounded("${item.images[0].link}")
                } else {
                    image.setImageRounded("")
                }
                handleStatus(item.currentStatus, root.context)


                mainContent.setOnClickListener {
                    // Клик по карточке заказа
                }
                theming()
            }
        }

        private fun theming(){
            binding.cardPrice.setCardBackgroundColor(Color.parseColor(Branding.brand?.colorsJson?.secondaryBrandColor))
            binding.offerPriceTv.setTextColor(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
            binding.currencyIv.setColorFilter(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
            binding.mainContent.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.generalBackgroundColor)))
            binding.descriptionOfferTv.setTextColor(ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.generalContrastSecondaryColor)))
            binding.nameOfferTv.setTextColor(ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.generalContrastColor)))
            binding.dateOfferTv.setTextColor(ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.generalContrastSecondaryColor)))
        }

        private fun handleStatus(status: Int, context: Context){
            val statusStringsMap = mapOf(
                0 to context.getString(R.string.inCart),  // Синий
                1 to context.getString(R.string.considering), // Синий
                5 to context.getString(R.string.accepted), // Синий
                6 to context.getString(R.string.acceptedByCustomer),  // Желтый
                7 to context.getString(R.string.purchased), // Желтый
                8 to context.getString(R.string.readyForDelivery), // Синий
                9 to context.getString(R.string.sentOrDelivered), // Синий
                10 to context.getString(R.string.ready), // Зеленый
                20 to context.getString(R.string.declined), // красный
                21 to context.getString(R.string.cancelledStatus) // серый
            )

            val stringStatus = statusStringsMap[status]
            binding.offerStatusTv.text = stringStatus
            when(status) {
                0, 1, 5, 8, 9 -> {
                    // Синий
                    binding.offerStatusCard.setCardBackgroundColor(Color.parseColor(Branding.brand?.colorsJson?.minorInfoSecondaryColor))
                    binding.offerStatusTv.setTextColor(Color.parseColor(Branding.brand?.colorsJson?.minorInfoColor))
                }
                6, 7 -> {
                    // Желтый
                    binding.offerStatusCard.setCardBackgroundColor(Color.parseColor(Branding.brand?.colorsJson?.minorWarningSecondaryColor))
                    binding.offerStatusTv.setTextColor(Color.parseColor(Branding.brand?.colorsJson?.minorWarningColor))
                }
                10 -> {
                    // Зеленый
                    binding.offerStatusCard.setCardBackgroundColor(Color.parseColor(Branding.brand?.colorsJson?.minorSuccessSecondaryColor))
                    binding.offerStatusTv.setTextColor(Color.parseColor(Branding.brand?.colorsJson?.minorSuccessColor))

                }
                20 -> {
                    // Красный
                    binding.offerStatusCard.setCardBackgroundColor(Color.parseColor(Branding.brand?.colorsJson?.minorErrorSecondaryColor))
                    binding.offerStatusTv.setTextColor(Color.parseColor(Branding.brand?.colorsJson?.minorErrorColor))
                }
                21 -> {
                    // Серый
                    binding.offerStatusCard.setCardBackgroundColor(Color.parseColor(Branding.brand?.colorsJson?.generalNegativeColor))
                    binding.offerStatusTv.setTextColor(Color.parseColor(Branding.brand?.colorsJson?.minorNegativeSecondaryColor))
                }
                else -> {
                    binding.offerStatusCard.invisible()
                }
            }
        }
    }
}