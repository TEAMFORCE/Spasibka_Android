package com.teamforce.thanksapp.utils

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.*
import com.teamforce.thanksapp.R

fun Fragment.activityNavController() = requireActivity().findNavController(R.id.navContainer)

fun NavController.navigateSafely(@IdRes actionId: Int) {
    currentDestination?.getAction(actionId)?.let { navigate(actionId) }
}

fun NavController.navigateSafely(@IdRes actionId: Int, bundle: Bundle?) {
    currentDestination?.getAction(actionId)?.let { navigate(actionId, bundle) }
}

fun NavController.navigateSafely(directions: NavDirections) {
    currentDestination?.getAction(directions.actionId)?.let { navigate(directions) }
}

fun NavController.navigateSafely(@IdRes actionId: Int, bundle: Bundle?, optionsTransaction: NavOptions) {
    currentDestination?.getAction(actionId)?.let {
        navigate(actionId, bundle, optionsTransaction)
    }
}

fun NavController.navigateSafely(@IdRes actionId: Int, bundle: Bundle?, optionsTransaction: NavOptions?, navigatorExtras: Navigator.Extras?) {
    currentDestination?.getAction(actionId)?.let {
        navigate(actionId, bundle, optionsTransaction, navigatorExtras)
    }
}
