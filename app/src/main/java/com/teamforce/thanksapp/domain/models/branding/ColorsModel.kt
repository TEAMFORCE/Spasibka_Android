package com.teamforce.thanksapp.domain.models.branding

import com.teamforce.thanksapp.utils.branding.ConstBranding

data class ColorsModel(
    // General color
    val mainBrandColor: String = ConstBranding.GENERAL_BRAND,
    val secondaryBrandColor: String = ConstBranding.GENERAL_BRAND_SECONDARY,
    val generalBackgroundColor: String = ConstBranding.GENERAL_BACKGROUND, // negative in design
    val generalContrastColor: String = ConstBranding.GENERAL_CONTRAST,
    val generalContrastSecondaryColor: String = ConstBranding.GENERAL_CONTRAST_SECONDARY, // never use
    val midpoint: String = ConstBranding.MIDPOINT, // never use
    val generalNegativeColor: String = ConstBranding.GENERAL_NEGATIVE, // never use

    // Minor Color
    val minorSuccessColor: String = ConstBranding.MINOR_SUCCESS,
    val minorSuccessSecondaryColor: String = ConstBranding.MINOR_SUCCESS_SECONDARY,
    val minorErrorColor: String = ConstBranding.MINOR_ERROR, // never use
    val minorErrorSecondaryColor: String = ConstBranding.MINOR_ERROR_SECONDARY, // never use
    val minorInfoColor: String = ConstBranding.MINOR_INFO,
    val minorInfoSecondaryColor: String = ConstBranding.MINOR_INFO_SECONDARY,
    val minorWarningColor: String = ConstBranding.MINOR_WARNING, // never use
    val minorWarningSecondaryColor: String = ConstBranding.MINOR_WARNING_SECONDARY, // never use
    val minorNegativeSecondaryColor: String = ConstBranding.MINOR_NEGATIVE_SECONDARY, // never use

    // Extra Color
    val extra1: String = ConstBranding.EXTRA_1, // is blue in original theme
    val extra2: String = ConstBranding.EXTRA_2, // is yellow in original theme
)
