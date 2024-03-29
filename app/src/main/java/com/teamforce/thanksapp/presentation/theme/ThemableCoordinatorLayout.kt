package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.utils.branding.Branding

class ThemableCoordinatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.bumptech.glide.R.attr.coordinatorLayoutStyle
): CoordinatorLayout(context, attrs, defStyleAttr), Themable {

    override fun setThemeColor(theme: ColorsModel) {
        val gradientDrawable = ResourcesCompat.getDrawable(
            resources,
            R.drawable.background_for_status_bar,
            null
        ) as GradientDrawable
        gradientDrawable.apply {
            colors = intArrayOf(
                Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor),
                Color.parseColor(Branding.brand.colorsJson.mainBrandColor)
            )
        }

        background = gradientDrawable
    }
}