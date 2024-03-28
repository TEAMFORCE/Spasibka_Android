package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.glide.setSmallLogo
import com.teamforce.thanksapp.utils.isNullOrEmptyMy


class ThemableImageViewSmallLogo @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): AppCompatImageView(context, attrs, defStyleAttr), Themable {
    override fun setThemeColor(theme: ColorsModel) {
        if(!Branding.brand.photo.isNullOrEmptyMy()){
            setSmallLogo(Branding.brand.photo!!)
        }

    }
}