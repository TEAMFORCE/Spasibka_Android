package com.teamforce.thanksapp.presentation.fragment.challenges.challengeChains

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentDetailsMainChallengeChainBinding
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ChainCondition
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.ChainModel
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.benefitDetail.ImageSlider.ImageSliderAdapter
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.benefitDetail.ImageSlider.ImageViewInSliderFragment
import com.teamforce.thanksapp.presentation.viewmodel.challenge.challengeChain.DetailsMainChallengeChainViewModel
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.Extensions.enableOnClickableLinks
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.blur
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.dp
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.parseDateTimeToDDMonthYYYY
import com.teamforce.thanksapp.utils.parseDateTimeWithBindToTimeZone
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsMainChallengeChainFragment :
    BaseFragment<FragmentDetailsMainChallengeChainBinding>(FragmentDetailsMainChallengeChainBinding::inflate) {

    private var chainId: Long? = null
    private val viewModel: DetailsMainChallengeChainViewModel by activityViewModels()

    private var chainData: ChainModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            chainId = it.getLong(CHAIN_ID)
        }
        transparentStatusBar()
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
        init()
        handleTopAppBar()
        postponeEnterTransition()
        readingTheCurrentPositionOfPhoto()
        listenerAppBar()
        chainId?.let {
            loadData(it)

        }
        viewModel.chain.observe(viewLifecycleOwner) {
            chainData = it
            setData(it)
            setBtnListeners(it)
            startPostponedEnterTransition()
        }
        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

    }

    private fun setBtnListeners(chainData: ChainModel){
        val bundle = Bundle().apply {
            putLong(CHAIN_ID, chainData.id)
            putString(CHAIN_NAME, chainData.name)
        }
        binding.stepsBtn.setOnClickListener {
            findNavController().navigateSafely(R.id.action_global_questStepsListFragment, bundle, OptionsTransaction().optionForEditProfile)
        }
        binding.participantsBlurView.setOnClickListener {
            findNavController().navigateSafely(R.id.action_global_participantsChainFragment, bundle, OptionsTransaction().optionForEditProfile)
        }
    }

    private fun listenerAppBar() {
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val isAppBarCollapsed = verticalOffset <= -appBarLayout.totalScrollRange + 56.dp
            if (isAppBarCollapsed) {
                binding.dotsIndicator.invisible()
                binding.participantsBlurView.invisible()
            }
            else {
                if (chainData?.photos?.size != null &&
                    chainData?.photos?.size!! > 1
                ) binding.dotsIndicator.visible()
                binding.participantsBlurView.visible()
            }

        }
    }

    private fun loadData(chainId: Long) {
        viewModel.loadChain(chainId)
    }

    private fun setData(chain: ChainModel) {
        chain.apply {
            if (this.photos.isNotEmpty()) setSliderOnImage(this.photos) else binding.imageSliderVp.invisible()
            setDetailChainData(this)
        }
    }

    private fun setDetailChainData(chainModel: ChainModel) {
        chainModel.apply {
            binding.nameChain.text = chainModel.name
            binding.descriptionChainTv.enableOnClickableLinks()
            binding.descriptionChainTv.text = chainModel.description
            binding.prizePoolAndWinnersValueTv.text =
                "${this.tasksFinished}/${this.taskTotal} ${getString(R.string.challenge_chain_finished)}"
            setDate(this)
            setOrganizer(this)
        }
    }

    private fun setDate(data: ChainModel) {
        data.endAt?.let {
            binding.dateEndValue.text = parseDateTimeToDDMonthYYYY(
                it,
                requireContext()
            )
        }
    }

    private fun setOrganizer(chain: ChainModel) {
        binding.userAvatar.setAvatarImageOrInitials(chain.authorPhoto, chain.author)

        binding.userTgName.text = chain.author

        binding.chainOrganizer.setOnClickListener { view ->
            transactionToProfileOfCreator(chain.authorId, view)
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

    private fun setSliderOnImage(images: List<String>) {
        if (images.size > 1) {
            binding.dotsIndicator.visible()
        }
        val imagePager = ImageSliderAdapter(
            childFragmentManager,
            viewLifecycleOwner.lifecycle,
            images
        )
        binding.imageSliderVp.apply {
            adapter = imagePager
            binding.dotsIndicator.attachTo(this)
        }
        binding.imageSliderVp.visible()
    }



    override fun applyTheme() {
        binding.collapsingToolbar.setBackgroundColor(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
    }

    private fun defaultStatusBar() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onStop() {
        super.onStop()
        defaultStatusBar()
    }

    override fun onResume() {
        super.onResume()
        transparentStatusBar()
    }

    private fun transparentStatusBar() {
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    private fun handleTopAppBar() {
        binding.toolbar.setNavigationOnClickListener {
            defaultStatusBar()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun init(){
        binding.participantsBlurView.blur(2f, requireContext(), binding.collapsingToolbar)
    }

    companion object {

        const val CHAIN_ID = "chain_id"
        const val CHAIN_NAME = "chain_name"


        @JvmStatic
        fun newInstance(chainId: Long) =
            DetailsMainChallengeChainFragment().apply {
                arguments = Bundle().apply {
                    putLong(CHAIN_ID, chainId)
                }
            }
    }
}