package com.teamforce.thanksapp.utils.Extensions

import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.teamforce.thanksapp.R


fun showSnackBar(view: View, text: String?, duration: Int) {

    val snack = Snackbar.make(
        view,
        text ?: view.context.getString(R.string.smthWentWrong),
        duration
    )
    snack
        .setTextMaxLines(6)
        .setTextColor(view.context.getColor(R.color.white))
        .setAction(view.context.getString(R.string.OK)) {
            snack.dismiss()
        }
        .show()

}