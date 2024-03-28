package com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.reviews

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentReviewsBinding
import com.teamforce.thanksapp.domain.models.benefit.BenefitItemByIdModel
import com.teamforce.thanksapp.domain.models.benefit.RatingModel
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.presentation.adapter.benefitCafe.reviews.ReviewAdapter
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.CustomDialogFragment
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.PositiveButtonClickListener
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.SettingsCustomDialogFragment
import com.teamforce.thanksapp.presentation.viewmodel.benefit.BenefitDetailViewModel
import com.teamforce.thanksapp.presentation.viewmodel.benefit.reviews.ReviewsViewModel
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.Consts.LIKE_TO_OBJECT_ID
import com.teamforce.thanksapp.utils.Consts.LIKE_TO_OBJECT_TYPE
import com.teamforce.thanksapp.utils.Extensions.PosterOverlayView
import com.teamforce.thanksapp.utils.Extensions.downloadImage
import com.teamforce.thanksapp.utils.Extensions.imagesView
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.SpannableUtils
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.dp
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReviewsFragment : BaseFragment<FragmentReviewsBinding>(FragmentReviewsBinding::inflate) {

    private val viewModel: ReviewsViewModel by activityViewModels()
    private val listAdapter: ReviewAdapter by lazy {
        ReviewAdapter(
            amIAdmin = viewModel.amIAdmin(),
            onDeleteClicked = ::onDeleteClicked,
            onImageClicked = ::onImageClicked,
            onLikeClicked = ::onLikeClicked,
            onLikeLongClicked = ::onLikeLongClicked
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.setMarketId(it.getInt(MARKET_ID))
            viewModel.setOfferId(it.getInt(OFFER_ID))
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        setupAdapter()
        setSpanText()


        viewModel.getRating()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.getReviews()
                    .collectLatest {
                        listAdapter.submitData(it)
                    }
            }
        }

        binding.createReviewBtn.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_global_createReviewFragment,
                null,
                OptionsTransaction().optionForEditProfile
            )
        }

        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        viewModel.ratingModel.observe(viewLifecycleOwner) {
            setMyReviewState(it)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {

        }
    }


    private fun onLikeClicked(reviewId: Long, positionElement: Int) {
        viewModel.pressLike(reviewId, positionElement)
        viewModel.likeResult.observe(viewLifecycleOwner) { likeResponse ->
            likeResponse?.let {
                listAdapter.updateLikesCount(it.position, it.likesAmount, it.isLiked)
            }
        }
    }

    private fun onLikeLongClicked(reviewId: Long) {
        val bundle = Bundle().apply {
            putInt(LIKE_TO_OBJECT_ID, reviewId.toInt())
            putParcelable(LIKE_TO_OBJECT_TYPE, ObjectsToLike.OFFER_REVIEW)
        }
        findNavController().navigateSafely(R.id.action_global_reactionsFragment, bundle)
    }


    private fun setMyReviewState(ratingModel: RatingModel) {
        binding.ratingBar.rating = ratingModel.avgRate
        binding.ratingTv.text = "${ratingModel.avgRate} / 5"
        if (ratingModel.review != null) {
            binding.createReviewBtn.invisible()
            binding.needBuyProduct.invisible()
            binding.viewMyReviewTv.visible()
        } else if (ratingModel.canReview) {
            binding.createReviewBtn.isEnabled = true
            binding.createReviewBtn.visible()
            binding.needBuyProduct.invisible()
            binding.viewMyReviewTv.invisible()
        } else {
            binding.createReviewBtn.isEnabled = false
            binding.viewMyReviewTv.invisible()
            binding.needBuyProduct.visible()
        }
    }

    private fun setSpanText() {
        binding.viewMyReviewTv.movementMethod = LinkMovementMethod.getInstance()
        val spannable = SpannableStringBuilder().append(
            SpannableUtils.createClickableSpannable(
                getString(R.string.reviews_has_review) + " ",
                Branding.appTheme.generalContrastColor
            ) {}
        ).append(
            SpannableUtils.createClickableSpannable(
                getString(R.string.reviews_check_review),
                Branding.appTheme.mainBrandColor
            ) {
                findNavController().navigateSafely(
                    R.id.action_global_reviewDetailFragment,
                    null,
                    OptionsTransaction().optionForEditProfile
                )
            }
        )
        binding.viewMyReviewTv.text = spannable
    }

    private fun setupAdapter() {
        binding.reviewsList.apply {
            this.adapter = listAdapter.withLoadStateHeaderAndFooter(
                header = HistoryLoadStateAdapter(),
                footer = HistoryLoadStateAdapter()
            )
        }

        listAdapter.addLoadStateListener { state ->
            binding.swipeRefreshLayout.isRefreshing = state.refresh == LoadState.Loading
            val isListEmpty =
                listAdapter.itemCount == 0 && state.source.refresh is LoadState.NotLoading
            if (isListEmpty) {
                binding.stateInfoTv.visible()
                binding.reviewsList.invisible()
            } else {
                binding.stateInfoTv.invisible()
                binding.reviewsList.visible()
            }

        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            listAdapter.refresh()
            binding.swipeRefreshLayout.isRefreshing = true
            viewModel.getRating()
        }


    }

    private fun handleTopAppBar() {
        binding.header.toolbar.title = getString(R.string.reviews_title)
        binding.header.closeBtn.visible()

        binding.header.closeBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.reviewsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val currentElevation = binding.header.toolbar.translationZ
                val targetElevation = if (recyclerView.computeVerticalScrollOffset() > 20.dp) {
                    2.dp.toFloat()
                } else {
                    0.toFloat()
                }

                if (currentElevation != targetElevation) {
                    binding.header.toolbar.translationZ = targetElevation
                }
            }
        })
    }


    override fun applyTheme() {

    }

    private fun onDeleteClicked(id: Long, position: Int) {
        showDialogAboutDeleteReview(id, position)
    }

    private fun showDialogAboutDeleteReview(reviewId: Long, position: Int) {
        val settingsDialog = SettingsCustomDialogFragment(
            positiveTextBtn = getString(R.string.review_detail_delete),
            negativeTextBtn = getString(R.string.review_detail_dialog_negative),
            title = getString(R.string.review_detail_dialog_title),
            subtitle = getString(R.string.review_detail_dialog_subtitle)
        )
        val dialogFragment = CustomDialogFragment.newInstance(settingsDialog)

        dialogFragment.setPositiveButtonClickListener(object : PositiveButtonClickListener {
            override fun onPositiveButtonClick() {
                viewModel.deleteReview(reviewId, position)
                viewModel.deletedPosition.observe(viewLifecycleOwner) {
                    listAdapter.deleteElement(it)
                    viewModel.getRating()
                }
            }
        })

        dialogFragment.show(requireActivity().supportFragmentManager, "MyDialogFragment")
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

    companion object {
        const val MARKET_ID = "reviews_market_id"
        const val OFFER_ID = "reviews_offer_id"

        @JvmStatic
        fun newInstance(marketId: Int, offerId: Long) =
            ReviewsFragment().apply {
                arguments = Bundle().apply {
                    putInt(MARKET_ID, marketId)
                    putLong(OFFER_ID, offerId)
                }
            }
    }
}