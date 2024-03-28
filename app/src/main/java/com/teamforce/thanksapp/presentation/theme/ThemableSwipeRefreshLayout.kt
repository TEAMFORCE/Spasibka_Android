package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.teamforce.thanksapp.domain.models.branding.ColorsModel

class ThemableSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SwipeRefreshLayout(context, attrs), Themable {


    override fun setThemeColor(theme: ColorsModel) {
        with(this){
            setColorSchemeColors(Color.parseColor(theme.mainBrandColor))
        }
    }
}