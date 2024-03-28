package com.teamforce.thanksapp.domain.models.profile

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationModel(
    val lat: Double,
    val lon: Double,
    val city: String,
    val timezone: String
) : Parcelable