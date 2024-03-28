package com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.benefitDetail.ImageSlider

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter


class ImageSliderAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val imageList: List<String>
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private var imageLoadListener: ImageLoadListener? = null

    fun setImageLoadListener(listener: ImageLoadListener) {
        imageLoadListener = listener
    }

    override fun getItemCount(): Int = imageList.size

    override fun createFragment(position: Int): Fragment {
        val imageListInner: ArrayList<String> = arrayListOf()
        imageList.forEach {
            imageListInner.add(it)
        }
        val fragment = ImageViewInSliderFragment.newInstance(
            imageListInner[position],
            imageListInner,
            position
        ).apply {
            setImageLoadListener(imageLoadListener)
        }
        return fragment
    }
}