package com.teamforce.thanksapp.presentation.adapter.challenge.challengeChains


import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ItemChallengeChainBinding
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ChainCondition
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ChallengeChainsModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.branding.Branding


class ChallengeChainsPagerAdapter(
) : PagingDataAdapter<ChallengeChainsModel, ChallengeChainsPagerAdapter.ChallengeChainViewHolder>(
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
        val binding = ItemChallengeChainBinding
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


    class ChallengeChainViewHolder(val binding: ItemChallengeChainBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ChallengeChainsModel) {
            if (data.photos.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load("${Consts.BASE_URL}${data.photos.first()}".toUri())
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
            } else {
                binding.imageBackground.setBackgroundColor(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
                binding.blurLinear.background = null
                binding.blurLinear.setBackgroundColor(Color.parseColor(Branding.brand.colorsJson.generalBackgroundColor))
            }
            // insert data
            binding.apply {
                challengeTitle.text = data.name
                challengeCreator.text = String.format(
                    binding.root.context.getString(R.string.creatorOfChallenge),
                    data.author,
                    ""
                )
                stateChallengeValue.text = getConditionOfChainReadable(data.currentState)
                val updateDate = parseDateTimeOutputOnlyDate(data.updatedAt, root.context)
                binding.lastUpdateChallengeValue.text =
                    String.format(root.context.getString(R.string.lastUpdateChallenge), updateDate)
            }
            setStatusPosition()
            setSlider(data)
            theming()
        }

        private fun setStatusPosition(){
            binding.root.post{
                val blurLinearHeight = binding.blurLinear.height
                val cardHeight = binding.mainCard.height
                val targetViewHeight = binding.statusRl.height
                val neededYPos = cardHeight - blurLinearHeight - (targetViewHeight / 2)
               // Log.e("ChallengeChainsPagerAdapter", "NeededYPos ${neededYPos}")

                val layoutParams = binding.statusRl.layoutParams as? ViewGroup.MarginLayoutParams
                if (layoutParams != null) {
                    layoutParams.topMargin = neededYPos // Установите нужное значение для вертикального смещения
                    binding.statusRl.layoutParams = layoutParams // Установите обновленные параметры макета обратно на View
                }
            }
        }

        private fun theming() {

            binding.slider.apply {
                trackActiveTintList =
                    ColorStateList.valueOf(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
                trackInactiveTintList =
                    ColorStateList.valueOf(Color.parseColor("#FBCDBF"))
                trackHeight = 12.dp
                this.isTickVisible = false
                setCustomThumbDrawable(
                    drawCircleThumb(
                        Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor),
                        AppCompatResources.getDrawable(
                            binding.root.context,
                            R.drawable.tag_icon_rocket
                        )
                    )
                )
            }
        }

        private fun drawCircleThumb(backgroundColor: Int, iconDrawable: Drawable?): Drawable {
            val shape = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
                setColor(backgroundColor)
            }

            iconDrawable?.let {
                val layers: Array<Drawable?> = arrayOf(shape, it)
                val layerDrawable = LayerDrawable(layers)
                val padding = 15
                layerDrawable.setLayerInset(
                    1,
                    padding,
                    padding,
                    padding,
                    padding
                )

                return layerDrawable
            }
            return shape
        }


        private fun setSlider(data: ChallengeChainsModel) {
            if (data.taskTotal > 0) {
                binding.slider.valueFrom = 0f
                binding.slider.valueTo = data.taskTotal.toFloat()
                binding.slider.value = data.tasksFinished.toFloat()
                binding.prizePoolAndWinnersSliderValue.text =
                    "${data.tasksFinished}/${data.taskTotal}"
                binding.sliderLinear.visible()
            } else {
                binding.sliderLinear.invisible()
            }
        }

        private fun getConditionOfChainReadable(chainCondition: ChainCondition): String {
            return when (chainCondition) {
                ChainCondition.ACTIVE -> {
                    binding.statusActiveCard
                        .setCardBackgroundColor(Color.parseColor(Branding.brand.colorsJson.minorSuccessColor))
                    binding.root.context.getString(R.string.active)
                }
                ChainCondition.COMPLETED -> {
                    binding.statusActiveCard
                        .setCardBackgroundColor(Color.parseColor(Branding.brand.colorsJson.generalContrastSecondaryColor))
                    binding.root.context.getString(R.string.completed)
                }
                ChainCondition.DEFERRED -> {
                    binding.statusActiveCard
                        .setCardBackgroundColor(Color.parseColor(Branding.brand.colorsJson.minorWarningColor))
                    binding.root.context.getString(R.string.list_challenges_fragment_deferred)
                }
            }
        }

    }
}