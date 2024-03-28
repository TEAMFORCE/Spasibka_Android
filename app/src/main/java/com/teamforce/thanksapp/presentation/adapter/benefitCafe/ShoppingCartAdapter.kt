package com.teamforce.thanksapp.presentation.adapter.benefitCafe

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.entities.benefit.BenefitItemEntity
import com.teamforce.thanksapp.data.entities.benefit.OfferInCart
import com.teamforce.thanksapp.databinding.ItemShoppingCartBinding
import com.teamforce.thanksapp.utils.addBaseUrl
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.traverseViewsTheming
import com.teamforce.thanksapp.utils.visible


class ShoppingCartAdapter(
    private val onRefuseOfferClicked: (orderId: Int) -> Unit,
    private val onCheckBoxOfferClicked: (orderId: Int, amount: Int, position: Int, isChecked: Boolean) -> Unit,
    private val onCounterOfferClicked: (orderId: Int, amount: Int, position: Int, isChecked: Boolean) -> Unit,
    private val onImageOfferClicked: (images: List<BenefitItemEntity.Image>, view: View) -> Unit,
    private val onOfferClicked: (offerId: Int) -> Unit
) : PagingDataAdapter<OfferInCart, ShoppingCartAdapter.OfferViewHolder>(DiffCallback) {

    private var checkedItemCounter: Int = 0

    fun getCheckedItemCounter() = checkedItemCounter

    companion object {

        object DiffCallback : DiffUtil.ItemCallback<OfferInCart>() {
            override fun areItemsTheSame(
                oldItem: OfferInCart,
                newItem: OfferInCart
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: OfferInCart,
                newItem: OfferInCart
            ): Boolean {
                return oldItem == newItem
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val binding = ItemShoppingCartBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return OfferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item, position)
        }
    }

    override fun onBindViewHolder(
        holder: OfferViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val item = getItem(position)
        if (item != null) {
            when (val latestPayload = payloads.lastOrNull()) {
                is CartChangePayload.ItemCount -> holder.bindTotalPrice(
                    quantity = latestPayload.quantity,
                    price = latestPayload.price
                )

                else -> onBindViewHolder(holder, position)
            }
        }
    }

    fun updateTotalPrice(position: Int?, quantity: Int, price: Int) {
        if (position != null) {
            notifyItemChanged(position, CartChangePayload.ItemCount(quantity = quantity, price = price))
        }
    }

    inner class OfferViewHolder(val binding: ItemShoppingCartBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OfferInCart, position: Int) {
            bindOffer(item, position)
        }

        private fun bindOffer(item: OfferInCart, position: Int) {
            with(binding) {
                nameOfferTv.text = item.name
                if (item.price != null) {
                    offerPriceTv.visible()
                    offerPriceTv.text = item.total.toString()
                } else {
                    offerPriceTv.invisible()
                }

                setImage(item, binding)

                if (item.isChecked) checkedItemCounter++
                selectionCheckbox.isChecked = item.isChecked

                refuseBtn.setOnClickListener {
                    if (item.isChecked) checkedItemCounter--
                    onRefuseOfferClicked(item.id)
                }

                selectionCheckbox.setOnClickListener {
                    if (selectionCheckbox.isChecked) checkedItemCounter++
                    else checkedItemCounter--
                    onCheckBoxOfferClicked(
                        item.offerId,
                        counter.currentValue,
                        position,
                        selectionCheckbox.isChecked
                    )
                }
                counter.currentValue = item.quantity
                counter.setListener { value ->
                    // Прокинуть значение счетчика в фрагмент
                    onCounterOfferClicked(
                        item.offerId,
                        value,
                        position,
                        selectionCheckbox.isChecked
                    )
                }
                infoLinear.setOnClickListener {
                    onOfferClicked(item.offerId)
                }
            }
            theming()
        }

        fun bindTotalPrice(
            quantity: Int,
            price: Int?
        ) {
            if (price != null) {
                binding.offerPriceTv.visible()
                binding.offerPriceTv.text = (price * quantity).toString()
            } else {
                binding.offerPriceTv.invisible()
            }
        }

        private fun setImage(item: OfferInCart, binding: ItemShoppingCartBinding) {
            if (!item.images.isNullOrEmpty()) {
                binding.image.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(binding.root.context)
                    .load(item.images[0].link?.addBaseUrl())
                    .error(R.drawable.ic_logo)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.image)

                binding.image.setOnClickListener { view ->
                    onImageOfferClicked(item.images, view)
                }
            } else {
                binding.image.setOnClickListener(null)
            }
        }

        private fun theming() {
            binding.root.traverseViewsTheming()
            //binding.cardPrice.setCardBackgroundColor(Color.parseColor(Branding.appTheme.secondaryBrandColor))
            binding.image.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(Branding.appTheme.mainBrandColor))
        }
    }
}

private sealed interface CartChangePayload {
    data class ItemCount(val quantity: Int, val price: Int) : CartChangePayload

}