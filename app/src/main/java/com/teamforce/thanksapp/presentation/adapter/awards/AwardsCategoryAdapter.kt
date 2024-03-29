package com.teamforce.thanksapp.presentation.adapter.awards

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ItemAwardBinding
import com.teamforce.thanksapp.databinding.ItemChapterAwardsBinding
import com.teamforce.thanksapp.domain.models.awards.AwardState
import com.teamforce.thanksapp.domain.models.awards.AwardsModel
import com.teamforce.thanksapp.domain.models.awards.CategoryAwardsModel
import com.teamforce.thanksapp.presentation.adapter.decorators.HorizontalDecoratorForLastItem
import com.teamforce.thanksapp.presentation.fragment.challenges.category.DisplayableItem
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.glide.animateToColor
import com.teamforce.thanksapp.utils.glide.setImage
import com.teamforce.thanksapp.utils.glide.setImageWithGraySale
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.visible
import kotlin.random.Random

class AwardsCategoryAdapter(
    private val onItemClick: (AwardsModel) -> Unit
) : ListAdapter<CategoryAwardsModel, AwardsCategoryAdapter.AwardsCategoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AwardsCategoryViewHolder {
        val binding = ItemChapterAwardsBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return AwardsCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AwardsCategoryViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) holder.bind(item, position)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    inner class AwardsCategoryViewHolder(val binding: ItemChapterAwardsBinding) :
        RecyclerView.ViewHolder(binding.root) {


        val listAdapter = ListDelegationAdapter(TaskAdapter(onItemClick))
        fun bind(item: CategoryAwardsModel, position: Int) {
            binding.titleTv.text = item.name
            setTaskAdapter(item.awards)

        }

        private fun setTaskAdapter(awards: List<AwardsModel>) {
            binding.awardsRv.apply {
                layoutManager =
                    LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
                adapter = listAdapter
                addItemDecoration(
                    HorizontalDecoratorForLastItem()
                )

            }
            listAdapter.items = awards
        }

    }

    private fun TaskAdapter(onItemClick: (AwardsModel) -> Unit) =
        adapterDelegateViewBinding<AwardsModel, DisplayableItem, ItemAwardBinding>(
            { layoutInflater, root -> ItemAwardBinding.inflate(layoutInflater, root, false) },
        ) {
            binding.root.setOnClickListener {
                onItemClick(item)
            }
            bind {
                Branding.traverseViews(binding.root)
                binding.amountLabelCard.visible()
                binding.receivedLabelIv.visible()
                when (item.state) {
                    AwardState.RECEIVED, AwardState.SET_IN_STATUS -> setReceivedAward(item, binding)
                    AwardState.CAN_BE_RECEIVED -> setAvailableAward(item, binding)
                    AwardState.NOT_AVAILABLE -> setNotAvailableAward(item, binding)
                }

            }


        }
    private fun setAvailableAward(item: AwardsModel, binding: ItemAwardBinding) {
        // Показать оранжевый текст Получить
        binding.actionText.text = binding.root.context.getString(R.string.awards_receive)
        binding.actionText.setTextColor(ColorStateList.valueOf(Color.parseColor(Branding.brand.colorsJson.mainBrandColor)))
        // Сделать иконку цветной
        binding.awardImageIv.setImage(item.cover, R.drawable.default_award)
        binding.awardImageIv.animateToColor()
        // Показать label с призом
        binding.awardAmountTv.text = item.reward.toString()

    }

    private fun setReceivedAward(item: AwardsModel, binding: ItemAwardBinding) {
        // Показать серый текст Получено
        binding.actionText.text = binding.root.context.getString(R.string.awards_received)
        binding.actionText.setTextColor(ColorStateList.valueOf(Color.parseColor(Branding.brand.colorsJson.generalContrastSecondaryColor)))
        // Сделать иконку цветной
        binding.awardImageIv.setImage(item.cover, R.drawable.default_award)
        binding.awardImageIv.animateToColor()
        // Показать label с галочкой
        binding.amountLabelCard.invisible()
    }

    private fun setNotAvailableAward(item: AwardsModel, binding: ItemAwardBinding) {
        // Показать текущий счет
        binding.actionText.setTextColor(Color.BLACK)
        binding.actionText.text = "${item.currentScores}/${item.targetScores}"
        // Сделать иконку серой
        binding.awardImageIv.setImageWithGraySale(item.cover, R.drawable.default_award)
        // Скрыть label
        binding.amountLabelCard.invisible()
        binding.receivedLabelIv.invisible()
    }


    companion object {

        object DiffCallback : DiffUtil.ItemCallback<CategoryAwardsModel>() {
            override fun areItemsTheSame(
                oldItem: CategoryAwardsModel,
                newItem: CategoryAwardsModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: CategoryAwardsModel,
                newItem: CategoryAwardsModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

}