package com.teamforce.thanksapp.utils

import androidx.navigation.NavOptions
import com.teamforce.thanksapp.R

class OptionsTransaction {
    val optionForProfileFragment = NavOptions.Builder()
        .setLaunchSingleTop(true)
        .setEnterAnim(androidx.transition.R.anim.abc_grow_fade_in_from_bottom)
        .setExitAnim(androidx.transition.R.anim.abc_shrink_fade_out_from_bottom)
        .build()

    val optionForBackChangeOrg = NavOptions.Builder()
        .setLaunchSingleTop(true)
        .setPopUpTo(R.id.settingsFragment, true)
        .setEnterAnim(androidx.transition.R.anim.abc_grow_fade_in_from_bottom)
        .setExitAnim(androidx.transition.R.anim.abc_shrink_fade_out_from_bottom)
        .build()

    // Если вернуть .setPopUpTo(R.id.main_graph, true),
    // то на любом фргаменте 1 уровня при back будет выход из приложения
    val optionForTransaction = NavOptions.Builder()
        .setLaunchSingleTop(true)
        .setEnterAnim(androidx.transition.R.anim.abc_grow_fade_in_from_bottom)
        .setExitAnim(androidx.transition.R.anim.abc_shrink_fade_out_from_bottom)
        .build()

    val optionForResultOut = NavOptions.Builder()
        .setLaunchSingleTop(true)
        .setPopUpTo(R.id.transaction_graph, true)
        .setEnterAnim(androidx.transition.R.anim.abc_grow_fade_in_from_bottom)
        .setExitAnim(androidx.transition.R.anim.abc_shrink_fade_out_from_bottom)
        .build()

    val optionForTransactionWithSaveBackStack = NavOptions.Builder()
        .setEnterAnim(androidx.transition.R.anim.abc_grow_fade_in_from_bottom)
        .setExitAnim(androidx.transition.R.anim.abc_shrink_fade_out_from_bottom)
        .build()


    val optionForResultTransaction = NavOptions.Builder()
        .setLaunchSingleTop(true)
        .setPopUpTo(R.id.transactionFragmentSecond, true)
        .build()

    val optionForAdditionalInfoFeedFragment = NavOptions.Builder()
        .setEnterAnim(androidx.transition.R.anim.abc_grow_fade_in_from_bottom)
        .setExitAnim(androidx.transition.R.anim.abc_shrink_fade_out_from_bottom)
        .build()

    val optionForEditProfile = NavOptions.Builder()
        .setLaunchSingleTop(true)
        .setEnterAnim(R.anim.slide_in_right)
        .setExitAnim(R.anim.slide_out_left)
        .setPopEnterAnim(R.anim.slide_in_left)
        .setPopExitAnim(R.anim.slide_out_right)
        .build()


    val optionForLastScreenOnBoarding = NavOptions.Builder()
        .setPopUpTo(R.id.sign_graph, true)
        .setEnterAnim(R.anim.slide_in_right)
        .setExitAnim(R.anim.slide_out_left)
        .setPopEnterAnim(R.anim.slide_in_left)
        .setPopExitAnim(R.anim.slide_out_right)
        .build()

    val optionForLastScreenOnBoardingFromMainFlow = NavOptions.Builder()
        .setPopUpTo(R.id.main_graph, true)
        .setEnterAnim(R.anim.slide_in_right)
        .setExitAnim(R.anim.slide_out_left)
        .setPopEnterAnim(R.anim.slide_in_left)
        .setPopExitAnim(R.anim.slide_out_right)
        .build()
}