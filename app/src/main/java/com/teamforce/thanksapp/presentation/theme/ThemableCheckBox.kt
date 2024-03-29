package com.teamforce.thanksapp.presentation.theme

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.graphics.drawable.AnimatedStateListDrawableCompat
import androidx.appcompat.graphics.drawable.StateListDrawableCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.dp
import com.teamforce.thanksapp.R as ThanksApp


class ThemableCheckBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.checkboxStyle
): androidx.appcompat.widget.AppCompatCheckBox(context, attrs, defStyleAttr), Themable {

    override fun setThemeColor(theme: ColorsModel) {


        // Получаем Drawable из ресурсов
        val drawable = ContextCompat.getDrawable(context, ThanksApp.drawable.checked_checkbox_d)

        if (drawable is LayerDrawable) {
            val layerDrawable = drawable

            // Получаем векторный Drawable по id
            val shapeDrawable = layerDrawable.getDrawable(1)

            // Если векторный Drawable является VectorDrawable
            if (shapeDrawable is VectorDrawable) {

                // Меняем цвет fillColor
                shapeDrawable.setTint(Color.parseColor(theme.mainBrandColor))
                buttonDrawable = getStateListDrawable()

            }
        }
    }

    private fun getStateListDrawable(): Drawable{
         val stateListDrawable = StateListDrawableCompat().apply {
            val checkedDrawable: Drawable = ContextCompat.getDrawable(context, ThanksApp.drawable.checked_checkbox_d)!!
            addState(intArrayOf(R.attr.state_checked), checkedDrawable)

            val uncheckedDrawable: Drawable = ContextCompat.getDrawable(context, ThanksApp.drawable.unchecked_checkbox_d)!!
            addState(intArrayOf(-R.attr.state_checked), uncheckedDrawable)
        }
        return stateListDrawable
    }

}


