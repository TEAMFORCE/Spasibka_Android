package com.teamforce.thanksapp.presentation.customViews.AvatarView

/** An attribute type of [AvatarView] to determine the shape of the image and border. */
enum class AvatarShape(public val value: Int) {
    /** Circle cropped image and border. */
    CIRCLE(0),

    /** Round rect cropped image and border. */
    ROUNDED_RECT(1),
}