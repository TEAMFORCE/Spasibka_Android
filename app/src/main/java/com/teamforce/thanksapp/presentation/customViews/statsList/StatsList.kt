package com.teamforce.thanksapp.presentation.customViews.statsList

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.teamforce.thanksapp.data.entities.profile.ProfileStatsEntity
import com.teamforce.thanksapp.databinding.ItemStatsBinding
import com.teamforce.thanksapp.databinding.StatsListLayoutBinding
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.utils.branding.Branding


class StatsList @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), Themable {

    private var _binding: StatsListLayoutBinding? = null
    private val binding get() = checkNotNull(_binding) { "Binding is null" }

    val colorList = mutableListOf<String>()

    init {
        _binding = StatsListLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        colorList.addAll(listOf(
            Branding.brand.colorsJson.mainBrandColor,
            Branding.brand.colorsJson.generalNegativeColor,
            Branding.brand.colorsJson.minorSuccessColor,
            Branding.brand.colorsJson.minorErrorColor,
            Branding.brand.colorsJson.minorInfoColor,
            Branding.brand.colorsJson.minorInfoSecondaryColor,
            Branding.brand.colorsJson.minorWarningColor,
            Branding.brand.colorsJson.extra1,
            Branding.brand.colorsJson.extra2
        ))
    }

    fun setStats(data: List<ProfileStatsEntity>){
        inflateStatsToList(data)
    }

    private fun inflateStatsToList(data: List<ProfileStatsEntity>) {
        binding.statsLinear.removeAllViews()
        data.forEach {
            val itemStatBinding: ItemStatsBinding = ItemStatsBinding.inflate(
                LayoutInflater.from(context),
                this,
              false
            )
            itemStatBinding.statNameTv.text = it.name
            itemStatBinding.statPercentTv.text = "${Math.round(it.percentFromTotalAmount)}%"
            val randomColor = Color.parseColor(Branding.brand.colorsJson.mainBrandColor)
            itemStatBinding.statCard.setCardBackgroundColor(randomColor)

            binding.statsLinear.addView(itemStatBinding.root)
        }

    }

    override fun setThemeColor(theme: ColorsModel) {

    }

}