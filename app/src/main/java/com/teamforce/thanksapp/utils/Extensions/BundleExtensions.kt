package com.teamforce.thanksapp.utils

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

fun <T : Parcelable> Bundle.getParcelableExt(key: String, objectClass: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= 33) {
        this.getParcelable(key, objectClass)
    } else {
        this.getParcelable(key)
    }
}

inline fun <reified T : Parcelable> Bundle.parceleable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= 33) {
        this.getParcelable(key, T::class.java)
    } else {
        this.getParcelable(key)
    }
}

inline fun <reified T : Serializable> Bundle.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializable(key) as? T
}
