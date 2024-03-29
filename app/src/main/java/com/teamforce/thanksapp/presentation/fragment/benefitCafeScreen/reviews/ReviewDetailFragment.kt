package com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.reviews

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentReviewDetailBinding
import com.teamforce.thanksapp.domain.models.benefit.BenefitItemByIdModel
import com.teamforce.thanksapp.domain.models.benefit.RatingModel
import com.teamforce.thanksapp.presentation.adapter.GeneralViewImageAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.presentation.customViews.autoFitGridLayoutManager.AutoFitGridLayoutManager
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.CustomDialogFragment
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.PositiveButtonClickListener
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.SettingsCustomDialogFragment
import com.teamforce.thanksapp.presentation.viewmodel.benefit.BenefitDetailViewModel
import com.teamforce.thanksapp.presentation.viewmodel.benefit.reviews.ReviewDetailViewModel
import com.teamforce.thanksapp.presentation.viewmodel.benefit.reviews.ReviewsViewModel
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.Extensions.PosterOverlayView
import com.teamforce.thanksapp.utils.Extensions.downloadImage
import com.teamforce.thanksapp.utils.Extensions.imagesView
import com.teamforce.thanksapp.utils.addBaseUrl
import com.teamforce.thanksapp.utils.dp
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.parseDateTimeWithBindToTimeZone
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReviewDetailFragment :
    BaseFragment<FragmentReviewDetailBinding>(FragmentReviewDetailBinding::inflate) {

    private val viewModel: ReviewDetailViewModel by viewModels()
    private val reviewsVM: ReviewsViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        handleReview()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.viewState.collectLatest { viewState ->
                    if (viewState.isLoading) {
                        // TODO Показать закгрузку
                    } else {
                        // Скрыть загрузку
                    }

                    if (viewState.error != null) {
                        Toast.makeText(requireContext(), viewState.error, Toast.LENGTH_SHORT).show()
                        viewModel.obtainError()
                    }

                    if (viewState.deleted) {
                        showSuccessfulDeletedToast()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        viewModel.obtainDeleted()
                    }

                }
            }
        }

        binding.editTv.setOnClickListener {
            findNavController().navigateSafely(R.id.action_global_createReviewFragment)
        }
        binding.deleteTv.setOnClickListener {
            showDialogAboutDeleteReview()
        }
    }

    private fun showSuccessfulDeletedToast() {
        Toast.makeText(
            requireContext(),
            getString(R.string.review_detail_successful_deleted),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleReview() {
        reviewsVM.ratingModel.observe(viewLifecycleOwner) {
            if (it?.review != null) {
                setReviewData(it.review)
            } else {
                // TODO Показать некоторую ошибку, о том, что показывать нечего
            }
        }
    }

    private fun setReviewData(review: RatingModel.Review) {
        binding.userNameLabelTv.tag = review.id
        binding.userNameLabelTv.text = viewModel.getMyName()
        binding.ratingBar.rating = review.rate.toFloat()
        binding.reviewTv.text = review.text
        binding.dateTime.text =
            parseDateTimeWithBindToTimeZone(review.createdAt, requireContext())
        setAvatar(avatar = viewModel.getAvatar(), name = viewModel.getMyName())
        initListOfPhotos(review.photos)
    }


    private fun showDialogAboutDeleteReview() {
        val settingsDialog = SettingsCustomDialogFragment(
            positiveTextBtn = getString(R.string.review_detail_delete),
            negativeTextBtn = getString(R.string.review_detail_dialog_negative),
            title = getString(R.string.review_detail_dialog_title),
            subtitle = getString(R.string.review_detail_dialog_subtitle)
        )
        val dialogFragment = CustomDialogFragment.newInstance(settingsDialog)

        dialogFragment.setPositiveButtonClickListener(object : PositiveButtonClickListener {
            override fun onPositiveButtonClick() {
                viewModel.deleteReview(reviewsVM.marketId, binding.userNameLabelTv.tag as Long)
            }
        })

        dialogFragment.show(requireActivity().supportFragmentManager, "MyDialogFragment")
    }

    private fun handleTopAppBar() {
        binding.header.toolbar.title = requireContext().getString(R.string.review_detail_title)
        binding.header.closeBtn.visible()

        binding.header.closeBtn.setOnClickListener {
            findNavController().navigateUp()
        }

    }

    private fun initListOfPhotos(photos: List<String?>) {
        var listAdapter: GeneralViewImageAdapter? = null
        if (photos.isNotEmpty()) {
            listAdapter = GeneralViewImageAdapter(photos = photos, ::onImageClicked)
        } else {
            binding.imageList.invisible()
        }

        binding.imageList.adapter = listAdapter
        binding.imageList.layoutManager = AutoFitGridLayoutManager(binding.root.context, 100.dp)

    }

    private fun setAvatar(avatar: String?, name: String?) {
        binding.userAvatar.setAvatarImageOrInitials(avatar, name)
    }

    private fun onImageClicked(listOfPhotos: List<String?>, clickedView: View, position: Int) {
        clickedView.imagesView(
            listOfPhotos,
            requireContext(),
            startPosition = position,
            view = PosterOverlayView(
                requireContext(),
                amountImages = listOfPhotos.size,
                linkImages = listOfPhotos,
                positionClickedImage = position
            ) { photo ->
                lifecycleScope.launch(Dispatchers.Main) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val url = "${Consts.BASE_URL}${photo.replace("_thumb", "")}"
                        downloadImage(url, requireContext())
                    } else {
                        when {
                            ContextCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                val url = "${Consts.BASE_URL}${photo.replace("_thumb", "")}"
                                downloadImage(url, requireContext())
                            }

                            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                                showRequestPermissionRational()
                            }

                            else -> {
                                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                        }
                    }
                }
            }
        )

    }


    private fun showDialogAboutPermissions() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(resources.getString(R.string.explainingAboutPermissions))

            .setNegativeButton(resources.getString(R.string.close)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(resources.getString(R.string.settings)) { dialog, which ->
                dialog.cancel()
                val reqIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .apply {
                        val uri = Uri.fromParts("package", "com.teamforce.thanksapp", null)
                        data = uri
                    }
                startActivity(reqIntent)
                // Почему то повторно не запрашивается разрешение
                // requestPermissions()
            }
            .show()
    }

    private fun showRequestPermissionRational() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(resources.getString(R.string.explainingAboutPermissionsRational))

            .setNegativeButton(resources.getString(R.string.close)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(resources.getString(R.string.good)) { dialog, _ ->
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                dialog.cancel()
            }
            .show()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                showDialogAboutPermissions()
            }
        }


    override fun applyTheme() {

    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReviewDetailFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}