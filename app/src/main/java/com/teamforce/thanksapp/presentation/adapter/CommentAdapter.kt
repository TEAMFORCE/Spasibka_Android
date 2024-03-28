package com.teamforce.thanksapp.presentation.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.net.toUri
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ItemCommentBinding
import com.teamforce.thanksapp.model.domain.CommentModel
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.Extensions.enableOnClickableLinks
import com.teamforce.thanksapp.utils.Extensions.viewSingleGif
import com.teamforce.thanksapp.utils.Extensions.viewSinglePhoto
import com.teamforce.thanksapp.utils.glide.setGif
import com.teamforce.thanksapp.utils.glide.setImage
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.isNullOrEmptyMy
import com.teamforce.thanksapp.utils.parseDateTimeWithBindToTimeZone
import com.teamforce.thanksapp.utils.visible

class CommentAdapter(
    private val profileId: String?,
    private val onDeleteLongClicked: (id: Int) -> Unit,
    private val onLikeClicked: (id: Int, position: Int) -> Unit,
    private val onLikeLongClicked: (id: Int) -> Unit,
    ) : PagingDataAdapter<CommentModel, CommentAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.ViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {

            holder.bind(item, profileId, position, onDeleteLongClicked, onLikeClicked)
        }
    }

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int,  payloads: MutableList<Any>) {
        val item = getItem(position)
        if (item != null) {
            when(val latestPayload = payloads.lastOrNull()){
                is CommentsChangePayload.Likes -> holder.bindLikes(
                    likesCount = latestPayload.newLikesCount,
                    userLiked = latestPayload.userLiked
                )
                else -> onBindViewHolder(holder, position)
            }
        }
    }


    fun updateLikesCount(position: Int?, newLikesCount: Int, userLiked: Boolean) {
        if (position != null) {
            getItem(position)?.likesAmount = newLikesCount
            getItem(position)?.userLiked = userLiked
            notifyItemChanged(position, CommentsChangePayload.Likes(newLikesCount, userLiked))
        }
    }


    inner class ViewHolder(
        private val binding: ItemCommentBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            data: CommentModel, profileId: String?,
            position: Int,
            onDeleteLongClicked: (id: Int) -> Unit,
            onLikeClicked: (id: Int, position: Int) -> Unit
        ) {
            bindAvatar(data)
            bindBaseInfo(data)
            bindGif(data)
            bindLikes(likesCount = data.likesAmount, userLiked = data.userLiked)
            binding.reactionGroup.onLikeClicked = {
                onLikeClicked.invoke(data.id, position)
            }
            binding.reactionGroup.onLongLikeClicked = {
                onLikeLongClicked.invoke(data.id)
            }
            binding.dateTime.text =
                parseDateTimeWithBindToTimeZone(data.created, binding.root.context)
            if (profileId == data.user.id.toString()) {
                binding.mainCardView.setOnLongClickListener {
                    showPopupMenu(data, onDeleteLongClicked)
                    return@setOnLongClickListener true
                }
            }
        }

        private fun showPopupMenu(data: CommentModel, onDeleteLongClicked: (id: Int) -> Unit) {
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
                        onDeleteLongClicked(data.id)
                    }
                }
                true
            }
            popup.show()
        }

        internal fun bindLikes(
            likesCount: Int,
            userLiked: Boolean,
        ) {
            binding.reactionGroup.setIsLiked(userLiked)
            binding.reactionGroup.setLikesAmount(likesCount)
        }


        private fun bindBaseInfo(data: CommentModel) {
            binding.message.enableOnClickableLinks()
            binding.fioSender.text =
                String.format(
                    binding.root.context.getString(R.string.fioSender),
                    data.user.surname, data.user.name
                )

            if (data.text.isEmpty()) {
                binding.message.invisible()
                binding.gifCard.visible()
            } else {
                binding.message.visible()
                binding.gifCard.invisible()
                binding.message.text = data.text
            }
        }

        private fun bindGif(data: CommentModel) {
            if (!data.picture.isNullOrEmptyMy()) {
                binding.gifImageView.setImage(data.picture!!)
                binding.gifImageView.setOnClickListener { imageView ->
                    (imageView as ImageView).viewSinglePhoto(data.picture, binding.root.context)
                }
            } else if (!data.gif.isNullOrEmptyMy()) {
                binding.gifImageView.setGif(data.gif!!)
                binding.gifImageView.setOnClickListener { imageView ->
                    (imageView as ImageView).viewSingleGif(data.gif, binding.root.context)
                }
            }
        }

        private fun bindAvatar(data: CommentModel) {
            binding.userAvatar.setAvatarImageOrInitials(data.user.avatar, "${data.user.name} ${data.user.surname}")
        }

    }


    companion object {
        const val TAG = "CommentAdapter"

        private val DiffCallback = object : DiffUtil.ItemCallback<CommentModel>() {
            override fun areItemsTheSame(
                oldItem: CommentModel,
                newItem: CommentModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: CommentModel,
                newItem: CommentModel
            ): Boolean {
                return oldItem == newItem
            }

            override fun getChangePayload(oldItem: CommentModel, newItem: CommentModel): Any? {
                return when {
                    oldItem.likesAmount != newItem.likesAmount -> {
                        CommentsChangePayload.Likes(newItem.likesAmount, newItem.userLiked)
                    }
                    else -> super.getChangePayload(oldItem, newItem)
                }
            }

        }
    }
}

private sealed interface CommentsChangePayload {
    data class Likes(val newLikesCount: Int, val userLiked: Boolean) : CommentsChangePayload

}