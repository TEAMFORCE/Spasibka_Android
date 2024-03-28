package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme

class ThemableSpinnerSolidLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextInputLayout(context, attrs, defStyleAttr), Themable {
    override fun setThemeColor(theme: ColorsModel) {
        with(this as TextInputLayout) {
            boxBackgroundColor = Color.parseColor(appTheme.secondaryBrandColor)
            hintTextColor = ColorStateList.valueOf(Color.parseColor(appTheme.mainBrandColor))
            setBoxStrokeColorStateList(ColorStateList.valueOf(Color.parseColor(appTheme.mainBrandColor)))
            setHelperTextColor(ColorStateList.valueOf(Color.parseColor(appTheme.mainBrandColor)))

            if (startIconDrawable != null)
                setStartIconTintList(
                    ColorStateList.valueOf(Color.parseColor(appTheme.mainBrandColor))
                )
        }
    }
}