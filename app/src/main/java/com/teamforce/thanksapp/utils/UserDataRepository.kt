package com.teamforce.thanksapp.utils

import com.teamforce.thanksapp.presentation.viewmodel.AuthorizationType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataRepository @Inject constructor(
    private val sharedPreferences: com.teamforce.thanksapp.data.SharedPreferencesWrapperUserData
) {

    fun saveCredentialsForChangeOrg(
        xCode: String?,
        orgCode: String?,
        xId: String?,
        xEmail: String?,
        authType: AuthorizationType
    ) {
        sharedPreferences.savePreferencesForChangeOrg(
            xCode = xCode,
            xId = xId,
            orgCode = orgCode,
            xEmail = xEmail,
            authType = authType
        )
    }

    fun deleteCredentialsForChangeOrg(){
        sharedPreferences.deleteCredentialsForChangeOrg()
    }


    fun saveCredentials(
        authToken: String?,
        telegram: String?,
        userName: String?,
    ): Boolean {
        return sharedPreferences.savePreferences(
            authToken = authToken,
            telegram = telegram,
            username = userName,
        )
    }

    fun getXcode(): String? = sharedPreferences.xCode
    fun getOrgCode(): String? = sharedPreferences.orgCode
    fun getXEmail(): String? = sharedPreferences.xEmail
    fun getXid(): String? = sharedPreferences.xId
    fun getAuthType(): String? = sharedPreferences.authType

    fun getVkAccessToken(): String? = sharedPreferences.vkAccessToken

    fun saveVkAccessToken(accessToken: String) {
        sharedPreferences.vkAccessToken = accessToken
    }

    fun saveCurrentLogin(currentLogin: String?) {
        sharedPreferences.currentLogin = currentLogin
    }

    fun saveUsername(userName: String?) {
        sharedPreferences.username = userName
    }

    fun saveUserIsAdmin(userIsAdmin: Boolean) {
        sharedPreferences.isAdmin = userIsAdmin
    }

    fun saveUserIsSuperUser(userIsSuperUser: Boolean) {
        sharedPreferences.isSuperUser = userIsSuperUser
    }

    fun saveUserCurrentOrgId(currentOrgId: Int?) {
        sharedPreferences.currentOrgId = currentOrgId.toString()
    }

    fun saveName(name: String?) {
        name?.let { sharedPreferences.name = it }
    }

    fun saveSurname(surname: String?) {
        surname?.let { sharedPreferences.surname = it }

    }

    fun userIsAdmin(): Boolean {
        return sharedPreferences.isAdmin
    }

    fun userIsSuperUser(): Boolean {
        return sharedPreferences.isSuperUser
    }

    fun getCurrentOrg(): String? {
        return sharedPreferences.currentOrgId
    }

    fun saveUserAvatar(photo: String?){
        if(!photo.isNullOrEmpty()){
            val neededIndex = photo.indexOf("/media/", 0)
            val processedUrlOfPhoto = photo.substring(neededIndex, photo.length)
            sharedPreferences.userAvatar = processedUrlOfPhoto
        } else sharedPreferences.userAvatar = null

    }

    fun getUserAvatar() = sharedPreferences.userAvatar

    fun getUserName(): String? {
        return sharedPreferences.username
    }

    fun getCurrentLogin(): String? {
        return sharedPreferences.currentLogin
    }

    fun saveProfileId(userId: String?) {
        sharedPreferences.profileId = userId
    }

    fun getProfileId(): String? {
        return sharedPreferences.profileId;
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getToken()
    }

    fun getName(): String? {
        return sharedPreferences.name
    }

    fun getSurname(): String? {
        return sharedPreferences.surname
    }

    fun clearAuthToken() {
        return sharedPreferences.clearToken()
    }

    fun logout() {
        sharedPreferences.logout()
    }
}
