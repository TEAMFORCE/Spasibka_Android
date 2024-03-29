package com.teamforce.thanksapp.utils.glide

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.google.android.material.imageview.ShapeableImageView
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.utils.addBaseUrl
import com.teamforce.thanksapp.utils.fastBlur


private fun initShimmer(context: Context): ShimmerDrawable {
    val shimmer = Shimmer.ColorHighlightBuilder()
        .setBaseColor(ContextCompat.getColor(context, R.color.color_grey2))
        .setHighlightAlpha(0.7f)
        .setHighlightColor(ContextCompat.getColor(context, R.color.color_grey))
        .setDuration(1000)
        .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
        .setAutoStart(true)
        .build()
    //create ShimmerDrawable()
    val shimmerDrawable = ShimmerDrawable()
    shimmerDrawable.setShimmer(shimmer)
    return shimmerDrawable
}

fun ImageView.setImageWithGraySale(
    photo: String? = "",
    @DrawableRes errorDrawable: Int = R.drawable.ic_app_logo
) {
    Glide.with(this)
        .load(photo?.addBaseUrl() ?: "")
        .centerCrop()
        .error(errorDrawable)
        .transition(DrawableTransitionOptions.withCrossFade())
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                val resource = AppCompatResources.getDrawable(context, errorDrawable)
                resource?.let {
                    animateToGrayscale(this@setImageWithGraySale, it)
                }
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                resource?.let {
                    animateToGrayscale(this@setImageWithGraySale, it)
                }
                return true
            }

        })
        .into(this)
}

 fun ShapeableImageView.animateToColor() {
    val originalDrawable = this.background

    originalDrawable?.let {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 500
        valueAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(value)
            val colorfulDrawable =
                originalDrawable.mutate().constantState?.newDrawable() ?: originalDrawable
            colorfulDrawable.colorFilter = ColorMatrixColorFilter(colorMatrix)
            this.background = colorfulDrawable
        }

        valueAnimator.start()
    }

}

private fun animateToGrayscale(targetView: ImageView, originalDrawable: Drawable) {
    val valueAnimator = ValueAnimator.ofFloat(1f, 0f)
    valueAnimator.duration = 300
    valueAnimator.addUpdateListener { animation ->
        val value = animation.animatedValue as Float
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(value)
        val grayscaleDrawable =
            originalDrawable.mutate().constantState?.newDrawable() ?: originalDrawable
        grayscaleDrawable.colorFilter = ColorMatrixColorFilter(colorMatrix)
        targetView.setImageDrawable(grayscaleDrawable)
    }

    valueAnimator.start()
}

fun ImageView.setImageAward(photo: String? = "") {
    Glide.with(this)
        .load(photo?.addBaseUrl() ?: "")
        .centerCrop()
        .error(R.drawable.default_award)
        .transition(DrawableTransitionOptions.withCrossFade())
        .placeholder(initShimmer(this.context))
        .into(this)
}

fun ImageView.setImage(
    photo: String? = "",
    @DrawableRes errorDrawable: Int = R.drawable.ic_app_logo
) {
    Glide.with(this)
        .load(photo?.addBaseUrl() ?: "")
        .centerCrop()
        .error(errorDrawable)
        .transition(DrawableTransitionOptions.withCrossFade())
        .placeholder(initShimmer(this.context))
        .into(this)
}

fun ImageView.setImageRounded(photo: String? = "") {
    Glide.with(this)
        .load(photo?.addBaseUrl())
        .circleCrop()
        .error(R.drawable.ic_logo)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.setSmallLogo(photo: String = "") {
    Glide.with(this)
        .load(photo.addBaseUrl())
        .centerInside()
        .error(R.drawable.ic_logo)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.setImageFromStorage(photo: String = "") {
    Glide.with(this)
        .load(photo)
        .centerCrop()
        .error(R.drawable.ic_app_logo)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.setImageFromStorage(photo: String = "", getBmp: (bitmap: Bitmap) -> Unit) {
    Glide.with(this)
        .load(photo)
        .centerCrop()
        .error(R.drawable.ic_app_logo)
        .listener(object : RequestListener<Drawable?> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable?>?,
                isFirstResource: Boolean
            ): Boolean {
                // Вернуть false, чтобы Glide продолжил обработку ошибки.
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable?>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                val bmp = (resource as? BitmapDrawable)?.bitmap
                if (bmp != null) {
                    getBmp.invoke(bmp)
                }
                return false
            }
        })
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.setGif(gif: String) {
    Glide.with(this)
        .load(gif)
        .centerCrop()
        .error(R.drawable.ic_app_logo)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.setPlaceholderImage() {
    Glide.with(this)
        .load(R.drawable.ic_logo)
        .centerInside()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

