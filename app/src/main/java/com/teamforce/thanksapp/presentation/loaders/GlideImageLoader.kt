package com.teamforce.thanksapp.presentation.loaders

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.teamforce.thanksapp.R
import com.teamforce.photopicker.loader.ImageLoader

class GlideImageLoader: ImageLoader {

    override fun loadImage(context: Context, view: ImageView, uri: Uri) {
        Glide.with(context)
            .load(uri)
            .placeholder(R.drawable.bg_placeholder)
            .centerCrop()
            .into(view)
    }
}