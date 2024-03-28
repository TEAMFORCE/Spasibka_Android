package com.teamforce.thanksapp.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.teamforce.thanksapp.presentation.viewmodel.AuthorizationType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesWrapperUserData @Inject constructor(
    private val prefs: SharedPreferences
) {

    fun logout() {
        prefs.edit {
            putString(SP_ARG_TELEGRAM, null)
            putString(SP_ARG_TOKEN, null)
            putString(SP_ARG_USERNAME, null)
            putString(SP_ARG_EMAIL, null)
            putString(SP_ARG_USER_ID, null)
            putString(SP_ARG_USERAVATAR, null)
            putBoolean(SP_USER_IS_ADMIN, false)
            putString(SP_USER_CURRENT_ORG_ID, null)
            putString(SP_USER_IS_SUPERUSER, null)
            putString(SP_ARG_CURRENT_LOGIN, null)
            putString(SP_USER_VK_ACCESS_TOKEN, null)
        }

    }

    fun deleteCredentialsForChangeOrg() {
        prefs.edit {
            putString(SP_ARG_TG_CODE, null)
            putString(SP_ARG_X_CODE, null)
            putString(SP_ARG_ORG_CODE, null)
            putString(SP_ARG_EMAIL_CODE, null)
            putBoolean(SP_USER_IS_ADMIN, false)
            putString(SP_USER_CURRENT_ORG_ID, null)
            putString(SP_USER_IS_SUPERUSER, null)
        }
    }

    fun savePreferences(
        authToken: String?,
        telegram: String?,
        username: String?,
    ): Boolean {
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putString(SP_ARG_TELEGRAM, telegram)
        editor.putString(SP_ARG_TOKEN, authToken)
        editor.putString(SP_ARG_USERNAME, username)
       return editor.commit()
    }

    fun savePreferencesForChangeOrg(
        xCode: String?,
        orgCode: String?,
        xId: String?,
        xEmail: String?,
        authType: AuthorizationType
    ) {
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putString(SP_ARG_TG_CODE, xId)
        editor.putString(SP_ARG_X_CODE, xCode)
        editor.putString(SP_ARG_ORG_CODE, orgCode)
        editor.putString(SP_ARG_AUTH_TYPE, authType.name)
        editor.putString(SP_ARG_EMAIL_CODE, xEmail)
        editor.apply()
    }

    var xId: String?
        get() = prefs.getString(
            SP_ARG_TG_CODE, null
        )
        set(value) = prefs.edit {
            putString(SP_ARG_TG_CODE, value)
        }

    var xCode: String?
        get() = prefs.getString(
            SP_ARG_X_CODE, null
        )
        set(value) = prefs.edit {
            putString(SP_ARG_X_CODE, value)
        }

    var xEmail: String?
        get() = prefs.getString(
            SP_ARG_EMAIL_CODE, null
        )
        set(value) = prefs.edit {
            putString(SP_ARG_EMAIL_CODE, value)
        }

    var orgCode: String?
        get() = prefs.getString(
            SP_ARG_ORG_CODE, null
        )
        set(value) = prefs.edit {
            putString(SP_ARG_ORG_CODE, value)
        }

    var authType: String?
        get() = prefs.getString(
            SP_ARG_AUTH_TYPE, null
        )
        set(value) = prefs.edit {
            putString(SP_ARG_AUTH_TYPE, value)
        }

    fun getToken(): String? {
        return prefs.getString(SP_ARG_TOKEN, null)
    }

    fun clearToken() {
        return prefs.edit {
            putString(SP_ARG_TOKEN, null)
        }
    }

    var currentLogin: String?
        get() = prefs.getString(
            SP_ARG_CURRENT_LOGIN, null
        )
        set(value) = prefs.edit {
            putString(SP_ARG_CURRENT_LOGIN, value)
        }

    var username: String?
        get() = prefs.getString(
            SP_ARG_USERNAME, null
        )
        set(value) = prefs.edit {
            putString(SP_ARG_USERNAME, value)
        }

    var userAvatar: String?
        get() = prefs.getString(
            SP_ARG_USERAVATAR, null
        )
        set(value) = prefs.edit {
            putString(SP_ARG_USERAVATAR, value)
        }

    var profileId: String?
        get() = prefs.getString(
            SP_ARG_USER_ID, null
        )
        set(value) = prefs.edit {
            putString(SP_ARG_USER_ID, value)
        }

    var pushToken: String?
        get() = prefs.getString(
            SP_ARG_PUSH_TOKEN, null
        )
        set(value) = prefs.edit {
            putString(SP_ARG_PUSH_TOKEN, value)
        }

    var isAdmin: Boolean
        get() = prefs.getBoolean(
            SP_USER_IS_ADMIN, false
        )
        set(value) = prefs.edit {
            putBoolean(SP_USER_IS_ADMIN, value)
        }
    var isSuperUser: Boolean
        get() = prefs.getBoolean(
            SP_USER_IS_SUPERUSER, false
        )
        set(value) = prefs.edit {
            putBoolean(SP_USER_IS_SUPERUSER, value)
        }

    var currentOrgId: String?
        get() = prefs.getString(
            SP_USER_CURRENT_ORG_ID, null
        )
        set(value) = prefs.edit {
            putString(SP_USER_CURRENT_ORG_ID, value)
        }

    var vkAccessToken: String?
        get() = prefs.getString(
            SP_USER_VK_ACCESS_TOKEN, null
        )
        set(value) = prefs.edit{
            putString(SP_USER_VK_ACCESS_TOKEN, value)
        }

    var surname: String?
        get() = prefs.getString(
            SP_USER_SURNAME, null
        )
        set(value) = prefs.edit{
            putString(SP_USER_SURNAME, value)
        }
    var name: String?
        get() = prefs.getString(
            SP_USER_NAME, null
        )
        set(value) = prefs.edit{
            putString(SP_USER_NAME, value)
        }

    companion object {
        private const val SP_ARG_TELEGRAM = "Telegram"
        private const val SP_ARG_TOKEN = "Token"
        private const val SP_ARG_USERNAME = "Username"
        private const val SP_ARG_CURRENT_LOGIN = "Current_Loging"
        private const val SP_ARG_USERAVATAR = "UserAvatar"
        private const val SP_ARG_EMAIL = "Email"
        private const val SP_ARG_USER_ID = "UserId"
        private const val SP_ARG_PUSH_TOKEN = "pushToken"
        private const val SP_ARG_TG_CODE = "tgCode"
        private const val SP_ARG_X_CODE = "xCode"
        private const val SP_ARG_ORG_CODE = "orgCode"
        private const val SP_ARG_AUTH_TYPE = "authType"
        private const val SP_ARG_EMAIL_CODE = "emailCode"
        private const val SP_ARG_PRIVACY_POLICY_CHECK = "privacyPolicyCheck"
        private const val SP_USER_IS_ADMIN = "userIsAdmin"
        private const val SP_USER_IS_SUPERUSER = "userIsSuperUser"
        private const val SP_USER_CURRENT_ORG_ID = "currentOrgId"
        private const val SP_USER_VK_ACCESS_TOKEN = "vkAccessToken"
        private const val SP_USER_NAME = "sp_name"
        private const val SP_USER_SURNAME = "sp_surname"
    }
}