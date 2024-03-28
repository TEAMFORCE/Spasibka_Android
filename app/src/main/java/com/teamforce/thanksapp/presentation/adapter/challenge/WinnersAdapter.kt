package com.teamforce.thanksapp.presentation.adapter.challenge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.teamforce.thanksapp.data.response.GetChallengeWinnersResponse
import com.teamforce.thanksapp.databinding.ItemWinnerBinding
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.glide.setImage
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.parseDateTimeWithBindToTimeZone
import com.teamforce.thanksapp.utils.visible

class WinnersAdapter(
    private val allowLikeWinners: Boolean?,
    private val onLikeClicked: (reportId: Int, position: Int) -> Unit,
    private val onLikeLongClicked: (reportId: Int) -> Unit,
    private val onCommentClicked: (reportId: Int, position: Int) -> Unit
) : ListAdapter<GetChallengeWinnersResponse.Winner, WinnersAdapter.WinnerViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WinnerViewHolder {
        val binding = ItemWinnerBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return WinnerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun onBindViewHolder(holder: WinnerViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) holder.bind(item, position)
    }

    override fun onBindViewHolder(holder: WinnerViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = getItem(position)
        if (item != null) {
            when (val latestPayload = payloads.lastOrNull()) {
                is WinnersChangePayload.Likes -> holder.bindLikes(
                    likesAmount = latestPayload.newLikesCount,
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
            notifyItemChanged(position, WinnersChangePayload.Likes(newLikesCount, userLiked))
        }
    }


    companion object {

        object DiffCallback : DiffUtil.ItemCallback<GetChallengeWinnersResponse.Winner>() {
            override fun areItemsTheSame(
                oldItem: GetChallengeWinnersResponse.Winner,
                newItem: GetChallengeWinnersResponse.Winner
            ): Boolean {
                return oldItem.participant_id == newItem.participant_id
            }

            override fun areContentsTheSame(
                oldItem: GetChallengeWinnersResponse.Winner,
                newItem: GetChallengeWinnersResponse.Winner
            ): Boolean {
                return oldItem == newItem
            }

        }
    }


    inner class WinnerViewHolder(val binding: ItemWinnerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GetChallengeWinnersResponse.Winner, position: Int) {
            with(binding) {
                showReactionGroupOrNo(allowLikeWinners)
                bindLikes(item.likesAmount, item.userLiked)
                binding.dateTv.text = parseDateTimeWithBindToTimeZone(item.awarded_at, root.context)
                binding.reactionGroup.setCommentsAmount(item.commentsAmount)
                binding.textReportTv.text = item.text
                setUserName(item)
                setRepostImage(item)
                binding.userAvatar.setAvatarImageOrInitials(item.participant_photo, item.participant_surname + " " + item.participant_name)

                binding.reactionGroup.onLikeClicked = {
                    onLikeClicked(item.reportId, position)
                }
                binding.reactionGroup.onCommentClicked =  {
                    onCommentClicked(item.reportId, position)
                }
                binding.reactionGroup.onLongLikeClicked = {
                    onLikeLongClicked(item.reportId)
                }

            }

        }

        private fun setRepostImage(item: GetChallengeWinnersResponse.Winner){
            if(item.photo.isNullOrEmpty()){
                binding.imageReportSiv.invisible()
            }else{
                binding.imageReportSiv.visible()
                binding.imageReportSiv.setImage(item.photo)
            }
        }
        private fun setUserName(item: GetChallengeWinnersResponse.Winner){
            if(item.participant_name.isNullOrEmpty() && item.participant_surname.isNullOrEmpty()){
                binding.userName.text = item.nickname
            }else{
                binding.userName.text = "${item.participant_name} ${item.participant_surname}"
            }
        }

        private fun showReactionGroupOrNo(allowLikeWinners: Boolean?){
            if (allowLikeWinners == true) binding.reactionGroup
            else binding.reactionGroup
        }

        internal fun bindLikes(likesAmount: Int, userLiked: Boolean) {
            binding.reactionGroup.setLikesAmount(likesAmount)
            binding.reactionGroup.setIsLiked(userLiked)
        }

    }
}

private sealed interface WinnersChangePayload {
    data class Likes(val newLikesCount: Int, val userLiked: Boolean) : WinnersChangePayload

}