package com.teamforce.thanksapp.utils.Extensions

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import com.stfalcon.imageviewer.StfalconImageViewer
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.addBaseUrl
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.isNullOrEmptyMy
import com.teamforce.thanksapp.utils.visible
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URL

fun AvatarView.setDrawableInCircle(image: Drawable?) {
    Glide.with(this)
        .load(image)
        .centerInside()
        .apply(RequestOptions.bitmapTransform(CircleCrop()))
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun AvatarView.setAvatarInTopAppBar(image: String?) {
    if (!image.isNullOrEmptyMy()) {
        Glide.with(this)
            .load(image!!.addBaseUrl())
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.white_circle_background)
            .into(this)
    } else {
        this.setImageResource(R.drawable.white_circle_background)
        this.setColorFilter(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
    }

}

fun AvatarView.setImage(image: String) {
    Glide.with(this)
        .load("${Consts.BASE_URL}${image}".toUri())
        .apply(RequestOptions.bitmapTransform(CircleCrop()))
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun AvatarView.setImageDrawable(@DrawableRes drawable: Int) {
    Glide.with(this)
        .load(drawable)
        .apply(RequestOptions.bitmapTransform(CircleCrop()))
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.viewSingleGif(gif: String, context: Context) {
    val images = mutableListOf<String>()
    images.add(gif)
    StfalconImageViewer.Builder<String>(context, images)
    { imageView, image ->
        Glide.with(this)
            .load(image.toUri())
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.ic_anon_avatar)
            .into(imageView)
    }

        .withHiddenStatusBar(false)
        .show()
}

fun ImageView.viewSinglePhoto(image: String, context: Context) {
    val images = mutableListOf<String>()
    val fullSizeImage = image.replace("_thumb", "").replace(Consts.BASE_URL, "")
    images.add(fullSizeImage)
    StfalconImageViewer.Builder<String>(context, images)
    { imageView, image ->
        Glide.with(this)
            .load(image.addBaseUrl().toUri())
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.ic_anon_avatar)
            .into(imageView)
    }

        .withHiddenStatusBar(false)
        .show()
}

fun ShapeableImageView.viewSinglePhoto(image: String, context: Context) {
    val images = mutableListOf<String>()
    val fullSizeImage = image.replace("_thumb", "").replace(Consts.BASE_URL, "")
    images.add(fullSizeImage)
    StfalconImageViewer.Builder<String>(context, images)
    { imageView, image ->
        Glide.with(this)
            .load(image.addBaseUrl().toUri())
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.ic_anon_avatar)
            .into(imageView)
    }

        .withHiddenStatusBar(false)
        .show()
}

fun AvatarView.viewSinglePhoto(image: String, context: Context) {
    val images = mutableListOf<String>()
    val fullSizeImage = image.replace("_thumb", "").replace(Consts.BASE_URL, "")
    images.add(fullSizeImage)
    StfalconImageViewer.Builder<String>(context, images)
    { imageView, image ->
        Glide.with(this)
            .load(image.addBaseUrl().toUri())
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.ic_anon_avatar)
            .into(imageView)
    }

        .withHiddenStatusBar(false)
        .show()
}
fun ImageView.showImageWithBaseUrlWithOpportunityToDownload(
    image: String,
    context: Context,
    view: View
) {
    val images = mutableListOf<String>()
    val fullSizeImage = image.replace("_thumb", "")
    images.add(fullSizeImage)
    StfalconImageViewer.Builder<String>(context, images) { imageView, image ->
        Glide.with(this)
            .load(image)
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.ic_anon_avatar)
            .into(imageView)
    }
        .withBackgroundColor(context.getColor(R.color.black))
        .withHiddenStatusBar(false)
        .withOverlayView(view)
        .show()
}

fun ShapeableImageView.showImageWithOpportunityToDownload(
    image: String,
    context: Context,
    view: View
) {
    val images = mutableListOf<String>()
    val fullSizeImage = image.replace("_thumb", "")
    images.add(fullSizeImage)
    StfalconImageViewer.Builder<String>(context, images) { imageView, image ->
        Glide.with(this)
            .load(image.addBaseUrl().toUri())
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.ic_anon_avatar)
            .into(imageView)
    }
        .withBackgroundColor(context.getColor(R.color.black))
        .withHiddenStatusBar(false)
        .withOverlayView(view)
        .show()
}

fun AvatarView.showImageWithOpportunityToDownload(image: String, context: Context, view: View) {
    val images = mutableListOf<String>()
    val fullSizeImage = image.replace("_thumb", "")
    images.add(fullSizeImage)
    StfalconImageViewer.Builder<String>(context, images) { imageView, image ->
        Glide.with(this)
            .load(image.addBaseUrl().toUri())
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.ic_anon_avatar)
            .into(imageView)
    }
        .withBackgroundColor(context.getColor(R.color.black))
        .withHiddenStatusBar(false)
        .withOverlayView(view)
        .show()
}

fun ImageView.showImageWithOpportunityToDownload(image: String, context: Context, view: View) {
    val images = mutableListOf<String>()
    val fullSizeImage = image.replace("_thumb", "")
    images.add(fullSizeImage)
    StfalconImageViewer.Builder<String>(context, images) { imageView, image ->
        Glide.with(this)
            .load(image.addBaseUrl().toUri())
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.ic_anon_avatar)
            .into(imageView)
    }
        .withBackgroundColor(context.getColor(R.color.black))
        .withHiddenStatusBar(false)
        .withOverlayView(view)
        .show()
}

fun View.imagesView(images: List<String?>, context: Context, view: View, startPosition: Int = 0) {
    val imagesInner = mutableListOf<String>()
    images.forEach {
        if (it != "null" && it != "" && !it.isNullOrEmpty())
            imagesInner.add(it.replace("_thumb", ""))
    }
    StfalconImageViewer.Builder<String>(context, imagesInner) { imageView, imageSelected ->
        Glide.with(this)
            .load(imageSelected.addBaseUrl().toUri())
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.ic_anon_avatar)
            .into(imageView)
    }.withStartPosition(startPosition)
        .withBackgroundColor(context.getColor(R.color.black))
        .withHiddenStatusBar(false)
        .allowZooming(true)
        .withImageChangeListener {
            val counterImages = view.findViewById<TextView>(R.id.counter_images_tv)
            counterImages.text = "${it + 1}/${images.size}"
        }
        .withOverlayView(view)
        .show()
}


fun getUriFromBitmap(imageUri: String): Bitmap? {
    var image: Bitmap? = null
    try {
        val stringUrl = "${Consts.BASE_URL}${imageUri}"
        val url = URL(stringUrl)
        image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
    } catch (e: IOException) {
        System.out.println(e)
    }
    return image
}

fun downloadImage(imageUrl: String, context: Context) {
    val title = "thanksApp_${System.currentTimeMillis()}"
    val request = DownloadManager.Request(Uri.parse(imageUrl))
        .setTitle(title)
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)
        .setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "$title.jpg"
        )

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)

    Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
}

//fun downloadImage(imageURL: String, context: Context) {
//    //val dirPath = Environment.getExternalStorageDirectory().absolutePath + "/" + context.getString(R.string.app_name) + "/"
//    val dirPath =
//        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//    val imageName = "thanksApp_${System.currentTimeMillis()}.jpg"
//    val dir = File(dirPath, imageName)
//    val fileName = imageURL.substring(imageURL.lastIndexOf('/') + 1)
//    Glide.with(context)
//        .load(imageURL)
//        .into(object : CustomTarget<Drawable?>() {
//
//            override fun onResourceReady(
//                resource: Drawable,
//                transition: com.bumptech.glide.request.transition.Transition<in Drawable?>?
//            ) {
//                val bitmap = (resource as BitmapDrawable).bitmap
//                Toast.makeText(context, "Saving Image...", Toast.LENGTH_SHORT).show()
//                saveImage(bitmap, dir, fileName, context)
//            }
//
//            override fun onLoadCleared(placeholder: Drawable?) {}
//            override fun onLoadFailed(errorDrawable: Drawable?) {
//                super.onLoadFailed(errorDrawable)
//                Toast.makeText(
//                    context,
//                    "Failed to Download Image! Please try again later.",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        })
//}

private fun saveImage(
    imageBitmap: Bitmap,
    storageDir: File,
    imageFileName: String,
    context: Context
) {
    var successDirCreated = false
    if (!storageDir.exists()) {
        successDirCreated = storageDir.mkdir()
    }
    if (successDirCreated) {
        try {
            var fos: OutputStream? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver?.also { resolver ->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
//                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val imageURI: Uri? =
                        resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageURI?.let {
                        resolver.openOutputStream(it)
                    }
                }
            } else {
                val imageDirectory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val image = File(imageDirectory, imageFileName)
                fos = FileOutputStream(image)
            }
            fos?.use {
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                it.close()
                Toast.makeText(context, "Image Saved!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error while saving image!", Toast.LENGTH_SHORT)
                .show()
            e.printStackTrace()
        }
    } else {
        Toast.makeText(context, "Failed to make folder!", Toast.LENGTH_SHORT).show()
    }
}

private fun verifyPermissions(context: Context): Boolean {

    when {
        ContextCompat.checkSelfPermission(
            context,
            WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED -> {
            return true
        }
        else -> {
            return false
        }
    }
}


class PosterOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    amountImages: Int = 0,
    linkImages: List<String?>,
    positionClickedImage: Int = 0,
    private val download: (link: String) -> Unit
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var currentLinkPosition = positionClickedImage
    private var linkImagesInner = mutableListOf<String>()

    init {
        View.inflate(context, R.layout.layout_image_overlay, this)
        val toolbar = findViewById<Toolbar>(R.id.image_viewer_toolbar)
        val counterImages = findViewById<TextView>(R.id.counter_images_tv)
        linkImagesInner = checkNullImagesLink(linkImages)
        if (amountImages > 0) {
            counterImages.visible()
            counterImages.text = "${currentLinkPosition + 1}/${amountImages}"
        }
        toolbar.setOnMenuItemClickListener {
            currentLinkPosition =
                if (amountImages > 0) counterImages.text.toString()[0].toString().toInt() - 1 else 0
            when (it.itemId) {
                R.id.download -> {
                    // Прокидываем актуальную линку для скачивания фото
                    download(linkImagesInner[currentLinkPosition])
                    true
                }
                else -> true
            }
        }
    }

    private fun checkNullImagesLink(linkImages: List<String?>): MutableList<String> {
        val imagesInner = mutableListOf<String>()
        linkImages.forEach {
            if (it != "null" && it != "" && !it.isNullOrEmpty())
                imagesInner.add(it.replace("_thumb", ""))
        }
        return imagesInner
    }
}