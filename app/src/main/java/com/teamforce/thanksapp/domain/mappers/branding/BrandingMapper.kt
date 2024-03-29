package com.teamforce.thanksapp.domain.mappers.branding

import com.teamforce.thanksapp.data.entities.branding.ColorsJson
import com.teamforce.thanksapp.data.entities.branding.DeclensionOfCurrency
import com.teamforce.thanksapp.data.entities.branding.OrganizationBrandingEntity
import com.teamforce.thanksapp.domain.models.branding.BonusnameModel
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.domain.models.branding.OrganizationBrandingModel
import com.teamforce.thanksapp.utils.branding.Branding
import javax.inject.Inject

class BrandingMapper @Inject constructor(

) {
    fun mapBrandingEntityToModel(from: OrganizationBrandingEntity, oldColor: ColorsModel): OrganizationBrandingModel{
        val mappedColors = mapColorsJson(from.colors_json)
        return OrganizationBrandingModel(
            bonusName = mapBonusName(from.bonusname.RU),
            smallLogo = from.small_logo,
            menuLogo = from.menu_logo,
            loginLogo = from.login_logo,
            photo = from.photo,
            photo2 = from.photo2,
            telegramGroup = from.telegram_group,
            name = from.name,
            colorsJson = mappedColors,
            isNewColor = mappedColors != oldColor
        )
    }

    private fun mapBonusName(from: DeclensionOfCurrency): BonusnameModel{
        return BonusnameModel(
            listOf(
                from.form1,
                from.form2,
                from.form3,
                from.form4,
                from.form5
            )
        )
    }

    private fun mapColorsJson(from: ColorsJson?): ColorsModel {
        if (from != null){
            return ColorsModel(
                mainBrandColor = from.generalBrand,
                secondaryBrandColor = from.generalBrandSecondary,
                // Почему то нет этого в фигме, изначально там белый цвет должен быть
                generalBackgroundColor = from.generalNegative,
                generalContrastColor = from.generalContrast,
                generalContrastSecondaryColor = from.generalContrastSecondary,
                midpoint = from.generalMidpoint,
                generalNegativeColor = from.generalNegative,
                minorSuccessColor = from.minorSuccess,
                minorSuccessSecondaryColor = from.minorSuccessSecondary,
                minorErrorColor = from.minorError,
                minorErrorSecondaryColor = from.minorErrorSecondary,
                minorInfoColor = from.minorInfo,
                minorInfoSecondaryColor = from.minorInfoSecondary,
                minorWarningColor = from.minorWarning,
                minorWarningSecondaryColor = from.minorWarningSecondary,
                minorNegativeSecondaryColor = from.minorNegativeSecondary,
                extra1 = from.extra1,
                extra2 = from.extra2
            )
        }else{
            return Branding.appThemeDefault
        }

    }


}