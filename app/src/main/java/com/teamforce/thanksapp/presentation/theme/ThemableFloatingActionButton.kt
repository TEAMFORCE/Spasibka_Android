package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.R
import com.teamforce.thanksapp.domain.models.branding.ColorsModel

class ThemableFloatingActionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.floatingActionButtonStyle
): FloatingActionButton(context, attrs, defStyleAttr), Themable {

    override fun setThemeColor(theme: ColorsModel) {
        setRippleColor(ColorStateList.valueOf(Color.parseColor(theme.mainBrandColor)))
        backgroundTintList = ColorStateList.valueOf(Color.parseColor(theme.secondaryBrandColor))
        imageTintList = ColorStateList.valueOf(Color.parseColor(theme.mainBrandColor))
    }

}