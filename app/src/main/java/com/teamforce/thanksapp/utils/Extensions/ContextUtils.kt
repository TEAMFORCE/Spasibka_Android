package com.teamforce.thanksapp.utils.Extensions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.Locals.Lang


fun getCurrentLang(context: Context): Lang {
    val preferences = context.getSharedPreferences(Consts.LOCALE_PREF, Context.MODE_PRIVATE)
    return when (preferences?.getString(Consts.CURRENT_LANGUAGE, Lang.SYSTEM.lang)) {
        Lang.RUSSIAN.lang -> {
            Lang.RUSSIAN
        }
        Lang.ENGLISH.lang -> {
            Lang.ENGLISH
        }
        Lang.SYSTEM.lang -> {
            Lang.ENGLISH
        }
        else -> {
            Lang.DEFAULT
        }
    }
}


fun Context.isActivityForIntentAvailable(intent: Intent?): Boolean{
    val packageManager = this.packageManager
    val list: List<*> =
        packageManager.queryIntentActivities(intent!!, PackageManager.MATCH_DEFAULT_ONLY)
    return list.isNotEmpty()
}
fun getCurrentSystemLang(): Lang{
    val systemLang = ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0)
    return when(systemLang?.language){
        Lang.ENGLISH.lang -> Lang.ENGLISH
        Lang.RUSSIAN.lang -> Lang.RUSSIAN
        else -> Lang.DEFAULT
    }
}