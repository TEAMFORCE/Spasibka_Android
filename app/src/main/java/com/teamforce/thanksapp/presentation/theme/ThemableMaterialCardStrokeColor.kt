package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.card.MaterialCardView
import com.teamforce.thanksapp.domain.models.branding.ColorsModel


class ThemableMaterialCardStrokeColor @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialButtonStyle
) : MaterialCardView(context, attrs, defStyleAttr), Themable {

    override fun setThemeColor(theme: ColorsModel) {
        this.setStrokeColor(ColorStateList.valueOf(Color.parseColor(theme.mainBrandColor)))
    }

}