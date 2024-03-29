package com.teamforce.thanksapp.presentation.adapter.challenge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.teamforce.thanksapp.databinding.ItemContenderBinding
import com.teamforce.thanksapp.domain.models.challenge.ChallengeType
import com.teamforce.thanksapp.domain.models.challenge.ContenderModel
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.Extensions.enableOnClickableLinks
import com.teamforce.thanksapp.utils.Extensions.viewSinglePhoto
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.glide.setImage
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.parseDateTimeWithBindToTimeZone
import com.teamforce.thanksapp.utils.visible

class ContendersAdapter(
    private val challengeActive: Boolean,
    private val typeOfChallenge: ChallengeType,
    private val onContendersClicked: (reportId: Int) -> Unit,
    private val onCommentClicked: (reportId: Int, position: Int) -> Unit,
    private val applyClickListener: (reportId: Int, state: Char) -> Unit,
    private val refuseClickListener: (reportId: Int, state: Char) -> Unit,
    private val onLikeClicked: (reportId: Int, position: Int) -> Unit,
    private val onLongLikeClicked: (reportId: Int) -> Unit,
    private val showContenders: Boolean
) : ListAdapter<ContenderModel, ContendersAdapter.ContenderViewHolder>(
    DiffCallback
) {
    var onImageClicked: ((view: View, photo: String) -> Unit)? = null

    fun updateLikesCount(position: Int?, newLikesCount: Int, userLiked: Boolean) {
        if (position != null) {
            getItem(position)?.likesAmount = newLikesCount
            getItem(position)?.userLiked = userLiked
            notifyItemChanged(position, ContendersChangePayload.Likes(newLikesCount, userLiked))
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContenderViewHolder {
        val binding = ItemContenderBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return ContenderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContenderViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) holder.bind(item, position)
    }

    override fun onBindViewHolder(holder: ContenderViewHolder, position: Int,  payloads: MutableList<Any>) {
        val item = getItem(position)
        if (item != null) {
            when (val latestPayload = payloads.lastOrNull()) {
                is ContendersChangePayload.Likes -> holder.bindLikes(
                    likesAmount = latestPayload.newLikesCount,
                    userLiked = latestPayload.userLiked
                )

                else -> onBindViewHolder(holder, position)
            }
        }
    }


    override fun getItemCount(): Int {
        return currentList.size
    }

    companion object {
        const val TAG = "ContendersAdapter"

        object DiffCallback : DiffUtil.ItemCallback<ContenderModel>() {
            override fun areItemsTheSame(
                oldItem: ContenderModel,
                newItem: ContenderModel
            ): Boolean {
                return oldItem.reportId == newItem.reportId
            }

            override fun areContentsTheSame(
                oldItem: ContenderModel,
                newItem: ContenderModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }


    inner class ContenderViewHolder(val binding: ItemContenderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContenderModel, position: Int) {
            with(binding) {
                binding.userAvatar.setAvatarImageOrInitials(item.participantPhoto, "${item.participantName} ${item.participantSurname}")

                if (!item.reportPhoto.isNullOrEmpty()) {
                    binding.imageReportSiv.visible()
                    binding.imageReportSiv.setImage(item.reportPhoto)
                    binding.imageReportSiv.setOnClickListener { clickedView ->
                        item.reportPhoto.let {
                            onImageClicked?.invoke(
                                clickedView,
                                it
                            )
                        }
                    }
                } else {
                    binding.imageReportSiv.invisible()
                }
                bindLikes(item.likesAmount, item.userLiked)
                binding.reactionGroup.setCommentsAmount(item.commentsAmount)
                binding.textReportTv.enableOnClickableLinks()
                binding.textReportTv.text = item.reportText
                binding.userName.text = "${item.participantName} ${item.participantSurname}"
                binding.dateTv.text = parseDateTimeWithBindToTimeZone(item.reportCreatedAt, root.context)
                binding.applyBtn.setOnClickListener {
                    applyClickListener.invoke(item.reportId, 'W')
                }
                binding.refuseBtn.setOnClickListener {
                    refuseClickListener.invoke(item.reportId, 'D')
                }
                binding.userAvatar.setOnClickListener { view ->
                    item.participantPhoto?.let { photo ->
                        (view as AvatarView).viewSinglePhoto(photo, binding.root.context)
                    }
                }
                binding.reactionGroup.onLikeClicked =  {
                    onLikeClicked(item.reportId, position)
                }

                binding.userItem.setOnClickListener {
                    onContendersClicked(item.reportId)
                }
            }

            binding.reactionGroup.onCommentClicked = {
                onCommentClicked(item.reportId, layoutPosition)
            }
            binding.reactionGroup.onLongLikeClicked = {
                if(item.likesAmount > 0){
                    onLongLikeClicked(item.reportId)
                }
            }

            showResponseGroupOrNo(item)
            showReactionsGroupOrNo(item)
            Branding.traverseViews(binding.root)
        }

        private fun showReactionsGroupOrNo(item: ContenderModel){
            if(typeOfChallenge == ChallengeType.VOTING ||
                typeOfChallenge == ChallengeType.DEFAULT && showContenders){
                binding.reactionGroup.visible()
            }else{
                binding.reactionGroup.invisible()

            }
        }

        private fun showResponseGroupOrNo(item: ContenderModel){
            if(item.canApprove && challengeActive){
                binding.responseLinear.visible()
            }else{
                binding.responseLinear.invisible()
            }
        }

        internal fun bindLikes(likesAmount: Int, userLiked: Boolean) {
            binding.reactionGroup.setLikesAmount(likesAmount)
            binding.reactionGroup.setIsLiked(userLiked)
        }


    }

}

private sealed interface ContendersChangePayload {
    data class Likes(val newLikesCount: Int, val userLiked: Boolean) : ContendersChangePayload

}