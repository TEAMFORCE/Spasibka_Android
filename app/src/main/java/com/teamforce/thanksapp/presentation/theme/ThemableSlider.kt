package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources

import com.google.android.material.slider.Slider
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.dp

class ThemableSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Slider(context, attrs, defStyleAttr), Themable {

    override fun setThemeColor(theme: ColorsModel) {

        tickActiveTintList =
            ColorStateList.valueOf(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
        tickInactiveTintList =
            ColorStateList.valueOf(Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor))

        trackActiveTintList =
            ColorStateList.valueOf(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
        trackInactiveTintList =
            ColorStateList.valueOf(Color.parseColor("#FBCDBF"))
        trackHeight = 12.dp
        setCustomThumbDrawable(
            drawCircleThumb(
                Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor),
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.ic_challenge
                )
            )
        )

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
            val padding = 10
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
}