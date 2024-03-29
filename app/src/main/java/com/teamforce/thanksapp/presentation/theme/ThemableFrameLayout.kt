package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.utils.branding.Branding

class ThemableFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr), Themable {

    override fun setThemeColor(theme: ColorsModel) {
        val gradientDrawable = ResourcesCompat.getDrawable(
            resources,
            R.drawable.background_for_frame_layout,
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