package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.switchmaterial.SwitchMaterial
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme

class ThemableSwitchMaterial @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.switchStyle,
) : SwitchMaterial(context, attrs, defStyleAttr), Themable {

    val thumbColorState = ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked),
        ), intArrayOf(
            Color.parseColor(appTheme.mainBrandColor),
            Color.parseColor(appTheme.generalBackgroundColor)
        )
    )
    val trackColorState = ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked),
        ), intArrayOf(
            Color.parseColor(appTheme.secondaryBrandColor),
            Color.GRAY
        )
    )
    override fun setThemeColor(theme: ColorsModel) {
        with(this as SwitchMaterial){
            trackTintList = trackColorState
            thumbTintList = thumbColorState
            // TODO Поменять ripple эффект у switch
        }

    }
}