package com.teamforce.thanksapp.presentation.adapter.mainScreen

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.model.content.GradientType
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ItemMainScreenEventBirthdayBinding
import com.teamforce.thanksapp.databinding.ItemMainScreenEventChallengeBinding
import com.teamforce.thanksapp.databinding.ItemMainScreenEventTransactionBinding
import com.teamforce.thanksapp.domain.models.events.EventDataModel
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.utils.SpannableUtils
import com.teamforce.thanksapp.utils.addBaseUrl
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.doubleQuoted
import com.teamforce.thanksapp.utils.glide.setImage
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.removeLinksUnderline
import com.teamforce.thanksapp.utils.visible


class EventsMainScreenAdapter :
    PagingDataAdapter<EventDataModel, RecyclerView.ViewHolder>(DiffCallback()) {


    var onTransactionClicked: ((transactionId: Int, message: String) -> Unit)? = null


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            when (holder) {
                is ChallengeViewHolder -> holder.bind(item)
                is ReportOfChallengeViewHolder -> holder.bind(item)
                is TransactionViewHolder -> holder.bind(item)
                is BirthdayViewHolder -> holder.bind(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.CHALLENGE.ordinal, ViewType.REPORT_OF_CHALLENGE.ordinal -> {
                val binding = ItemMainScreenEventChallengeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ChallengeViewHolder(binding)
            }

            ViewType.TRANSACTION.ordinal -> {
                val binding = ItemMainScreenEventTransactionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TransactionViewHolder(binding)
            }

            ViewType.BIRTHDAY.ordinal -> {
                val binding = ItemMainScreenEventBirthdayBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                BirthdayViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Unsupported viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item?.typeOfObject) {
            ObjectsToLike.CHALLENGE, ObjectsToLike.REPORT_OF_CHALLENGE -> ViewType.CHALLENGE.ordinal
            ObjectsToLike.TRANSACTION -> ViewType.TRANSACTION.ordinal
            ObjectsToLike.BIRTHDAY -> ViewType.BIRTHDAY.ordinal
            else -> throw IllegalArgumentException("Unsupported typeOfObject")
        }
    }

    inner class ChallengeViewHolder(val binding: ItemMainScreenEventChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EventDataModel) {
            binding.text.text = binding.root.context.getString(R.string.main_fragment_new_challenge)
            if (!item.photos.isNullOrEmpty() && item.photos.first() != null) {
                binding.contentLinear.background = null
                binding.image.visible()
                binding.image.setImage(item.photos.first())
            } else {
                binding.image.invisible()
                binding.contentLinear.background = getThemableRadialGradient(binding.root.context)

            }
            binding.card.setOnClickListener {
                startActivityByMainLink(item.mainlink, binding.root.context)
            }
        }
    }

    inner class ReportOfChallengeViewHolder(val binding: ItemMainScreenEventChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EventDataModel) {
            binding.text.text = binding.root.context.getString(R.string.main_fragment_new_winner)
            if (!item.photos.isNullOrEmpty() && item.photos.first() != null) {
                binding.contentLinear.background = null
                binding.image.visible()
                binding.image.setImage(item.photos.first())
            } else {
                binding.image.invisible()
                binding.contentLinear.background = getThemableRadialGradient(binding.root.context)

            }
            binding.card.setOnClickListener {
                startActivityByMainLink(item.mainlink, binding.root.context)
            }
        }
    }

    inner class TransactionViewHolder(val binding: ItemMainScreenEventTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EventDataModel) {
            val transactionWithMe = item.text.getOrNull(0) != '<'
            handleСircle(item, transactionWithMe)
            setBackground(transactionWithMe)
            binding.text.text = processText(item.text, transactionWithMe, binding.root.context)
            binding.card.setOnClickListener {
                onTransactionClicked?.invoke(item.id, item.text)
            }
        }

        private fun setBackground(transactionWithMe: Boolean) {
            val backColor =
                if (transactionWithMe) Branding.brand.colorsJson.mainBrandColor else Branding.brand.colorsJson.secondaryBrandColor
            binding.contentLinear.background =
                if (transactionWithMe) getThemableRadialGradient(binding.root.context)
                else AppCompatResources.getDrawable(binding.root.context, R.drawable.thanks_group)
            val textColor = if (transactionWithMe) Color.WHITE else Color.BLACK
            binding.card.setCardBackgroundColor(Color.parseColor(backColor))
            binding.text.setTextColor(textColor)
            binding.currencyIv.imageTintList =
                ColorStateList.valueOf(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
        }

        private fun handleСircle(item: EventDataModel, transactionWithMe: Boolean) {
            if (transactionWithMe) {
                setThanksAmount(item)
            } else {
                setImage(item)
            }
        }

        private fun setThanksAmount(item: EventDataModel) {
            binding.image.invisible()
            binding.amountView.visible()
            val amount = item.text.split("\\s+".toRegex()).getOrNull(2)?.toIntOrNull()
            binding.amount.text = amount.toString()
        }


        private fun setImage(item: EventDataModel) {
            binding.image.visible()
            binding.amountView.invisible()
            binding.image.setAvatarImageOrInitials(item.icon, item.textIcon)

        }

        fun processText(
            inputText: String,
            transactionWithMe: Boolean,
            context: Context
        ): CharSequence {
            if (transactionWithMe) {
                return context.getString(R.string.main_fragment_new_transaction)
            } else {
                val clearText =
                    HtmlCompat.fromHtml(inputText, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
                val name = clearText.take(4) + "..."
                return getFirstThreeWords(inputText.substringAfter("</a>"), name)

            }
        }

    }

    fun getFirstThreeWords(inputText: String, name: String): CharSequence {
        val words = inputText.split("\\s+".toRegex())
        val firstWord = words.take(2).joinToString(" ").trim()
        val otherWords = words.take(4).joinToString(" ").replace(firstWord, "").trim()
        val spannable = SpannableStringBuilder().append(
            name
        ).append(
            "${firstWord}\n"
        ).append(
            SpannableUtils.createClickableSpannable(
                otherWords,
                Branding.appTheme.mainBrandColor
            ) {}
        )
        return spannable

    }

    inner class BirthdayViewHolder(val binding: ItemMainScreenEventBirthdayBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EventDataModel) {
            binding.card.setOnClickListener {
                startActivityByMainLink(item.mainlink, binding.root.context)
            }
            binding.image.imageTintList =
                ColorStateList.valueOf(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
            binding.card.setCardBackgroundColor(Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor))
        }
    }

    private fun startActivityByMainLink(mainLink: String?, context: Context) {
        if (mainLink != null) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mainLink))
            ContextCompat.startActivity(context, intent, Bundle.EMPTY)
        }
    }

    private fun getThemableRadialGradient(context: Context): Drawable {
        val gradientDrawable = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.gradient_for_news_item,
            null
        ) as GradientDrawable
        gradientDrawable.apply {
            colors = intArrayOf(
                Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor),
                Color.parseColor(Branding.brand.colorsJson.mainBrandColor)
            )
        }
        return gradientDrawable
    }


    companion object {
        const val TAG = "EventsMainScreenAdapter"

        class DiffCallback : DiffUtil.ItemCallback<EventDataModel>() {
            override fun areItemsTheSame(
                oldItem: EventDataModel,
                newItem: EventDataModel,
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: EventDataModel,
                newItem: EventDataModel,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}

enum class ViewType(val value: String) {
    CHALLENGE("Q"),
    REPORT_OF_CHALLENGE("R"),
    TRANSACTION("T"),
    BIRTHDAY("B"),
}