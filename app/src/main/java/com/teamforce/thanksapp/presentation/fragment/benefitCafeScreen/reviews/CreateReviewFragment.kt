package com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.reviews

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.teamforce.photopicker.PhotoPickerFragment
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentCreateReviewBinding
import com.teamforce.thanksapp.domain.models.benefit.BenefitItemByIdModel
import com.teamforce.thanksapp.domain.models.benefit.RatingModel
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.customViews.photoSelectionView.PhotoSelectionListener
import com.teamforce.thanksapp.presentation.viewmodel.benefit.BenefitDetailViewModel
import com.teamforce.thanksapp.presentation.viewmodel.benefit.reviews.CreateReviewViewModel
import com.teamforce.thanksapp.presentation.viewmodel.benefit.reviews.ReviewsViewModel
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.parceleable
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber


const val ARG_PARAM1 = "param1"
const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class CreateReviewFragment :
    BaseFragment<FragmentCreateReviewBinding>(FragmentCreateReviewBinding::inflate),
    PhotoPickerFragment.Callback {

    private val viewModel: CreateReviewViewModel by viewModels()
    private val reviewsVM: ReviewsViewModel by activityViewModels()



    private val marketId: Int? by lazy { reviewsVM.marketId }
    private val offerId: Long? by lazy { reviewsVM.offerId.toLong() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleRatingBarTv()
        onAttachImagePicked()
        binding.closeBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        reviewsVM.ratingModel.observe(viewLifecycleOwner) {
            viewModel.obtainReview(it?.review)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.viewState.collectLatest { viewState ->
                    binding.progressBar.visibility =
                        if (viewState.isLoading) View.VISIBLE else View.GONE

                    if (viewState.error != null) {
                        Toast.makeText(requireContext(), viewState.error, Toast.LENGTH_SHORT).show()
                        viewModel.obtainError()
                    }
                    if (viewState.created) {
                        showSuccessfulCreatedToast()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        viewModel.obtainCreated()
                    }
                    if (viewState.edited) {
                        showSuccessfulEditedToast()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        viewModel.obtainEdited()
                    }
                    if (viewState.review != null) {
                        binding.screenTitleTv.text = getString(R.string.create_review_title_edit)
                        binding.createBtn.text = getString(R.string.create_review_title_edit)
                        binding.createBtn.setOnClickListener {
                            editReview(viewState.review.id)
                        }
                        setInitValues(viewState.review)
                    } else {
                        binding.screenTitleTv.text = getString(R.string.create_review_title_create)
                        binding.createBtn.text = getString(R.string.create_review_title_create)
                        binding.createBtn.setOnClickListener {
                            createReview()
                        }
                    }
                }
            }
        }
    }

    private fun setInitValues(review: RatingModel.Review) {
        binding.ratingBar.rating = review.rate.toFloat()
        binding.descriptionEt.setText(review.text)
        binding.photoSelection.updateRecyclerViewStringList(review.photos)
    }

    private fun showSuccessfulCreatedToast() {
        reviewsVM.getRating()
        Toast.makeText(
            requireContext(),
            getString(R.string.create_review_successful_created),
            Toast.LENGTH_SHORT
        ).show()
    }
    private fun showSuccessfulEditedToast() {
        reviewsVM.getRating()
        Toast.makeText(
            requireContext(),
            getString(R.string.create_review_successful_edited),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun createReview() {
        if (marketId != null && offerId != null) {
            viewModel.createReview(
                marketId = marketId!!,
                offerId = offerId!!,
                text = binding.descriptionEt.text.toString(),
                rate = binding.ratingBar.rating.toInt(),
                imagesFilePart = binding.photoSelection.getPhotos()
            )
        } else {
            viewModel.obtainError(getString(R.string.create_review_undefined_error))
            Timber.tag(TAG).e("MarketId -  " + marketId + " or OfferId - " + offerId + " is null")
        }
    }

    private fun editReview(reviewId: Long) {
        if (marketId != null) {
            viewModel.updateReview(
                marketId = marketId!!,
                reviewId = reviewId,
                text = binding.descriptionEt.text.toString(),
                rate = binding.ratingBar.rating.toInt(),
                imagesFilePart = binding.photoSelection.getPhotos(),
                fileList = binding.photoSelection.getFileList()
            )
        } else {
            viewModel.obtainError(getString(R.string.create_review_undefined_error))
            Timber.tag(TAG).e("MarketId -  " + marketId + " or ReviewId - " + reviewId + " is null")
        }
    }

    private fun onAttachImagePicked() {
        binding.photoSelection.photoSelectionListener = object : PhotoSelectionListener {
            override fun onAttachImageClicked(maxSelection: Int, alreadySelected: Int) {
                openPicker(maxSelection, alreadySelected)
            }
        }
    }

    private fun openPicker(maxSelection: Int, alreadySelected: Int) {
        PhotoPickerFragment.newInstance(
            multiple = true,
            allowCamera = true,
            maxSelection = maxSelection,
            alreadySelected = alreadySelected,
            mainColor = Branding.brand.colorsJson.mainBrandColor
        ).show(childFragmentManager, "photoPicker")
    }

    private fun handleRatingBarTv() {
        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rate, b ->
            binding.ratingTv.text = when (rate) {
                1f -> getString(R.string.create_review_terrible)
                2f -> getString(R.string.create_review_bad)
                3f -> getString(R.string.create_review_normal)
                4f -> getString(R.string.create_review_good)
                5f -> getString(R.string.create_review_great)
                else -> ""
            }
        }
    }

    override fun applyTheme() {
    }

    companion object {

        const val TAG = "CreateReviewFragment"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateReviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onImagesPicked(photos: ArrayList<Uri>) {
        binding.photoSelection.updateRecyclerView(photos)
    }
}