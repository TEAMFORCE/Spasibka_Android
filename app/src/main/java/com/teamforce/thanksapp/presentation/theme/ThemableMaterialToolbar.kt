package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.domain.models.branding.ColorsModel

class ThemableMaterialToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.toolbarStyle
) : MaterialToolbar(context, attrs, defStyleAttr), Themable {

    override fun setThemeColor(theme: ColorsModel) {
        (this as MaterialToolbar).setBackgroundColor(Color.parseColor(theme.mainBrandColor))
        (this as MaterialToolbar).setTitleTextColor(Color.parseColor(theme.generalBackgroundColor))
        (this as MaterialToolbar).setNavigationIconTint(Color.parseColor(theme.generalBackgroundColor))
    }
}