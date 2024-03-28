package com.teamforce.thanksapp.model.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TagModel(
    val id: Int,
    val name: String,
    var enabled: Boolean = false
) : Parcelable
