package com.teamforce.thanksapp.data.entities.branding

data class OrganizationBrandingEntity(
    val bonusname: BonusnameEntity,
    val colors_json: ColorsJson,
    val name: String,
    val small_logo: String?,
    val menu_logo: String?,
    val login_logo: String?,
    val photo: String?,
    val photo2: String?,
    val telegram_group: List<String>
)