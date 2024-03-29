package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.imageview.ShapeableImageView
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.utils.branding.Branding

class ThemableShapeableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.style.Widget_MaterialComponents_ShapeableImageView
): ShapeableImageView(context, attrs, defStyleAttr), Themable {

    override fun setThemeColor(theme: ColorsModel) {
        imageTintList = ColorStateList.valueOf(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
        setBackgroundColor(Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor))
    }
}