package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.utils.branding.Branding


class ThemableImageVIewIcon@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): AppCompatImageView(context, attrs, defStyleAttr), Themable {

    override fun setThemeColor(theme: ColorsModel) {
        setColorFilter(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
    }
}