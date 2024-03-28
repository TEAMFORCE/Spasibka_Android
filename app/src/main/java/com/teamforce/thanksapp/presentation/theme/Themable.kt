package com.teamforce.thanksapp.presentation.theme

import com.teamforce.thanksapp.domain.models.branding.ColorsModel

interface Themable {
    fun setThemeColor(theme: ColorsModel)
}