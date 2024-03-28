package com.teamforce.thanksapp.data.entities.location

import com.teamforce.thanksapp.domain.models.profile.LocationModel

data class LocationEntity(
    val city: String?,
    val lat: Double?,
    val lon: Double?,
    val offset: Int?
)

fun LocationEntity.toLocationModel(): LocationModel? {
    if (lat == null || lon == null || city == null || offset == null) return null

    val hours = offset / 3600
    val minutes = (offset % 3600) / 60
    val plusOrMinus = if (hours == 0) "0" else if (hours > 0) "+" else "-"
    return LocationModel(
        lat = lat,
        lon = lon,
        city = city,
        timezone = "UTC$plusOrMinus${String.format("%02d:%02d", hours, minutes)}"
    )
}