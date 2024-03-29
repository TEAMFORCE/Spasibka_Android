package com.teamforce.thanksapp.presentation.fragment.auth.onBoarding.ImageSlider

import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter


class OnBoardingImageSliderAdapter (
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    @DrawableRes val imageList: List<Int>
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = imageList.size

    override fun createFragment(position: Int): Fragment {
        val imageListInner: ArrayList<Int> = arrayListOf()
        imageList.forEach {
            imageListInner.add(it)
        }
        return OnBoardingImageViewInSliderFragment.newInstance(imageListInner[position], position)
    }
}