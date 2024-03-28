package com.teamforce.thanksapp.presentation.adapter.templates

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.children
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.data.entities.challenges.SectionsData
import com.teamforce.thanksapp.databinding.ItemTemplateBinding
import com.teamforce.thanksapp.databinding.ItemTemplateWithBackgroundImageBinding
import com.teamforce.thanksapp.domain.models.templates.TemplateForBundleModel
import com.teamforce.thanksapp.domain.models.templates.TemplateModel
import com.teamforce.thanksapp.presentation.fragment.challenges.category.CategoryItem
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.glide.setImage
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.isNullOrEmptyMy
import com.teamforce.thanksapp.utils.visible

class TemplatesPagerAdapter() :
    PagingDataAdapter<TemplateModel, RecyclerView.ViewHolder>(DiffCallback) {

    var onTemplateClicked: ((data: TemplateForBundleModel) -> Unit)? = null

    var onTemplateEditClicked: ((data: TemplateForBundleModel) -> Unit)? = null

    companion object {
        object DiffCallback : DiffUtil.ItemCallback<TemplateModel>() {
            override fun areItemsTheSame(
                oldItem: TemplateModel,
                newItem: TemplateModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: TemplateModel,
                newItem: TemplateModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_template -> {
                val binding = ItemTemplateBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                TemplateViewHolder(binding)
            }
            else -> {
                val binding = ItemTemplateWithBackgroundImageBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                TemplateWithBackImageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            when (holder) {
                is TemplateViewHolder -> {
                    holder.bind(item)
                }
                is TemplateWithBackImageViewHolder -> {
                    holder.bind(item)
                }

            }
        }
    }


    inner class TemplateViewHolder(val binding: ItemTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: TemplateModel) {
            val templateForBundle = TemplateForBundleModel(
                id = data.id, title = data.name,
                description = data.description,
                photos = data.photos,
                challengeWithVoting = data.challengeType,
                multipleReports = data.multipleReports,
                showContenders = data.showContenders,
                sections = data.sections,
                scopeOfTemplates = data.scope
            )
            binding.title.text = data.name
            binding.description.text = data.description
            binding.mainCard.setOnClickListener {
                onTemplateClicked?.invoke(
                    templateForBundle
                )
            }
            if (data.editableByMe) {
                binding.editCard.visible()
                binding.editCard.setOnClickListener {
                    onTemplateEditClicked?.invoke(templateForBundle)
                }
            } else {
                binding.editCard.invisible()
                binding.editCard.setOnClickListener { }
            }
            data.sections?.let {
                addSections(binding.chipGroup, it, true)
            }
            binding.editIv.setColorFilter(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
            binding.shareIv.setColorFilter(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
            binding.title.setTextColor(ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.generalContrastColor)))
            binding.description.setTextColor(ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.generalContrastColor)))
            binding.mainCard.setCardBackgroundColor(Color.parseColor(Branding.brand?.colorsJson?.secondaryBrandColor))
            binding.successfulPersonImage.setColorFilter(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
        }

    }

    internal fun addSections(
        chipGroup: ChipGroup,
        listOfCategories: List<CategoryItem>,
        transparentChip: Boolean
    ) {
        chipGroup.removeAllViews()
        if(listOfCategories.isNotEmpty()){
            for (i in listOfCategories.indices) {
                val chip: Chip = LayoutInflater.from(chipGroup.context)
                    .inflate(
                        R.layout.chip_tag_in_template_list,
                        chipGroup,
                        false
                    ) as Chip
                with(chip) {
                    text = listOfCategories[i].name
                    setEnsureMinTouchTargetSize(true)
                    minimumWidth = 0
                }
                chipGroup.addView(chip)
                themingChips(chipGroup, transparentChip)
            }
        }
    }

    private fun themingChips(chipGroup: ChipGroup, transparentChip: Boolean) {
        chipGroup.children.forEach { chip ->
            if (transparentChip) {
                with(chip as Chip) {
                    chipStrokeWidth = 1f
                    chipBackgroundColor =
                        ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.secondaryBrandColor))
                }
            } else {
                with(chip as Chip) {
                    chipStrokeWidth = 0f
                    chipBackgroundColor =
                        ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.generalBackgroundColor))
                }
            }
        }
    }

    inner class TemplateWithBackImageViewHolder(val binding: ItemTemplateWithBackgroundImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: TemplateModel) {
            val templateForBundle = TemplateForBundleModel(
                id = data.id, title = data.name,
                description = data.description,
                photos = data.photos,
                challengeWithVoting = data.challengeType,
                multipleReports = data.multipleReports,
                showContenders = data.showContenders,
                sections = data.sections,
                scopeOfTemplates = data.scope
            )
            addSections(binding.chipGroup, data.sections, false)

            binding.title.text = data.name
            binding.description.text = data.description
            if(data.photos.isNotEmpty()){
                binding.imageBackground.setImage(data.photos[0])
            }
            binding.mainCard.setOnClickListener {
                onTemplateClicked?.invoke(
                    templateForBundle
                )
            }
            if (data.editableByMe) {
                binding.editCard.visible()
                binding.editCard.setOnClickListener {
                    onTemplateEditClicked?.invoke(templateForBundle)
                }
            } else {
                binding.editCard.invisible()
                binding.editCard.setOnClickListener { }
            }
            binding.editIv.setColorFilter(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
            binding.title.setTextColor(ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.generalBackgroundColor)))
            binding.description.setTextColor(ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.generalBackgroundColor)))
            //   binding.shareIv.setColorFilter(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)?.photos.isNullOrEmpty()) {
            true -> R.layout.item_template
            false -> R.layout.item_template_with_background_image
        }
    }
}