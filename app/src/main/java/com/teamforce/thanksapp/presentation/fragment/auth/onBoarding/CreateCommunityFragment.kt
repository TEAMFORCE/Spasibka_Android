package com.teamforce.thanksapp.presentation.fragment.auth.onBoarding

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentCreateCommunityBinding
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.auth.onBoarding.ImageSlider.OnBoardingImageSliderAdapter
import com.teamforce.thanksapp.presentation.fragment.auth.onBoarding.ImageSlider.ZoomOutPageTransformer
import com.teamforce.thanksapp.presentation.viewmodel.onboarding.OnboardingViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.branding.Branding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateCommunityFragment : BaseFragment<FragmentCreateCommunityBinding>(FragmentCreateCommunityBinding::inflate) {

    private val sharedViewModel: OnboardingViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.error.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        setSliderOnImage()
    }

    private fun setSliderOnImage() {
        val images = listOf(
            R.drawable.onboarding_picture,
            R.drawable.ic_successful_person,
            //   R.drawable.ic_greet_picture_svg
        )

        val imagePager = OnBoardingImageSliderAdapter(
            childFragmentManager,
            viewLifecycleOwner.lifecycle,
            images
        )
        binding.imageSliderVp.apply {
            setPageTransformer(ZoomOutPageTransformer())
            adapter = imagePager
            binding.dotsIndicator.attachTo(this)
        }

    }

    override fun applyTheme() {
        binding.dotsIndicator.setDotIndicatorColor(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
        binding.dotsIndicator.setStrokeDotsIndicatorColor(Color.parseColor(Branding.brand?.colorsJson?.secondaryBrandColor))
    }


    companion object {

        @JvmStatic
        fun newInstance() =
            CreateCommunityFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}