package com.teamforce.thanksapp.presentation.adapter.benefitCafe.reviews

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.net.toUri
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ItemReviewBinding
import com.teamforce.thanksapp.domain.models.benefit.ReviewModel
import com.teamforce.thanksapp.domain.models.history.HistoryItemModel
import com.teamforce.thanksapp.model.domain.CommentModel
import com.teamforce.thanksapp.presentation.adapter.GeneralViewImageAdapter
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.presentation.customViews.autoFitGridLayoutManager.AutoFitGridLayoutManager
import com.teamforce.thanksapp.utils.addBaseUrl
import com.teamforce.thanksapp.utils.dp
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.parseDateTimeWithBindToTimeZone
import com.teamforce.thanksapp.utils.visible


class ReviewAdapter(
    private val amIAdmin: Boolean,
    private val onDeleteClicked: (id: Long, position: Int) -> Unit,
    private val onLikeClicked: (id: Long, position: Int) -> Unit,
    private val onLikeLongClicked: (id: Long) -> Unit,
    private val onImageClicked: (listOfPhotos: List<String?>, clickedView: View, position: Int) -> Unit
) : PagingDataAdapter<ReviewModel, ReviewAdapter.ViewHolder>(DiffCallback) {

    override fun onBindViewHolder(
        holder: ReviewAdapter.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val item = getItem(position)
        if (item != null) {
            when (val latestPayload = payloads.lastOrNull()) {
                is ReviewChangePayload.Likes -> holder.bindLikes(
                    likesCount = latestPayload.newLikesCount,
                    userLiked = latestPayload.userLiked
                )

                else -> onBindViewHolder(holder, position)
            }
        }
    }

    override fun onBindViewHolder(holder: ReviewAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item, position)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReviewAdapter.ViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ReviewModel>() {
            override fun areItemsTheSame(
                oldItem: ReviewModel,
                newItem: ReviewModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ReviewModel,
                newItem: ReviewModel
            ): Boolean {
                return oldItem == newItem
            }

            override fun getChangePayload(oldItem: ReviewModel, newItem: ReviewModel): Any? {
                return when {
                    oldItem.likesAmount != newItem.likesAmount -> {
                        ReviewChangePayload.Likes(newItem.likesAmount, newItem.isLiked)
                    }

                    else -> super.getChangePayload(oldItem, newItem)
                }
            }
        }
    }

    fun updateLikesCount(position: Int?, newLikesCount: Int, userLiked: Boolean) {
        if (position != null && this.itemCount != 0 && this.itemCount >= position) {
            getItem(position)?.likesAmount = newLikesCount
            getItem(position)?.isLiked = userLiked
            notifyItemChanged(position, ReviewChangePayload.Likes(newLikesCount, userLiked))
        }
    }

    fun deleteElement(position: Int) {
        notifyItemRemoved(position)
    }

    inner class ViewHolder(val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReviewModel, position: Int) {
            binding.userNameLabelTv.text = item.authorName
            binding.reviewTv.text = item.text
            binding.ratingBar.rating = item.rate.toFloat()
            binding.dateTime.text =
                parseDateTimeWithBindToTimeZone(item.createdAt, binding.root.context)
            setAvatar(item)
            handleMenu(item, position)
            initListOfPhotos(item)
            bindLikes(likesCount = item.likesAmount, userLiked = item.isLiked)
            binding.reactionGroup.onLikeClicked = {
                if (!item.isMyReview) onLikeClicked.invoke(item.id, position)
                else Toast.makeText(
                    binding.root.context,
                    binding.root.context.getString(R.string.reviews_check_cant_like_your_review),
                    Toast.LENGTH_SHORT
                ).show()
            }
            binding.reactionGroup.onLongLikeClicked = {
                if(item.likesAmount > 0) onLikeLongClicked.invoke(item.id)
            }
        }

        internal fun bindLikes(
            likesCount: Int,
            userLiked: Boolean,
        ) {
            binding.reactionGroup.setIsLiked(userLiked)
            binding.reactionGroup.setLikesAmount(likesCount)
        }

        private fun initListOfPhotos(item: ReviewModel) {
            var listAdapter: GeneralViewImageAdapter? = null
            if (item.photos.isNotEmpty()) {
                listAdapter = GeneralViewImageAdapter(photos = item.photos, onImageClicked)
            } else {
                binding.imageList.invisible()
            }

            binding.imageList.adapter = listAdapter
            binding.imageList.layoutManager = AutoFitGridLayoutManager(binding.root.context, 100.dp)

        }

        private fun handleMenu(item: ReviewModel, position: Int) {
            if (item.isMyReview || amIAdmin) {
                binding.ivMore.visible()
                binding.ivMore.setOnClickListener {
                    showPopupMenu(item, position)
                }
            } else {
                binding.ivMore.invisible()
            }
        }

        private fun showPopupMenu(data: ReviewModel, position: Int) {
            val popup = PopupMenu(
                binding.root.context,
                binding.dateTime,
                Gravity.START,
                0,
                R.style.PopupMenuStyle
            )
            popup.menuInflater.inflate(R.menu.comment_context_menu, popup.menu)

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.delete -> {
                        onDeleteClicked(data.id, position)
                    }
                }
                true
            }
            popup.show()
        }

        private fun setAvatar(data: ReviewModel) {
            binding.userAvatar.setAvatarImageOrInitials(data.authorPhoto, data.authorName)
        }


    }
}

private sealed interface ReviewChangePayload {
    data class Likes(val newLikesCount: Int, val userLiked: Boolean) : ReviewChangePayload

}