package com.teamforce.thanksapp.presentation.theme

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.button.MaterialButton
import com.teamforce.thanksapp.domain.models.branding.ColorsModel

class ThemableDefaultButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr), Themable {

    private var themeInner: ColorsModel? = null


    override fun setThemeColor(theme: ColorsModel) {
        themeInner = theme
        with(this as MaterialButton) {
            setTextColor(Color.parseColor(theme.generalBackgroundColor))
            backgroundTintList = ColorStateList.valueOf(Color.parseColor(theme.mainBrandColor))
        }
    }


    override fun setEnabled(enabled: Boolean) {

        if (themeInner != null && this.isEnabled != enabled) {
            val backgroundTintAnimator = createColorAnimator(
                themeInner!!.secondaryBrandColor,
                themeInner!!.mainBrandColor
            )

            val textTintAnimator = createColorAnimator(
                themeInner!!.mainBrandColor,
                themeInner!!.generalBackgroundColor
            )

            backgroundTintAnimator.addUpdateListener { animation ->
                (this as MaterialButton).backgroundTintList =
                    ColorStateList.valueOf(animation.animatedValue as Int)
            }

            textTintAnimator.addUpdateListener { animation ->
                (this as MaterialButton).setTextColor(
                    ColorStateList.valueOf(animation.animatedValue as Int)
                )
            }
            if(enabled){
                backgroundTintAnimator.start()
                textTintAnimator.start()
            }else{
                backgroundTintAnimator.reverse()
                textTintAnimator.reverse()
            }

        }
        super.setEnabled(enabled)
    }

    private fun createColorAnimator(
        startColor: String,
        endColor: String
    ): ValueAnimator {
        val start = Color.parseColor(startColor)
        val end = Color.parseColor(endColor)

        val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), start, end)
        colorAnimator.duration = 300 // Adjust the duration as needed


        return colorAnimator
    }
}