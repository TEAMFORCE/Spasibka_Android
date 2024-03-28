package com.teamforce.thanksapp.presentation.adapter.feed

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.chip.ChipGroup
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ItemFeedBinding
import com.teamforce.thanksapp.domain.models.challenge.SectionsOfChallengeType
import com.teamforce.thanksapp.domain.models.events.EventDataModel
import com.teamforce.thanksapp.domain.models.events.TagEventModel
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.utils.Extensions.paintLinks
import com.teamforce.thanksapp.utils.addBaseUrl
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.parseDateTimeWithBindToTimeZone
import com.teamforce.thanksapp.utils.visible

class EventsAdapter: PagingDataAdapter<EventDataModel, EventsAdapter.ViewHolder>(DiffCallback()) {

    var onTransactionClicked: ((transactionId: Int, message: String) -> Unit)? = null
    var onLikeClicked: ((objectId: Int, typeOfObject: ObjectsToLike, position: Int) -> Unit)? = null
    var onLikeLongClicked: ((itemId: Int, objectType: ObjectsToLike) -> Unit)? = null
    var onCommentClicked: ((transactionId: Int, message: CharSequence) -> Unit)? = null

    var onCelebrateSomeone: ((userId: Int) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item, position)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = getItem(position)
        if (item != null) {
            when (val latestPayload = payloads.lastOrNull()) {
                is EventChangePayload.Likes -> holder.bindLikes(
                    likesCount = latestPayload.newLikesCount,
                    userLiked = latestPayload.userLiked
                )

                else -> onBindViewHolder(holder, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    fun updateLikesCount(position: Int?, newLikesCount: Int, userLiked: Boolean) {
        if (position != null) {
            getItem(position)?.likesAmount = newLikesCount
            getItem(position)?.userLiked = userLiked
            notifyItemChanged(position, EventChangePayload.Likes(newLikesCount, userLiked))
        }
    }

    inner class ViewHolder(val binding: ItemFeedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EventDataModel, position: Int) {
            with(binding) {
                setTags(binding.chipGroup, item.tags)
                toggleLikesAndCommentsVisibility(item)
                binding.tvTitle.text = item.header
                // Без этого клики по spannable не работают
                senderAndReceiver.movementMethod = LinkMovementMethod.getInstance()
                dateTime.text = parseDateTimeWithBindToTimeZone(item.time, binding.root.context)
                setImage(item)
                senderAndReceiver.text =
                    HtmlCompat.fromHtml(item.text, HtmlCompat.FROM_HTML_MODE_COMPACT)
                senderAndReceiver.paintLinks(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))

                bindLikes(item.likesAmount, item.userLiked)
                setComment(item.commentsAmount)
                cardMain.setOnClickListener {
                    when (item.typeOfObject) {
                        ObjectsToLike.TRANSACTION -> {
                            onTransactionClicked?.invoke(item.id, item.text)
                        }

                        ObjectsToLike.OFFER, ObjectsToLike.PURCHASE -> {}
                        ObjectsToLike.REPORT_OF_CHALLENGE -> {
                            startActivityByMainLink(item.mainlink + "/${SectionsOfChallengeType.WINNERS}")
                        }
                        else -> {
                            startActivityByMainLink(item.mainlink)
                        }
                    }
                }
                binding.reactionGroup.onLikeClicked = {
                    if (item.typeOfObject != null) {
                        onLikeClicked?.invoke(item.id, item.typeOfObject, position)
                    }
                }
                binding.reactionGroup.onCommentClicked = {
                    if (item.typeOfObject == ObjectsToLike.TRANSACTION) {
                        onCommentClicked?.invoke(item.id, senderAndReceiver.text)
                    } else if (item.typeOfObject == ObjectsToLike.CHALLENGE || item.typeOfObject == ObjectsToLike.REPORT_OF_CHALLENGE) {
                        startActivityByMainLink(item.mainlink + "/${SectionsOfChallengeType.COMMENTS.name}")
                    }
                }
                binding.reactionGroup.onLongLikeClicked = {
                    if(item.typeOfObject == ObjectsToLike.CHALLENGE && item.likesAmount > 0){
                        startActivityByMainLink(item.mainlink + "/${SectionsOfChallengeType.REACTIONS.name}")
                    }else if(item.typeOfObject != null  && item.likesAmount > 0){
                        onLikeLongClicked?.invoke(item.id, item.typeOfObject)
                    }
                }

                setCelebrateBtn(item)
            }
        }

        private fun setCelebrateBtn(item: EventDataModel){
            if(item.typeOfObject == ObjectsToLike.BIRTHDAY){
                binding.celebrateBtn.visible()
                binding.reactionGroup.invisible()
            }else{
                binding.reactionGroup.visible()
                binding.celebrateBtn.invisible()
            }
            binding.celebrateBtn.setOnClickListener {
                onCelebrateSomeone?.invoke(item.id)
            }

            binding.celebrateBtn.setCardBackgroundColor(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
        }

        private fun setImage(item: EventDataModel){
            binding.userAvatar.visible()
            binding.iconSiv.invisible()
            binding.userAvatar.setAvatarImageOrInitials(item.icon, item.textIcon)
            setSpecialIcon(item)
        }

        private fun setSpecialIcon(item: EventDataModel){
            val icon = when(item.typeOfObject) {
                ObjectsToLike.CHALLENGE -> R.drawable.ic_challenge_badge
                else -> {
                    return
                }
            }
                setIconItem(icon)
            }

        private fun setIconItem(@DrawableRes resId: Int) {
            binding.userAvatar.invisible()
            binding.iconSiv.visible()
            binding.iconSiv.setImageResource(resId)
            binding.iconSiv.setThemeColor(Branding.appTheme)
        }

        private fun setTags(tagsChipGroup: ChipGroup, tagList: List<TagEventModel>?) {
            tagsChipGroup.removeAllViews()
            tagList?.let {
                for (i in tagList.indices) {
                    val tagName = tagList[i].name
                    val tvTag: TextView = LayoutInflater.from(tagsChipGroup.context)
                        .inflate(
                            R.layout.text_view_tag_event,
                            tagsChipGroup,
                            false
                        ) as TextView
                    with(tvTag) {
                        text = String.format(context.getString(R.string.item_feed_tag), tagName)
                        setTextColor(Color.parseColor(Branding.brand.colorsJson.minorInfoColor))
                    }
                    tagsChipGroup.addView(tvTag)
                }
            }
        }

        private fun toggleLikesAndCommentsVisibility(item: EventDataModel) {
            if(item.canBeReacted) {
                binding.reactionGroup.visible()
            }else{
                binding.reactionGroup.invisible()
            }
        }

        private fun startActivityByMainLink(mainLink: String?) {
            if (mainLink != null) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mainLink))
                startActivity(binding.root.context, intent, Bundle.EMPTY)
            }
        }

        internal fun bindLikes(
            likesCount: Int,
            userLiked: Boolean,
        ) {
            binding.reactionGroup.setIsLiked(userLiked)
            binding.reactionGroup.setLikesAmount(likesCount)
        }

        private fun setComment(commentsAmount: Int) {
            binding.reactionGroup.setCommentsAmount(commentsAmount)
        }

    }

    companion object {
        const val TAG = "NewFeedAdapter"

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

            override fun getChangePayload(oldItem: EventDataModel, newItem: EventDataModel): Any? {
                return null
            }
        }
    }
}

private sealed interface EventChangePayload {
    data class Likes(val newLikesCount: Int, val userLiked: Boolean) : EventChangePayload

}