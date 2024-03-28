package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView
import com.teamforce.thanksapp.domain.models.branding.ColorsModel

class ThemableTextViewMainBrandColor @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr), Themable {

    override fun setThemeColor(theme: ColorsModel) {
        with(this as TextView){
            setTextColor(Color.parseColor(theme.mainBrandColor))
        }

    }
}