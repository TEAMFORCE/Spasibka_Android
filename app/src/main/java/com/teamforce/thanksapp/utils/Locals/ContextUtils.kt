package com.teamforce.thanksapp.utils.Locals

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.util.Log
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.Extensions.getCurrentLang
import java.util.*

class ContextUtils(base: Context) : ContextWrapper(base) {

    companion object {

       fun getCurrentLangInApp(c: Context): String? {
            val preferences = c.getSharedPreferences(Consts.LOCALE_PREF, Context.MODE_PRIVATE)
            return preferences?.getString(Consts.CURRENT_LANGUAGE, Lang.SYSTEM.lang)
        }
        fun getCurrentLangInAppLangFormat(c: Context): Lang {
            val preferences = c.getSharedPreferences(Consts.LOCALE_PREF, Context.MODE_PRIVATE)

            return when (preferences?.getString(Consts.CURRENT_LANGUAGE, Lang.SYSTEM.lang)) {
                Lang.RUSSIAN.lang -> Lang.RUSSIAN
                Lang.ENGLISH.lang -> Lang.ENGLISH
                Lang.SYSTEM.lang -> Lang.SYSTEM
                else -> Lang.DEFAULT
            }
        }

        fun updateLocale(
            c: Context,
            localeToSwitchTo: Locale,
            sysLang: Boolean = false
        ): ContextWrapper {
            var context = c
            val resources: Resources = context.resources
            val configuration: Configuration = resources.configuration

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val localeList = LocaleList(localeToSwitchTo)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
            } else {
                configuration.locale = localeToSwitchTo
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                context = context.createConfigurationContext(configuration)
            } else {
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }


            val preferences = context.getSharedPreferences(Consts.LOCALE_PREF, Context.MODE_PRIVATE)
            val edit = preferences.edit()
            if (sysLang) edit.putString(Consts.CURRENT_LANGUAGE, Lang.SYSTEM.lang).apply()
            else edit.putString(Consts.CURRENT_LANGUAGE, localeToSwitchTo.language).apply()

            return ContextUtils(context)
        }

        fun saveNewLocaleToSP(context: Context, lang: Lang){
            val preferences = context.getSharedPreferences(Consts.LOCALE_PREF, Context.MODE_PRIVATE)
            preferences.edit().putString(Consts.CURRENT_LANGUAGE, lang.lang).apply()
        }
    }
}

enum class Lang(val lang: String) {
    RUSSIAN("ru"), ENGLISH("en"), SYSTEM("system"), DEFAULT("en");

    companion object {
        fun fromString(value: String?): Lang {
            return try {
                if (value.isNullOrBlank()) {
                    DEFAULT
                } else {
                    Lang.values().firstOrNull { it.lang == value } ?: DEFAULT
                }
            } catch (e: NoSuchElementException) {
                DEFAULT
            }
        }
    }
}
