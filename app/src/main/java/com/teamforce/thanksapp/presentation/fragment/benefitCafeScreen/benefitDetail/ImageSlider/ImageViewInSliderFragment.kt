package com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.benefitDetail.ImageSlider

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.stfalcon.imageviewer.StfalconImageViewer
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentImageViewInSliderBinding
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.Extensions.PosterOverlayView
import com.teamforce.thanksapp.utils.Extensions.downloadImage
import com.teamforce.thanksapp.utils.addBaseUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ImageViewInSliderFragment : Fragment(R.layout.fragment_image_view_in_slider) {

    private val binding: FragmentImageViewInSliderBinding by viewBinding()

    private var imageLink: String? = null
    private var imagesLink: ArrayList<String>? = null
    private var currentPosition: Int = 0


    private var imageLoadListener: ImageLoadListener? = null

    fun setImageLoadListener(listener: ImageLoadListener?) {
        imageLoadListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageLink = it.getString(IMAGE_LINK)
            imagesLink = it.getStringArrayList(IMAGE_LINKS)
            currentPosition = it.getInt(START_POSITION)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (imagesLink != null) {
            setImageViewer()
            Log.e(TAG, "ImageLink ${imageLink}")
            Glide.with(this)
                .load(imageLink?.addBaseUrl())
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .addListener(object: RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        imageLoadListener?.onImageLoadFailed()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        imageLoadListener?.onImageLoadComplete()
                        return false
                    }
                })
                .into(binding.image)
        }

    }

    private fun setImageViewer() {
        binding.image.setOnClickListener { view ->
            view.imagesViewWithListenerCurrentPosition(
                imagesLink?.toList()!!,
                requireContext(),
                PosterOverlayView(
                    requireContext(),
                    amountImages = imagesLink?.size!!,
                    linkImages = imagesLink?.toList()!!,
                    positionClickedImage = currentPosition
                ) { photo ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        val url = "${Consts.BASE_URL}${photo.replace("_thumb", "")}"
                        downloadImageInner(url)
                    }
                }
            )

        }

        binding.nextRelative.setOnClickListener {
            val pos = currentPosition + 1
            sendCurrentPosition(pos)
        }

        binding.prevRelative.setOnClickListener {
            val pos = currentPosition - 1
            sendCurrentPosition(pos)
        }
    }

    private fun downloadImageInner(url: String){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Запрос разрешения WRITE_EXTERNAL_STORAGE для Android версии меньше 10
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Разрешение не предоставлено, запросите его у пользователя
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
            } else {
                downloadImage(url, requireContext())
            }
        } else {
            downloadImage(url, requireContext())
        }
    }

    private fun View.imagesViewWithListenerCurrentPosition(
        images: List<String?>,
        context: Context,
        view: View
    ) {
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
                .into(imageView)
        }.withStartPosition(currentPosition)
            .withBackgroundColor(context.getColor(R.color.black))
            .withHiddenStatusBar(false)
            .allowZooming(true)
            .withImageChangeListener {
                val counterImages = view.findViewById<TextView>(R.id.counter_images_tv)
                counterImages.text = "${it + 1}/${images.size}"
                currentPosition = it
            }
            .withDismissListener {
                sendCurrentPosition(currentPosition)
            }
            .withOverlayView(view)
            .show()
    }

    private fun sendCurrentPosition(pos: Int){
        try {
            setFragmentResult(
                IMAGE_SLIDER_REQUEST_KEY, bundleOf(
                    IMAGE_SLIDER_BUNDLE_KEY to pos)
            )
        }catch (e: Exception){
            Log.e(TAG, "SendCurrentPosition - ${e.localizedMessage}")
        }
    }



    companion object {

        private const val TAG = "ImageViewSlider"
        private const val IMAGE_LINK = "image_link"
        private const val IMAGE_LINKS = "images_links"
        private const val CURRENT_POSITION = "current_position"
        private const val START_POSITION = "start_position"
        private const val REQUEST_CODE_STORAGE_PERMISSION = 100

        const val IMAGE_SLIDER_REQUEST_KEY = "Image Slider Request Key"
        const val IMAGE_SLIDER_BUNDLE_KEY = "Image Slider Bundle Key"

        @JvmStatic
        fun newInstance(imageLink: String, imagesLinks: ArrayList<String>, currentPosition: Int) =
            ImageViewInSliderFragment().apply {
                arguments = Bundle().apply {
                    putString(IMAGE_LINK, imageLink)
                    putInt(START_POSITION, currentPosition)
                    putStringArrayList(IMAGE_LINKS, imagesLinks)
                }
            }
    }
}

interface ImageLoadListener {
    fun onImageLoadComplete()
    fun onImageLoadFailed()
}