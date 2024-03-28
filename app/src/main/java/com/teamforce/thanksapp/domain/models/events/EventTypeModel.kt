package com.teamforce.thanksapp.domain.models.events

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class EventFilterModel(
    val eventtypes: List<EventTypeModel>
): Parcelable

@Parcelize
data class EventTypeModel(
    val ids: List<Int>,
    val name: String,
    var on: Boolean,
    val showFilter: Boolean = false
): Parcelable
