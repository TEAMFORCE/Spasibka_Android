package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.utils.dp

class ThemableNotFilledButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr), Themable {

    init {
        this.setPadding(0.dp, 22.dp, 0.dp, 22.dp)
    }
    override fun setThemeColor(theme: ColorsModel) {
        with(this as MaterialButton){
            setTextColor(Color.parseColor(theme.generalContrastColor))
            iconTint = ColorStateList.valueOf(Color.parseColor(theme.mainBrandColor))
            backgroundTintList = ColorStateList.valueOf(Color.parseColor(theme.generalBackgroundColor))
        }

    }
}