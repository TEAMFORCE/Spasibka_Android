package com.teamforce.thanksapp.utils

import com.teamforce.thanksapp.BuildConfig
import com.teamforce.thanksapp.presentation.activity.MainActivity

object Consts {
    const val PACKAGE_NAME = "com.teamforce.thanksapp"
    const val AMOUNT_THANKS = "amount_thanks"
    const val RECEIVER_TG = "receiverTG"
    const val RECEIVER_NAME = "receiverName"
    const val RECEIVER_SURNAME = "receiverSurname"
    const val BUNDLE_TG_OR_EMAIL = "telegramOrEmail"
    // TODO Вынести в local properties
    const val LINK_TO_BOT = "https://t.me/DigitalRefBot"
    const val LINK_TO_BOT_Name = "LinkToBot"
    const val USER_ID = "userId"
    const val TRANSACTION_ID = "transactionId"
    const val LIKE_TO_OBJECT_ID = "likeToObjectId"
    const val LIKE_TO_OBJECT_TYPE = "likeToObjectType"
    const val OBJECTS_COMMENT_ID = "ObjectsCommentId"
    const val OBJECTS_COMMENT_TYPE = "ObjectsCommentType"
    const val PROFILE_DATA = "profileData"
    const val USERNAME = "username"
    const val ALL_DATA = "all_data"
    const val UNAUTHORIZED = "403"
    const val RETURN_AFTER_FINISHIED = "isNeedReturnAfterFinished"
    const val FROM_MAIN_FLOW = "fromMainFlow"
    const val OTHER_CATEGORY_ID = -1L

    // Аргументы в расширенную инфу о траназкции
    const val DATE_TRANSACTION = "date-transaction"
    const val STATUS_TRANSACTION = "status-transaction"
    const val WE_REFUSED_YOUR_OPERATION = "we-refused_your_operation"
    const val AVATAR_USER = "user-avatar"
    const val NEEDED_PAGE_POSITION = "needed-page-position"
    const val MESSSAGE = "message"

    // Locale constants
    const val LOCALE_PREF = "locale_preferences"
    const val CURRENT_LANGUAGE = "current_language"


    var BASE_URL: String = BuildConfig.URL_PORT
    var APPLICATION_ID: String = BuildConfig.APPLICATION_ID
    var FILE_PROVIDER: String = "${APPLICATION_ID}.provider"

    const val PAGE_SIZE = 15
    const val PREFETCH_DISTANCE = 10
    const val PAGE_SIZE_30 = 30
    const val PAGE_SIZE_3 = 3

    const val CURRENT_POSITION_PAGE_IN_VIEW_PAGER = "currentPositionOfPageInViewPager"

    // Onboarding
    const val COMMUNITY_ID = "community_id"

    // VK SDK
    val VK_ACCOUNT_MANAGER_ID = "${APPLICATION_ID}.account"

    val PACKAGE_SHEME = "package"
}