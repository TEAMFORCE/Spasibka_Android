package com.teamforce.thanksapp.presentation.adapter.challenge

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ItemChallengeBinding
import com.teamforce.thanksapp.model.domain.ChallengeModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.branding.Branding


class ChallengePagerAdapter(

) : PagingDataAdapter<ChallengeModel, ChallengePagerAdapter.ChallengeViewHolder>(DiffCallback) {

    var onChallengeClicked: ((challengeId: Int, clickedView: View, titleView: View) -> Unit)? = null


    companion object {
        object DiffCallback : DiffUtil.ItemCallback<ChallengeModel>() {
            override fun areItemsTheSame(
                oldItem: ChallengeModel,
                newItem: ChallengeModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ChallengeModel,
                newItem: ChallengeModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val binding = ItemChallengeBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return ChallengeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
            holder.binding.mainCard.setOnClickListener {
                it.transitionName = it.context.getString(R.string.detail_challenge_image_transition, item.id)
                holder.binding.infoBlock.transitionName = it.context.getString(R.string.detail_challenge_title_transition, item.id)

                onChallengeClicked?.invoke(item.id, it, holder.binding.infoBlock)
            }
        }
    }

// TODO Проблема с фото в списке, фото сохраняется на новый элемент
    class ChallengeViewHolder(val binding: ItemChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ChallengeModel) {
            binding.topFrame.setBackgroundColor(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
            if (!data.photo.isNullOrEmpty()) {
                binding.imageBackground.visible()
                Glide.with(binding.root.context)
                    .load(data.photo.first().addBaseUrl())
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.imageBackground)
            } else {
                binding.imageBackground.invisible()
            }
            // insert data
            binding.apply {
                challengeTitle.text = data.name
                challengeCreator.text = String.format(
                    binding.root.context.getString(R.string.creatorOfChallenge),
                    data.creatorSurname,
                    data.creatorName
                )
                if (data.fromOrganization){
                    challengeCreator.text = String.format(
                        binding.root.context.getString(R.string.creatorOfChallenge),
                        data.organizationName,
                        ""
                    )
                }else{
                    challengeCreator.text = String.format(
                        binding.root.context.getString(R.string.creatorOfChallenge),
                        data.creatorSurname,
                        data.creatorName
                    )
                }
                winnersValue.text = data.winnersCount.toString()
                if (data.parameters?.get(0)?.id == 1) {
                    prizePoolValue.text = data.parameters[1].value.toString()
                } else {
                    data.parameters?.get(0)?.let { prizePoolValue.text = it.value.toString() }
                }
                awardValue.text = data.prizeSize.toString()
                val updateDate =
                    data.updatedAt?.let { parseDateTimeOutputOnlyDate(it, root.context) }

                binding.lastUpdateChallengeValue.text =
                    String.format(root.context.getString(R.string.lastUpdateChallenge), updateDate)
            }
            theming()
        }

        private fun theming() {
            binding.root.traverseViewsTheming()
        }

    }
}