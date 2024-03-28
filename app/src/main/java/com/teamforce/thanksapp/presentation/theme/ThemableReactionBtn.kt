package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.button.MaterialButton
import com.teamforce.thanksapp.domain.models.branding.ColorsModel

class ThemableReactionBtn @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr), Themable {

    override fun setThemeColor(theme: ColorsModel) {
        with(this as MaterialButton) {
            setTextColor(Color.parseColor(theme.generalContrastColor))
            backgroundTintList = ColorStateList.valueOf(Color.parseColor(theme.minorInfoColor))
            rippleColor = ColorStateList.valueOf(Color.parseColor(theme.minorInfoColor))
            iconTint = ColorStateList.valueOf(Color.parseColor(theme.generalContrastColor))
        }
    }


}