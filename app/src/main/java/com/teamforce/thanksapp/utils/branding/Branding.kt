package com.teamforce.thanksapp.utils.branding

import android.view.View
import android.view.ViewGroup
import com.teamforce.thanksapp.domain.models.branding.BonusnameModel
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.domain.models.branding.OrganizationBrandingModel
import com.teamforce.thanksapp.presentation.theme.Themable
import java.util.*
import javax.inject.Singleton

@Singleton
class Branding {
    companion object {
        /**
         * Пробегается по всем внутренним View, которые реализуют интерфейс
         * Themable, вызывая у них метод setThemeColor()
         *
         * @param rootView View с которой начнется итерация
         * @return None
         */
        fun traverseViews(rootView: View) {
            val viewsStack = Stack<View>()
            viewsStack.push(rootView)

            while (!viewsStack.isEmpty()) {
                val view = viewsStack.pop()
                if (view is Themable) view.setThemeColor(Branding.appTheme)

                if (view is ViewGroup) {
                    val childCount = view.childCount
                    for (i in childCount - 1 downTo 0) {
                        val childView = view.getChildAt(i)
                        viewsStack.push(childView)
                    }
                }
            }
        }
        val appThemeDefault = ColorsModel(
            mainBrandColor = ConstBranding.GENERAL_BRAND,
            secondaryBrandColor = ConstBranding.GENERAL_BRAND_SECONDARY,
            generalBackgroundColor = ConstBranding.GENERAL_BACKGROUND, // name is negative in design
            generalContrastColor = ConstBranding.GENERAL_CONTRAST,
            generalContrastSecondaryColor = ConstBranding.GENERAL_CONTRAST_SECONDARY, // never use
            midpoint = ConstBranding.MIDPOINT, // never use
            generalNegativeColor = ConstBranding.GENERAL_NEGATIVE, // never use

            minorSuccessColor = ConstBranding.MINOR_SUCCESS,
            minorSuccessSecondaryColor = ConstBranding.MINOR_SUCCESS_SECONDARY,
            minorErrorColor = ConstBranding.MINOR_ERROR, // never use
            minorErrorSecondaryColor = ConstBranding.MINOR_ERROR_SECONDARY, // never use
            minorInfoColor = ConstBranding.MINOR_INFO,
            minorInfoSecondaryColor = ConstBranding.MINOR_INFO_SECONDARY,
            minorWarningColor = ConstBranding.MINOR_WARNING, // never use
            minorWarningSecondaryColor = ConstBranding.MINOR_WARNING_SECONDARY, // never use
            minorNegativeSecondaryColor = ConstBranding.MINOR_NEGATIVE_SECONDARY, // never use

            extra1 = ConstBranding.EXTRA_1, // is blue in original theme
            extra2 = ConstBranding.EXTRA_2, // is yellow in original theme
        )

        var brand: OrganizationBrandingModel = OrganizationBrandingModel(
            bonusName = BonusnameModel(listOf("спасибка", "спасибке", "спасибку", "спасибок", "спасибки")),
            colorsJson = appThemeDefault,
            name = null,
            smallLogo = null,
            loginLogo = null,
            menuLogo = null,
            photo = null,
            photo2 = null,
            telegramGroup = null,
            isNewColor = false
        )

        var forms: List<String>? = null

        private fun getFormForThanks(sum: Int?, case: Cases): String {
            val thanksForms = listOf("спасибка", "спасибке", "спасибку", "спасибок", "спасибки")
            val lastDigit = if(sum == null) 0 else sum % 10
            val lastTwoDigits = if(sum == null) 0 else sum % 100
            return if (lastTwoDigits in 11..19) {
                thanksForms[3]
            } else if (lastDigit == 1) {
                when (case) {
                    Cases.GENITIVE -> thanksForms[0]
                    Cases.ACCUSATIVE -> thanksForms[2]
                    Cases.DATIVE -> thanksForms[1]
                }
            } else if (lastDigit in 2..4) {
                thanksForms[4]
            } else {
                thanksForms[3]
            }
        }

        private fun getFormForWord(sum: Int?, case: Cases): String {
            val lastDigit = if(sum == null) 0 else sum % 10
            val lastTwoDigits = if(sum == null) 0 else sum % 100
            return if (lastTwoDigits in 11..19) {
                forms?.get(3) ?: ""
            } else if (lastDigit == 1) {
                when (case) {
                    Cases.GENITIVE -> forms?.get(0) ?: ""
                    Cases.ACCUSATIVE -> forms?.get(2) ?: ""
                    Cases.DATIVE -> forms?.get(1) ?: ""
                }
            } else if (lastDigit in 2..4) {
                forms?.get(4) ?: ""
            } else {
                forms?.get(3) ?: ""
            }
        }

        fun declineCurrency(sum: Int?, case: Cases): String {
            return if(!forms.isNullOrEmpty() && forms?.size!! >= 5){
                getFormForWord(sum, case)
            }else{
                getFormForThanks(sum, case)
            }
        }

        var appTheme = ColorsModel(
            mainBrandColor = ConstBranding.GENERAL_BRAND,
            secondaryBrandColor = ConstBranding.GENERAL_BRAND_SECONDARY,
            generalBackgroundColor = ConstBranding.GENERAL_BACKGROUND, // name is negative in design
            generalContrastColor = ConstBranding.GENERAL_CONTRAST,
            generalContrastSecondaryColor = ConstBranding.GENERAL_CONTRAST_SECONDARY, // never use
            midpoint = ConstBranding.MIDPOINT, // never use
            generalNegativeColor = ConstBranding.GENERAL_NEGATIVE, // never use

            minorSuccessColor = ConstBranding.MINOR_SUCCESS,
            minorSuccessSecondaryColor = ConstBranding.MINOR_SUCCESS_SECONDARY,
            minorErrorColor = ConstBranding.MINOR_ERROR, // never use
            minorErrorSecondaryColor = ConstBranding.MINOR_ERROR_SECONDARY, // never use
            minorInfoColor = ConstBranding.MINOR_INFO,
            minorInfoSecondaryColor = ConstBranding.MINOR_INFO_SECONDARY,
            minorWarningColor = ConstBranding.MINOR_WARNING, // never use
            minorWarningSecondaryColor = ConstBranding.MINOR_WARNING_SECONDARY, // never use
            minorNegativeSecondaryColor = ConstBranding.MINOR_NEGATIVE_SECONDARY, // never use

            extra1 = ConstBranding.EXTRA_1, // is blue in original theme
            extra2 = ConstBranding.EXTRA_2, // is yellow in original theme
        )
    }

}

enum class Cases {
    GENITIVE, // Р.П У меня есть ....
    ACCUSATIVE, // В.П Я отправил/получил, Он получил
    DATIVE // Д.П Надо отправить по
}

