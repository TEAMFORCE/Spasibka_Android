package com.teamforce.thanksapp.presentation.adapter.challenge.challengeChains

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ItemChallengeChainMiniBinding
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ChallengeChainsModel
import com.teamforce.thanksapp.utils.addBaseUrl
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.fastBlur


class ChainMiniAdapter(
) : ListAdapter<ChallengeChainsModel, ChainMiniAdapter.ChallengeChainViewHolder>(
    DiffCallback
) {

    var onChallengeChainClicked: ((challengeChainId: Long, clickedView: View) -> Unit)? = null


    companion object {
        object DiffCallback : DiffUtil.ItemCallback<ChallengeChainsModel>() {
            override fun areItemsTheSame(
                oldItem: ChallengeChainsModel,
                newItem: ChallengeChainsModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ChallengeChainsModel,
                newItem: ChallengeChainsModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeChainViewHolder {
        val binding = ItemChallengeChainMiniBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return ChallengeChainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChallengeChainViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
            holder.binding.mainCard.setOnClickListener {
                it.transitionName = item.id.toString()
                onChallengeChainClicked?.invoke(item.id, it)
            }
        }
    }


    class ChallengeChainViewHolder(val binding: ItemChallengeChainMiniBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ChallengeChainsModel) {
            setData(data)
            if (data.photos.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(data.photos.first().addBaseUrl())
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable?>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            // Вернуть false, чтобы Glide продолжил обработку ошибки.
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable?>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            val bmp = (resource as? BitmapDrawable)?.bitmap
                            if (bmp != null) {
                                binding.blurLinear.fastBlur(bmp)
                            }
                            return false
                        }


                    })
                    .into(binding.imageBackground)
            }else{
                binding.imageBackground.setBackgroundColor(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
                binding.blurLinear.background = null
                binding.blurLinear.setBackgroundColor(Color.parseColor(Branding.brand.colorsJson.generalBackgroundColor))
            }
            theming()
        }

        private fun setData(data: ChallengeChainsModel){
            // insert data
            binding.apply {
                chainTitle.text = data.name
                chainCreator.text = String.format(
                    binding.root.context.getString(R.string.creatorOfChallenge),
                    data.author,
                    ""
                )
            }
        }

        private fun theming() {
        }


    }
}