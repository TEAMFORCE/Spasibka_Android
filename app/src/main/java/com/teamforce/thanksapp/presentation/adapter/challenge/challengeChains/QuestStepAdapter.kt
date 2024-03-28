package com.teamforce.thanksapp.presentation.adapter.challenge.challengeChains

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ItemQuestStepBinding
import com.teamforce.thanksapp.databinding.ItemTaskInQuestStepBinding
import com.teamforce.thanksapp.domain.models.challenge.ChallengeCondition
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.StepModel
import com.teamforce.thanksapp.presentation.adapter.decorators.GridSpacingItemDecoration
import com.teamforce.thanksapp.presentation.fragment.challenges.category.DisplayableItem
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.branding.Branding


class QuestStepAdapter(
    private val onItemClick: (StepModel.Task) -> Unit
) : ListAdapter<StepModel, QuestStepAdapter.QuestStepViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestStepViewHolder {
        val binding = ItemQuestStepBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return QuestStepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestStepViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) holder.bind(item, position)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    inner class QuestStepViewHolder(val binding: ItemQuestStepBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val fallUpAnim: Animation = AnimationUtils.loadAnimation(binding.root.context, R.anim.fall_up)

        val listAdapter = ListDelegationAdapter(TaskAdapter(onItemClick))
        fun bind(item: StepModel, position: Int) {
            binding.titleTv.text = "${binding.root.context.getString(R.string.challenge_chain_step)} ${item.step}"
            binding.expandIv.setOnClickListener {
                val targetItem = getItem(position)
                if(targetItem.isOpen) {
                    binding.expandIv.rotateAnim(180f, 0f, 300)
                    binding.stepsRv.startAnimation(fallUpAnim)
                    notifyItemChanged(position)
                }else{
                    binding.expandIv.rotateAnim(0f, 180f, 300 )
                    binding.stepsRv.visible()
                    binding.stepsRv.scheduleLayoutAnimation()
                    setTaskAdapter(item.tasks)
                }
                targetItem.isOpen = !targetItem.isOpen
            }
            if(item.isOpen){
                binding.expandIv.rotateAnim(0f, 180f, 300 )
                binding.stepsRv.visible()
                binding.stepsRv.scheduleLayoutAnimation()
                setTaskAdapter(item.tasks)
            }else{
                binding.expandIv.rotateAnim(180f, 0f, 300)
                binding.stepsRv.invisible()
            }

        }

        private fun setTaskAdapter(tasks: List<StepModel.Task>){
            binding.stepsRv.apply {
                layoutManager = GridLayoutManager(binding.root.context, 2)
                adapter = listAdapter
                if (itemDecorationCount > 0){
                    removeItemDecorationAt(0)
                }
                addItemDecoration(GridSpacingItemDecoration(2, 40, false))
            }
            listAdapter.items = tasks
        }

    }

    private fun TaskAdapter(onItemClick: (StepModel.Task) -> Unit) =
        adapterDelegateViewBinding<StepModel.Task, DisplayableItem, ItemTaskInQuestStepBinding>(
            { layoutInflater, root -> ItemTaskInQuestStepBinding.inflate(layoutInflater, root, false) },
        ) {
            binding.mainCard.setOnClickListener {
                onItemClick(item)
            }
            bind {
                if (!item.image.isNullOrEmpty()) {
                    binding.imageBackground.visible()
                    Glide.with(binding.root.context)
                        .load("${Consts.BASE_URL}${item.image!!}".toUri())
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.imageBackground)
                } else {
                    binding.imageBackground.invisible()
                }
                binding.topFrame.setBackgroundColor(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
                binding.challengeTitle.text = item.name
                binding.challengeCreator.text = item.creatorName
                binding.rewardAmountTv.text = item.reward.toString()
                setStatus(item.status, binding)
                binding.root.traverseViewsTheming()

            }


        }
    private fun setStatus(challengeCondition: ChallengeCondition, binding: ItemTaskInQuestStepBinding){
        return when (challengeCondition) {
            ChallengeCondition.ACTIVE -> {
                binding.statusActiveCard
                    .setCardBackgroundColor(Color.parseColor(Branding.brand.colorsJson.minorSuccessColor))
                binding.statusTaskTv.text =  binding.root.context.getString(R.string.active)
            }
            ChallengeCondition.FINISHED -> {
                binding.statusActiveCard
                    .setCardBackgroundColor(Color.parseColor(Branding.brand.colorsJson.generalContrastSecondaryColor))
                binding.statusTaskTv.text = binding.root.context.getString(R.string.completed)
            }
            ChallengeCondition.DEFERRED -> {
                binding.statusActiveCard
                    .setCardBackgroundColor(Color.parseColor(Branding.brand.colorsJson.minorWarningColor))
                binding.statusTaskTv.text = binding.root.context.getString(R.string.list_challenges_fragment_deferred)
            }
        }
    }

    companion object {

        object DiffCallback : DiffUtil.ItemCallback<StepModel>() {
            override fun areItemsTheSame(
                oldItem: StepModel,
                newItem: StepModel
            ): Boolean {
                return oldItem.step == newItem.step
            }

            override fun areContentsTheSame(
                oldItem: StepModel,
                newItem: StepModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

}