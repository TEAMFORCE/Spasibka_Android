package com.teamforce.thanksapp.presentation.fragment.challenges

import android.graphics.Color
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.platform.Hold
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentDetailsMainChallengeBinding
import com.teamforce.thanksapp.domain.models.challenge.ChallengeType
import com.teamforce.thanksapp.domain.models.challenge.SectionOfChallenge
import com.teamforce.thanksapp.domain.models.challenge.SectionsOfChallengeType
import com.teamforce.thanksapp.domain.models.general.ObjectsComment
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.model.domain.ChallengeModelById
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.benefitDetail.ImageSlider.ImageLoadListener
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.benefitDetail.ImageSlider.ImageSliderAdapter
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.benefitDetail.ImageSlider.ImageViewInSliderFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts.CHALLENGER_ID
import com.teamforce.thanksapp.presentation.fragment.challenges.updateChallenge.EditChallengeFragment.Companion.CHALLENGE_FOR_CHANGE_KEY
import com.teamforce.thanksapp.presentation.fragment.challenges.updateChallenge.EditChallengeFragment.Companion.EDIT_CHALLENGE_BUNDLE_KEY
import com.teamforce.thanksapp.presentation.fragment.challenges.updateChallenge.EditChallengeFragment.Companion.EDIT_CHALLENGE_REQUEST_KEY
import com.teamforce.thanksapp.presentation.fragment.challenges.updateChallenge.EditChallengeFragment.Companion.EditChallengeResponse
import com.teamforce.thanksapp.presentation.fragment.reactions.CommentsBottomSheetFragment
import com.teamforce.thanksapp.presentation.viewmodel.challenge.ChallengesViewModel
import com.teamforce.thanksapp.presentation.viewmodel.challenge.DetailsMainChallengeViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Extensions.enableOnClickableLinks
import com.teamforce.thanksapp.utils.Extensions.setNameOrTg
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.branding.Cases
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


@AndroidEntryPoint
class DetailsMainChallengeFragment :
    BaseFragment<FragmentDetailsMainChallengeBinding>(FragmentDetailsMainChallengeBinding::inflate),
    ImageLoadListener {


    private val viewModel: DetailsMainChallengeViewModel by activityViewModels()

    private val challengeListViewModel: ChallengesViewModel by activityViewModels()


    private var dataOfChallenge: ChallengeModelById? = null
    private var challengeId: Int? = null
    private var initSectionPage: SectionsOfChallengeType? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            challengeId = it.getInt(CHALLENGER_ID)
            initSectionPage = it.parceleable(INIT_SECTION_PAGE)
        }

        setFragmentResultListener(EDIT_CHALLENGE_REQUEST_KEY) { requestKey, bundle ->
            val result = bundle.getParcelableExt(
                EDIT_CHALLENGE_BUNDLE_KEY,
                EditChallengeResponse::class.java
            )
            result?.let {
                if (result.challengeWasChanged) {
                    loadDataFromDb()
                } else if (result.challengeWasDeleted) {
                    challengeListViewModel.setNeedUpdateListValue(true)
                    activity?.onBackPressedDispatcher?.onBackPressed()
                    activity?.onBackPressedDispatcher?.onBackPressed()
                }
            }
        }

        setFragmentResultListener(CommentsBottomSheetFragment.COMMENTS_REQUEST_KEY) { requestKey, bundle ->
            val result = bundle.getInt(
                CommentsBottomSheetFragment.COMMENTS_AMOUNT_BUNDLE_KEY
            )
            if (result != 0) binding.reactionGroup.setCommentsAmount(result)
        }
        transparentStatusBar()
    }

    private fun transparentStatusBar() {
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    private fun readingTheCurrentPositionOfPhoto() {
        childFragmentManager.setFragmentResultListener(
            ImageViewInSliderFragment.IMAGE_SLIDER_REQUEST_KEY,
            viewLifecycleOwner
        ) { requestKey, bundle ->
            val result = bundle.getInt(
                ImageViewInSliderFragment.IMAGE_SLIDER_BUNDLE_KEY
            )
            binding.imageSliderVp.setCurrentItem(result, false)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      //  postponeEnterTransition()
        handleTopAppBar()
        readingTheCurrentPositionOfPhoto()
        loadDataFromDb()
        listenerAppBar()
        checkReportSharedPref()
        handleReactions()
        handleInitChallengeSection()
        binding.sendReportBtn.setOnClickListener {
            val bundle = Bundle()
            challengeId?.let { it1 -> bundle.putInt(CHALLENGER_ID, it1) }
            it.findNavController().navigateSafely(
                R.id.action_global_createReportFragment,
                bundle,
                OptionsTransaction().optionForEditProfile
            )
        }


        binding.resultBtn.setOnClickListener {
            openResults()
        }


        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.viewState.collectLatest { state ->
                    if(state.isLoading){
                        binding.shimmerLayout.startShimmer()
                        binding.shimmerLayout.visible()
                        binding.scrollView.invisible()
                    }else{
                        binding.shimmerLayout.stopShimmer()
                        binding.scrollView.visible()
                        binding.shimmerLayout.invisible()
                    }
                    if(state.challenge!= null){
                        dataOfChallenge = state.challenge
                        state.challenge.let {
                            setReactionsData(it)
                            setDetailChallengeData(it)
                            showEditBtnOrHide(it)
                            showShareBtnOrHide(it)
                            setImages(it.photos)
                        }
//                        binding.shimmerLayout.stopShimmer()
//                        binding.shimmerLayout.invisible()
//                        binding.content.visible()
//                        binding.appBarLayout.visible()
                    }
                    if(state.error != null){
                        Toast.makeText(requireContext(),state.error.status, Toast.LENGTH_LONG).show()
                        viewModel.obtainError(null)
                    }

                    if(state.likeResult != null){
                        binding.reactionGroup.setLikesAmount(state.likeResult.likesAmount)
                        binding.reactionGroup.setIsLiked(state.likeResult.isLiked)
                    }
                    if(state.challengeNotFound){
                        showChallengeNotFoundDialog()
                        viewModel.obtainChallengeNotFound()
                    }
                }
            }
        }
    }

    private fun showChallengeNotFoundDialog() {
        val message = getString(R.string.detail_challenge_not_found)
        val buttonText = getString(R.string.detail_challenge_return_back)

        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_ThanksApp_Dialog_Simple)
            .setMessage(message)
            .setPositiveButton(buttonText) { dialog, which ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                findNavController().navigateUp()
            }
            .show()
    }


    private fun openResults(sectionOfChallenge: SectionsOfChallengeType? = null) {
        val bundle = Bundle().apply { putParcelable(INIT_SECTION_PAGE, sectionOfChallenge) }
        findNavController().navigateSafely(
            R.id.action_global_resultsChallengeFragment,
            bundle,
            OptionsTransaction().optionForEditProfile
        )
    }

    private fun handleInitChallengeSection() {
        when (initSectionPage) {
            SectionsOfChallengeType.COMMENTS -> openComments()
            SectionsOfChallengeType.WINNERS, SectionsOfChallengeType.CONTENDERS, SectionsOfChallengeType.MY_RESULT -> {
                openResults(initSectionPage)
            }

            SectionsOfChallengeType.REACTIONS -> openReactions()
            SectionsOfChallengeType.DEPENDENCIES, SectionsOfChallengeType.DETAIL, null -> {
                // do nothing
            }
        }
        initSectionPage = null
    }

    private fun showEditBtnOrHide(challengeModelById: ChallengeModelById) {
        if (viewModel.amIAdmin() || challengeModelById.amICreator) {
            binding.ivEdit.visible()
            binding.ivEdit.setOnClickListener {
                val bundle =
                    Bundle().apply { putParcelable(CHALLENGE_FOR_CHANGE_KEY, dataOfChallenge) }
                findNavController().navigate(
                    R.id.action_detailsMainChallengeFragment_to_editChallengeFragment,
                    bundle
                )
            }
        }
    }

    private fun showShareBtnOrHide(challengeModelById: ChallengeModelById) {
        if (challengeModelById.linkToShare.isNotNullOrEmptyMy()) {
            binding.ivShare.visible()
            binding.ivShare.setOnClickListener {
                copyTextToClipboardViaIntent(
                    challengeModelById.linkToShare!!,
                    requireContext(),
                    getString(R.string.detail_challenge_subtitle)
                )
            }
        }
    }

    override fun applyTheme() {
        binding.collapsingToolbar.setBackgroundColor(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
        binding.dotsIndicator.dotsColor =
            Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor)
        binding.dotsIndicator.selectedDotColor =
            Color.parseColor(Branding.brand.colorsJson.mainBrandColor)
    }

    override fun onResume() {
        super.onResume()
        transparentStatusBar()
    }


    private fun loadDataFromDb() {
        challengeId?.let { challengeId ->
            viewModel.loadChallenge(challengeId)
            viewModel.loadChallengeResult(challengeId)
            binding.shimmerLayout.startShimmer()
        }
    }

    private fun listenerAppBar() {
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val isAppBarCollapsed = verticalOffset <= -appBarLayout.totalScrollRange
            if (isAppBarCollapsed) {
                binding.dotsIndicator.invisible()
                binding.reactionGroup.invisible()
            } else {
                if (dataOfChallenge?.photos?.size != null &&
                    dataOfChallenge?.photos?.size!! > 1
                ) binding.dotsIndicator.visible()
                binding.reactionGroup.visible()
            }

        }
    }

    private fun setReactionsData(challenge: ChallengeModelById) {
        binding.reactionGroup.setCommentsAmount(challenge.commentsAmount)
    }

    private fun setImages(photos: List<String>?) {
        if (!photos.isNullOrEmpty()) {
            binding.imageSliderVp.visibility = View.VISIBLE
            setSliderOnImage(photos)
        } else {
            binding.imageSliderVp.visibility = View.GONE
            //startPostponedEnterTransition()
        }
    }

    private fun setSliderOnImage(images: List<String>) {
        if (images.size > 1) {
            binding.dotsIndicator.visible()
        } else {
            binding.dotsIndicator.invisible()
        }
        val imagePager = ImageSliderAdapter(
            childFragmentManager,
            viewLifecycleOwner.lifecycle,
            images
        ).apply {
            setImageLoadListener(this@DetailsMainChallengeFragment)
        }
        binding.imageSliderVp.apply {
            adapter = imagePager
            binding.dotsIndicator.attachTo(this)
        }
    }

    private fun transactionToProfileOfCreator(creatorId: Int, view: View) {
        val bundle = Bundle()
        bundle.putInt(Consts.USER_ID, creatorId)
        view.findNavController().navigateSafely(
            R.id.action_global_someonesProfileFragment,
            bundle,
            OptionsTransaction().optionForTransactionWithSaveBackStack
        )
    }


    private fun checkReportSharedPref() {
        val sharedPref = requireContext().getSharedPreferences("report${challengeId}", 0)
        if (!sharedPref.getString("commentReport${challengeId}", "").isNullOrEmpty() ||
            !sharedPref.getString("imageReport${challengeId}", "").isNullOrEmpty()
        ) {
            binding.sendReportBtn.text = requireContext().getString(R.string.draft)
        } else {
            binding.sendReportBtn.text = requireContext().getString(R.string.sendReport)
        }
    }

    private fun setDetailChallengeData(challenge: ChallengeModelById) {
        setSubtitle(challenge)
        binding.nameChallenge.text = challenge.name
        binding.descriptionChallenge.enableOnClickableLinks()
        binding.descriptionChallenge.text = challenge.description
        setTypeChallenge(challenge)
        binding.prizeFundValue.text =
            "${challenge.fund} ${
                Branding.declineCurrency(
                    challenge.fund,
                    Cases.GENITIVE
                )
            }"
        binding.dateEndValue.text =
            formatDateFromTimestamp(
                challenge.endAt
            )

        setDateStart(challenge)
        handleTheOrganizer(challenge)

        binding.prizePoolValue.text =
            String.format(
                requireContext()
                    .getString(R.string.occupiedPrizePool),
                challenge.winnersCount,
                challenge.awardees
            )

        enableOrDisableSentReportButton(challenge)
    }

    private fun setSubtitle(challenge: ChallengeModelById) {
        challenge.groups.firstOrNull()?.let {
            binding.subtitleTv.text = HtmlCompat.fromHtml(
                String.format(
                    getString(R.string.detail_challenge_subtitle_with_chain),
                    it.groupName
                ), HtmlCompat.FROM_HTML_MODE_COMPACT
            )
        }
    }

    private fun setTypeChallenge(challenge: ChallengeModelById) {
        when (challenge.typeOfChallenge) {
            ChallengeType.DEFAULT -> {
                binding.challengeTypeTv.text = getString(R.string.detail_challenge_type_default)
            }

            ChallengeType.VOTING -> {
                binding.challengeTypeTv.text = getString(R.string.detail_challenge_type_voting)
            }
        }
    }

    private fun handleTheOrganizer(challenge: ChallengeModelById) {
        if (challenge.fromOrganization) {
            binding.userNameLabelTv.text = challenge.organizationName
            binding.userAvatar.avatarInitials = challenge.organizationName
            binding.userTgName.invisible()
        } else {
            binding.userTgName.visible()
            setTheOrganizerAsAPersonalAccount(challenge)
        }

    }

    private fun setTheOrganizerAsAPersonalAccount(challenge: ChallengeModelById) {
        binding.userAvatar.setAvatarImageOrInitials(challenge.creatorPhoto, "${challenge.creatorName} ${challenge.creatorSurname}")

        binding.userTgName.setNameOrTg(
            name = challenge.creatorName,
            surname = challenge.creatorSurname,
            tgName = challenge.creatorTgName
        )


        binding.challengeOrganizer.setOnClickListener { view ->
            transactionToProfileOfCreator(challenge.creatorId, view)
        }
    }

    private fun setDateStart(challenge: ChallengeModelById) {
        if (challenge.startAt.isNullOrEmptyMy()) binding.dateStartCardView.invisible()
        else {
            binding.dateStartValue.text = parseDateTimeWithBindToTimeZone(
                challenge.startAt,
                requireContext()
            )
        }
    }

    private fun handleReactions() {
        binding.reactionGroup.onLikeClicked = {
            challengeId?.let { challengeId -> viewModel.pressLike(challengeId) }
        }

        binding.reactionGroup.onLongLikeClicked = {
            openReactions()
        }

        binding.reactionGroup.onCommentClicked = {
            openComments()
        }
    }

    private fun openReactions() {
        challengeId?.let {
            val bundle = Bundle().apply {
                putInt(Consts.LIKE_TO_OBJECT_ID, it)
                putParcelable(Consts.LIKE_TO_OBJECT_TYPE, ObjectsToLike.CHALLENGE)
            }
            findNavController().navigate(R.id.action_global_reactionsFragment, bundle)
        }
    }

    private fun openComments() {
        challengeId?.let {
            val bundle = Bundle().apply {
                putInt(Consts.OBJECTS_COMMENT_ID, it)
                putString(CommentsBottomSheetFragment.OBJECTS_COMMENT_NAME, dataOfChallenge?.name)
                putParcelable(Consts.OBJECTS_COMMENT_TYPE, ObjectsComment.CHALLENGE)
            }
            findNavController().navigate(R.id.action_global_commentsBottomSheetFragment, bundle)
        }
    }

    private fun defaultStatusBar() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onStop() {
        super.onStop()
        defaultStatusBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        defaultStatusBar()
        viewModel.clearChallengeData()
    }


    private fun enableOrDisableSentReportButton(
        challenge: ChallengeModelById
    ) {
        binding.sendReportBtn.isEnabled = challenge.isAvailable
    }

    private fun handleTopAppBar() {
        binding.toolbar.setNavigationOnClickListener {
            defaultStatusBar()
            requireActivity().onBackPressedDispatcher.onBackPressed()

        }
    }

    companion object {
        const val TAG = "DetailsMainChallengeFragment"
        const val CURRENT_POSITION_PAGE = "currentPositionOfPage"
        const val INIT_SECTION_PAGE = "initSectionPage"

        @JvmStatic
        fun newInstance() =
            DetailsMainChallengeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onImageLoadComplete() {
        //startPostponedEnterTransition()
    }

    override fun onImageLoadFailed() {
        //startPostponedEnterTransition()
    }

}