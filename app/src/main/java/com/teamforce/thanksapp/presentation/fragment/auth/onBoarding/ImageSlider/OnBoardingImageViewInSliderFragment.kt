package com.teamforce.thanksapp.presentation.fragment.auth.onBoarding.ImageSlider

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentOnBoardingImageViewInSliderBinding
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.utils.navigateSafely
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingImageViewInSliderFragment: BaseFragment<FragmentOnBoardingImageViewInSliderBinding>(FragmentOnBoardingImageViewInSliderBinding::inflate){


    private var imageRes: Int? = null
    private var position: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageRes = it.getInt(IMAGE_LINK)
            position = it.getInt(IMAGE_POSITION)

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(this)
            .load(imageRes)
            .centerInside()
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.ic_anon_avatar)
            .into(binding.image)
        setActionBtn()
    }

    override fun applyTheme() {

    }

    private fun setActionBtn() {
        when (position) {
            0 -> {
                setCreateCommunityBtn()
            }
            1 -> setJoinBtn()
            else -> {}
        }
    }


    private fun setJoinBtn() {
        binding.actionBtn.setText(R.string.onboarding_join)
        binding.actionBtn.setOnClickListener {
            findNavController().navigateSafely(R.id.action_createCommunityFragment_to_joinToOrganizationBottomSheetDialogFragment)

        }
    }

    private fun setCreateCommunityBtn() {
        binding.actionBtn.setText(R.string.onboarding_create_community)
        binding.actionBtn.setOnClickListener {
            findNavController().navigateSafely(R.id.action_createCommunityFragment_to_createCommunityBottomSheetDialogFragment)
        }
    }


    companion object {
        private const val IMAGE_LINK = "image_link"
        private const val IMAGE_POSITION = "image_position"

        @JvmStatic
        fun newInstance(@DrawableRes imageRes: Int, position: Int) =
            OnBoardingImageViewInSliderFragment().apply {
                arguments = Bundle().apply {
                    putInt(IMAGE_LINK, imageRes)
                    putInt(IMAGE_POSITION, position)
                }
            }
    }
}