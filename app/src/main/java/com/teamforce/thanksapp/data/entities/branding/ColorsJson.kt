package com.teamforce.thanksapp.data.entities.branding

import com.google.gson.annotations.SerializedName

data class ColorsJson(
    @SerializedName("general-brand")
    val generalBrand: String,
    @SerializedName("general-brand-secondary")
    val generalBrandSecondary: String,
    @SerializedName("minor-info")
    val minorInfo: String,
    @SerializedName("minor-error")
    val minorError: String,
    @SerializedName("minor-success")
    val minorSuccess: String,
    @SerializedName("minor-warning")
    val minorWarning: String,
    @SerializedName("general-contrast")
    val generalContrast: String,
    @SerializedName("general-midpoint")
    val generalMidpoint: String,
    @SerializedName("general-negative")
    val generalNegative: String,
    @SerializedName("minor-info-secondary")
    val minorInfoSecondary: String,
    @SerializedName("minor-error-secondary")
    val minorErrorSecondary: String,
    @SerializedName("minor-success-secondary")
    val minorSuccessSecondary: String,
    @SerializedName("minor-warning-secondary")
    val minorWarningSecondary: String,
    @SerializedName("minor-negative-secondary")
    val minorNegativeSecondary: String,
    @SerializedName("general-contrast-secondary")
    val generalContrastSecondary: String,
    val extra1: String,
    val extra2: String,


    )