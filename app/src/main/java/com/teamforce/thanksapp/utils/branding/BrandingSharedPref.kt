package com.teamforce.thanksapp.utils.branding

import android.content.SharedPreferences
import com.teamforce.thanksapp.domain.models.branding.BonusnameModel
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.domain.models.branding.OrganizationBrandingModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrandingSharedPref @Inject constructor(
    private val prefs: SharedPreferences
) {

    fun safeBrandingTheme(brand: OrganizationBrandingModel) {
        val editor: SharedPreferences.Editor = prefs.edit()
        putColors(brand.colorsJson, editor)
        editor.apply {
            putString(FORMS_BRAND, brand.bonusName.RU?.joinToString(","))
            putString(NAME_ORGANIZATION_BRAND, brand.name)
            putString(SMALL_LOGO_BRAND, brand.smallLogo)
            putString(MENU_LOGO_BRAND, brand.menuLogo)
            putString(LOGIN_LOGO_BRAND, brand.loginLogo)
            putString(PHOTO_BRAND, brand.photo)
            putString(PHOTO_2_BRAND, brand.photo2)
            putString(TG_GROUP_BRAND, brand.telegramGroup?.joinToString(","))
        }
        editor.apply()

    }

    fun getBrandingTheme(): OrganizationBrandingModel {
        prefs.apply {
            return OrganizationBrandingModel(
                colorsJson = getColors(this),
                bonusName = BonusnameModel(RU = this.getString(FORMS_BRAND, null)?.split(",")),
                name = this.getString(NAME_ORGANIZATION_BRAND, null),
                smallLogo = this.getString(SMALL_LOGO_BRAND, null),
                menuLogo = this.getString(MENU_LOGO_BRAND, null),
                loginLogo = this.getString(LOGIN_LOGO_BRAND, null),
                photo = this.getString(PHOTO_BRAND, null),
                photo2 = this.getString(PHOTO_2_BRAND, null),
                telegramGroup = this.getString(TG_GROUP_BRAND, null)?.split(",")
            )
        }

    }

    private fun putColors(colors: ColorsModel, editorSharedPref: SharedPreferences.Editor) {
        editorSharedPref.apply {
            putString(GENERAL_BRAND, colors.mainBrandColor)
            putString(GENERAL_BRAND_SECONDARY, colors.secondaryBrandColor)
            putString(GENERAL_BACKGROUND, colors.generalBackgroundColor)
            putString(GENERAL_CONTRAST, colors.generalContrastColor)
            putString(GENERAL_CONTRAST_SECONDARY, colors.generalContrastSecondaryColor)
            putString(MIDPOINT, colors.midpoint)
            putString(GENERAL_NEGATIVE, colors.generalNegativeColor)
            putString(MINOR_SUCCESS, colors.minorSuccessColor)
            putString(MINOR_SUCCESS_SECONDARY, colors.minorSuccessSecondaryColor)
            putString(MINOR_ERROR, colors.minorErrorColor)
            putString(MINOR_ERROR_SECONDARY, colors.minorErrorSecondaryColor)
            putString(MINOR_INFO, colors.minorInfoColor)
            putString(MINOR_INFO_SECONDARY, colors.minorInfoSecondaryColor)
            putString(MINOR_WARNING, colors.minorWarningColor)
            putString(MINOR_WARNING_SECONDARY, colors.minorWarningSecondaryColor)
            putString(MINOR_NEGATIVE_SECONDARY, colors.minorNegativeSecondaryColor)
            putString(EXTRA_1, colors.extra1)
            putString(EXTRA_2, colors.extra2)
        }
    }

    private fun getColors(prefs: SharedPreferences): ColorsModel {
        prefs.apply {
            return ColorsModel(
                mainBrandColor = getString(GENERAL_BRAND, ConstBranding.GENERAL_BRAND) ?: ConstBranding.GENERAL_BRAND,
                secondaryBrandColor = getString(GENERAL_BRAND_SECONDARY, ConstBranding.GENERAL_BRAND_SECONDARY) ?: ConstBranding.GENERAL_BRAND_SECONDARY,
                generalBackgroundColor = getString(GENERAL_BACKGROUND, ConstBranding.GENERAL_BACKGROUND) ?: ConstBranding.GENERAL_BACKGROUND,
                generalContrastColor = getString(GENERAL_CONTRAST, ConstBranding.GENERAL_CONTRAST) ?: ConstBranding.GENERAL_CONTRAST,
                generalContrastSecondaryColor = getString(GENERAL_CONTRAST_SECONDARY, ConstBranding.GENERAL_CONTRAST_SECONDARY) ?: ConstBranding.GENERAL_CONTRAST_SECONDARY,
                midpoint = getString(MIDPOINT, ConstBranding.MIDPOINT) ?: ConstBranding.MIDPOINT,
                generalNegativeColor = getString(GENERAL_NEGATIVE, ConstBranding.GENERAL_NEGATIVE) ?: ConstBranding.GENERAL_NEGATIVE,
                minorSuccessColor = getString(MINOR_SUCCESS, ConstBranding.MINOR_SUCCESS) ?: ConstBranding.MINOR_SUCCESS,
                minorSuccessSecondaryColor = getString(MINOR_SUCCESS_SECONDARY, ConstBranding.MINOR_SUCCESS_SECONDARY) ?: ConstBranding.MINOR_SUCCESS_SECONDARY,
                minorErrorColor = getString(MINOR_ERROR, ConstBranding.MINOR_ERROR) ?: ConstBranding.MINOR_ERROR,
                minorErrorSecondaryColor = getString(MINOR_ERROR_SECONDARY, ConstBranding.MINOR_ERROR_SECONDARY) ?: ConstBranding.MINOR_ERROR_SECONDARY,
                minorInfoColor = getString(MINOR_INFO, ConstBranding.MINOR_INFO) ?: ConstBranding.MINOR_INFO,
                minorInfoSecondaryColor = getString(MINOR_INFO_SECONDARY ,ConstBranding.MINOR_INFO_SECONDARY) ?: ConstBranding.MINOR_INFO_SECONDARY,
                minorWarningColor = getString(MINOR_WARNING, ConstBranding.MINOR_WARNING) ?: ConstBranding.MINOR_WARNING,
                minorWarningSecondaryColor = getString(MINOR_WARNING_SECONDARY, ConstBranding.MINOR_WARNING_SECONDARY) ?: ConstBranding.MINOR_WARNING_SECONDARY,
                minorNegativeSecondaryColor = getString(MINOR_NEGATIVE_SECONDARY, ConstBranding.MINOR_NEGATIVE_SECONDARY) ?: ConstBranding.MINOR_NEGATIVE_SECONDARY,
                extra1 = getString(EXTRA_1, ConstBranding.EXTRA_1) ?: ConstBranding.EXTRA_1,
                extra2 = getString(EXTRA_2, ConstBranding.EXTRA_2) ?: ConstBranding.EXTRA_2
            )
        }

    }


    fun clearBrandingTheme() {
        prefs.edit().clear().apply()
    }


    companion object {
        private const val SP_NAME = "com.teamforce.thanksapp"
        private const val GENERAL_BRAND = "general-brand"
        private const val GENERAL_BRAND_SECONDARY = "general-brand-secondary"
        private const val GENERAL_BACKGROUND = "general-background"
        private const val GENERAL_CONTRAST = "general-contrast"
        private const val GENERAL_CONTRAST_SECONDARY = "general-contrast-secondary"
        private const val MIDPOINT = "midpoint"
        private const val GENERAL_NEGATIVE = "general-negative"
        private const val MINOR_SUCCESS = "minor-success"
        private const val MINOR_SUCCESS_SECONDARY = "minor-success-secondary"
        private const val MINOR_ERROR = "minor-error"
        private const val MINOR_ERROR_SECONDARY = "minor-error-secondary"
        private const val MINOR_INFO = "minor-info"
        private const val MINOR_INFO_SECONDARY = "minor-info-secondary"
        private const val MINOR_WARNING = "minor-warning"
        private const val MINOR_WARNING_SECONDARY = "minor-warning-secondary"
        private const val MINOR_NEGATIVE_SECONDARY = "minor-negative-secondary"
        private const val EXTRA_1 = "extra1"
        private const val EXTRA_2 = "extra2"
        private const val FORMS_BRAND = "forms_brand"
        private const val NAME_ORGANIZATION_BRAND = "name_org_brand"
        private const val SMALL_LOGO_BRAND = "small_logo_brand"
        private const val MENU_LOGO_BRAND = "menu_logo_brand"
        private const val LOGIN_LOGO_BRAND = "login_logo_brand"
        private const val TG_GROUP_BRAND = "tg_group_brand"
        private const val PHOTO_BRAND = "photo_brand"
        private const val PHOTO_2_BRAND = "photo_2_brand"

    }


}