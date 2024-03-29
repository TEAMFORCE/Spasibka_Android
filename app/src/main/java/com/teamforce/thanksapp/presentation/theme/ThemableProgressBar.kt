package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ProgressBar
import com.teamforce.thanksapp.domain.models.branding.ColorsModel

class ThemableProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.progressBarStyle
) : ProgressBar(context, attrs, defStyleAttr), Themable {


    override fun setThemeColor(theme: ColorsModel) {
        with(this){
            indeterminateTintList = ColorStateList.valueOf(Color.parseColor(theme.mainBrandColor))
        }

    }
}