package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.domain.models.branding.ColorsModel

class ThemableTextInputLayoutOutlined @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.style.OutlinedRoundedBox

): TextInputLayout(context, attrs, defStyleAttr), Themable {

    override fun setThemeColor(theme: ColorsModel) {
        hintTextColor = ColorStateList.valueOf(Color.parseColor(theme.generalContrastSecondaryColor))
        boxStrokeColor = Color.parseColor(theme.minorInfoSecondaryColor)
        setStartIconTintList(ColorStateList.valueOf(Color.parseColor(theme.mainBrandColor)))
        setEndIconTintList(ColorStateList.valueOf(Color.parseColor(theme.generalContrastColor)))
        boxStrokeErrorColor = ColorStateList.valueOf(Color.parseColor(theme.minorErrorColor))
    }

}