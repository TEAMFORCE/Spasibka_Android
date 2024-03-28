package com.teamforce.thanksapp.presentation.theme

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.view.marginBottom
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.utils.branding.Branding

class ThemableRadioButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.radioButtonStyle
): androidx.appcompat.widget.AppCompatRadioButton(context, attrs, defStyleAttr), Themable {

   private val btnColorTint = ColorStateList(
        arrayOf(
            intArrayOf(R.attr.state_checked),
            intArrayOf(-R.attr.state_checked),
        ), intArrayOf(
            Color.parseColor(Branding.appTheme.mainBrandColor),
           Color.parseColor(Branding.appTheme.generalContrastColor)
        )
    )

    override fun setThemeColor(theme: ColorsModel) {
        with(this){
            buttonTintList = btnColorTint
        }
    }
}