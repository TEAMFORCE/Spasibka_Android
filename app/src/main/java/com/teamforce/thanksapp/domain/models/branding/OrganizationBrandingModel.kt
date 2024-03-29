package com.teamforce.thanksapp.domain.models.branding


data class OrganizationBrandingModel(
    val bonusName: BonusnameModel,
    val colorsJson: ColorsModel,
    val name: String?,
    val smallLogo: String?,
    val menuLogo: String?,
    val loginLogo: String?,
    val photo: String? = null,
    val photo2: String? = null,
    val telegramGroup: List<String>?,
    val isNewColor: Boolean = false,
)
