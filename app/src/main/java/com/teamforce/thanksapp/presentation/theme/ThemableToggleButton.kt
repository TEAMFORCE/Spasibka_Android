package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.button.MaterialButton
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.utils.branding.Branding


class ThemableToggleButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr), Themable {

    private var themeInner: ColorsModel? = null


    override fun setThemeColor(theme: ColorsModel) {
        themeInner = theme
        with(this as MaterialButton) {
            setTextColor(Color.parseColor(theme.generalContrastColor))
            backgroundTintList = ColorStateList.valueOf(Color.parseColor(theme.minorInfoSecondaryColor))
        }
    }

    fun setColorByChecked(isChecked: Boolean){
        if(isChecked){
            setTextColor(Color.parseColor(themeInner?.generalBackgroundColor))
            backgroundTintList = ColorStateList.valueOf(Color.parseColor(themeInner?.mainBrandColor))
        }else{
            setTextColor(Color.parseColor(themeInner?.generalContrastColor))
            backgroundTintList = ColorStateList.valueOf(Color.parseColor(themeInner?.minorInfoSecondaryColor))
        }
    }
}