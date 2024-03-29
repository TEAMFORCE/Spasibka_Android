package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import com.google.android.material.R
import com.teamforce.thanksapp.domain.models.branding.ColorsModel


class ThemableThirdButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr), Themable {


    override fun setThemeColor(theme: ColorsModel) {
        with(this as MaterialButton) {
            setTextColor(Color.parseColor(theme.generalContrastSecondaryColor))
            iconTint = ColorStateList.valueOf(Color.parseColor(theme.generalContrastSecondaryColor))
            setBackgroundColor(Color.parseColor(theme.minorInfoSecondaryColor))
            rippleColor = ColorStateList.valueOf(Color.parseColor(theme.minorInfoColor))
        }
    }

}


class ThemableThirdButtonSuccess @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr), Themable {


    override fun setThemeColor(theme: ColorsModel) {
        with(this as MaterialButton) {
            setTextColor(Color.parseColor(theme.minorSuccessColor))
            iconTint = ColorStateList.valueOf(Color.parseColor(theme.minorSuccessColor))
            setBackgroundColor(Color.parseColor(theme.minorSuccessSecondaryColor))
            rippleColor = ColorStateList.valueOf(Color.parseColor(theme.minorSuccessColor))
        }
    }

}

class ThemableThirdButtonError @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr), Themable {


    override fun setThemeColor(theme: ColorsModel) {
        with(this as MaterialButton) {
            setTextColor(Color.parseColor(theme.minorErrorColor))
            iconTint = ColorStateList.valueOf(Color.parseColor(theme.minorErrorColor))
            setBackgroundColor(Color.parseColor(theme.minorErrorSecondaryColor))
            rippleColor = ColorStateList.valueOf(Color.parseColor(theme.minorErrorColor))
        }
    }

}