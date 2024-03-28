package com.teamforce.thanksapp.presentation.fragment.mainScreen

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.material.transition.platform.MaterialElevationScale
import com.teamforce.thanksapp.NotificationSharedViewModel
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentMainScreenBinding
import com.teamforce.thanksapp.domain.models.recommendations.RecommendModel
import com.teamforce.thanksapp.presentation.adapter.mainScreen.EventsMainScreenAdapter
import com.teamforce.thanksapp.presentation.adapter.mainScreen.RecommendsAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.customViews.LockableNestedScrollView.LockableNestedScrollView
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.BenefitConsts
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts
import com.teamforce.thanksapp.presentation.fragment.challenges.challengeChains.DetailsMainChallengeChainFragment
import com.teamforce.thanksapp.presentation.fragment.profileScreen.settings.ListOfOrganizationsFragment
import com.teamforce.thanksapp.presentation.viewmodel.mainScreen.MainScreenViewModel
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.ViewLifecycleDelegate
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.dp
import com.teamforce.thanksapp.utils.getDaysAmountFromNowToTarget
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainScreenFragment :
    BaseFragment<FragmentMainScreenBinding>(FragmentMainScreenBinding::inflate) {

    private val viewModel: MainScreenViewModel by activityViewModels()
    private val sharedViewModel: NotificationSharedViewModel by activityViewModels()

    private val newsAdapter by ViewLifecycleDelegate {
        EventsMainScreenAdapter()
    }

    private val recommendsAdapter by ViewLifecycleDelegate {
        RecommendsAdapter(::onRecommendClicked)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = 400L
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = 400L
        }
    }

    private fun handleBackDropBehavior() {
        val headerHeight = 56.dp
        val topViewHeight =
            (binding.hideableLinear.layoutParams as CoordinatorLayout.LayoutParams).height
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        val bottomSheetBehavior: BottomSheetBehavior<LockableNestedScrollView> =
            BottomSheetBehavior.from(binding.scrollView).apply {
                state = BottomSheetBehavior.STATE_COLLAPSED
                peekHeight = screenHeight - headerHeight - topViewHeight
            }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.recommendsRv.isNestedScrollingEnabled = false
                    binding.newsRv.isNestedScrollingEnabled = false
                }
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    binding.recommendsRv.isNestedScrollingEnabled = true
                    binding.newsRv.isNestedScrollingEnabled = true
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val alpha = 0.9f - slideOffset
                binding.hideableLinear.alpha = alpha
            }
        })

        binding.scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            bottomSheetBehavior.isDraggable = scrollY == 0
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomSheetBehavior: BottomSheetBehavior<LockableNestedScrollView> =
            BottomSheetBehavior.from(binding.scrollView)
        val isExpandedState = bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED
        binding.recommendsRv.isNestedScrollingEnabled = isExpandedState
        binding.newsRv.isNestedScrollingEnabled = isExpandedState
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackDropBehavior()
        handleTopAppBar()
        listenersForNavSection()
        loadBalanceData()
        handleNewsList()
        handleRecommendsList()
        if (viewModel.balance.value == null) {
            loadBalanceData()
            setBalanceData()
            viewModel.isLoading.distinctUntilChanged().observe(
                viewLifecycleOwner
            ) { isLoading ->
                if (isLoading) {
                    binding.shimmerLayout.startShimmer()
                    binding.shimmerLayout.visible()
                    binding.hideableLinear.invisible()
                } else {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.invisible()
                    binding.hideableLinear.
                    visible()
                }
            }
        } else {
            setBalanceData()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) {
                binding.recommendsShimmer.startShimmer()
                binding.recommendsShimmer.visible()
                binding.recommendsRv.invisible()
            } else {
                binding.recommendsShimmer.stopShimmer()
                binding.recommendsShimmer.invisible()
                binding.recommendsRv.visible()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        newsAdapter.addLoadStateListener { state ->
            if (state.refresh == LoadState.Loading) {
                binding.newsRv.invisible()
                binding.newsShimmer.visible()
                binding.newsShimmer.startShimmer()
            } else {
                binding.newsRv.visible()
                binding.newsShimmer.invisible()
                binding.newsShimmer.stopShimmer()
            }
        }

    }

    private fun onRecommendClicked(
        itemId: Long,
        type: RecommendModel.RecommendObjectType,
        marketId: Int?
    ) {
        when (type) {
            RecommendModel.RecommendObjectType.CHAIN -> navigateToChainDetails(itemId)
            RecommendModel.RecommendObjectType.CHALLENGE -> navigateToChallengeDetails(itemId)
            RecommendModel.RecommendObjectType.OFFER -> marketId?.let {
                navigateToOfferDetails(
                    itemId,
                    it
                )
            }

            else -> Unit // Do nothing for other types
        }
    }

    private fun navigateToChainDetails(chainId: Long) {
        val bundle = Bundle().apply {
            putLong(DetailsMainChallengeChainFragment.CHAIN_ID, chainId)
        }
        findNavController().navigateSafely(
            R.id.action_global_detailsMainChallengeChainFragment,
            bundle,
            OptionsTransaction().optionForEditProfile
        )
    }

    private fun navigateToChallengeDetails(challengeId: Long) {
        val bundle = Bundle().apply {
            putInt(ChallengesConsts.CHALLENGER_ID, challengeId.toInt())
        }
        findNavController().navigateSafely(
            R.id.action_global_detailsMainChallengeFragment,
            bundle,
            OptionsTransaction().optionForEditProfile
        )
    }

    private fun navigateToOfferDetails(offerId: Long, marketId: Int) {
        val bundle = Bundle().apply {
            putInt(BenefitConsts.OFFER_ID, offerId.toInt())
            putInt(BenefitConsts.MARKET_ID, marketId)
        }
        findNavController().navigateSafely(
            R.id.action_global_benefitDetailMainFragment,
            bundle,
            OptionsTransaction().optionForEditProfile
        )
    }


    private fun loadRecommends() {
        viewModel.loadRecommends()
    }

    private fun handleRecommendsList() {
        binding.recommendsRv.adapter = recommendsAdapter
        loadRecommends()
        viewModel.recommends.observe(viewLifecycleOwner) {
            if(!it.isNullOrEmpty()){
                recommendsAdapter.submitList(it)
                binding.recommendsEmptyView.root.invisible()
            }else{
                binding.recommendsEmptyView.root.visible()
            }
        }


    }

    private fun loadNews() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.getEvents().collectLatest {
                    newsAdapter.submitData(it)
                }
            }
        }
    }

    private fun handleNewsList() {
        binding.newsRv.adapter = newsAdapter
        loadNews()
        newsAdapter.onTransactionClicked = { transactionId, message ->
            val bundle = Bundle()
            bundle.putInt(Consts.TRANSACTION_ID, transactionId)
            bundle.putString(Consts.MESSSAGE, message)
            findNavController()
                .navigateSafely(
                    R.id.action_global_additionalInfoFeedItemFragment,
                    bundle
                )
        }
    }

    private fun setBalanceData() {
        viewModel.balance.observe(viewLifecycleOwner) {
            binding.hideableLinear.setMyCountText(it.income.amount.toString())
            binding.hideableLinear.setRemainsText(it.distribute.amount.toString())
            binding.hideableLinear.setDaysBeforeBurn(getDaysAmountFromNowToTarget(it.distribute.expireDate))
        }
    }

    private fun loadBalanceData() {
        viewModel.loadUserBalance()
    }


    private fun listenersForNavSection() {
        binding.eventsLinear.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_global_eventsFragment,
                null,
                OptionsTransaction().optionForEditProfile
            )
        }

        binding.participantsCard.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_global_employees_graph,
                null,
                OptionsTransaction().optionForEditProfile
            )
        }

        binding.awardsCard.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_global_awardsContainerFragment,
                null,
                OptionsTransaction().optionForEditProfile
            )

        }

        binding.historyCard.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_global_history_graph,
                null,
                OptionsTransaction().optionForEditProfile
            )
        }
    }


    private fun handleTopAppBar() {
        binding.header.toolbar.background = null
        setUserAvatarInTopAppBar()
        setOnClickListenersInTopAppBar()
        sharedViewModel.state.observe(viewLifecycleOwner) { notificationsCount ->
            if (notificationsCount == 0) {
                binding.header.apply {
                    notifyBadgeFrame.invisible()
                }
            } else {
                binding.header.apply {
                    binding.header.notifyBadgeFrame.visible()
                    notifyBadgeText.text = notificationsCount.toString()
                }
            }
        }
    }

    private fun setOnClickListenersInTopAppBar() {
        binding.header.userAvatar.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_global_profileGraph
            )
        }

        binding.header.notifyLayout.setOnClickListener {
            findNavController().navigateSafely(R.id.action_global_notificationsFragment)
        }
    }

    private fun setUserAvatarInTopAppBar() {
        sharedViewModel.getUserData()
        sharedViewModel.userData.observe(viewLifecycleOwner) { userData ->
            userData?.let {

                binding.header.userAvatar.setAvatarImageOrInitials(it.avatar, it.username)
                binding.header.usernameTv.text = it.username.toString()
            }
        }
    }


    override fun applyTheme() {
        binding.header.notifyIv.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(Branding.brand.colorsJson.generalBackgroundColor))
        binding.header.usernameTv.setTextColor(Color.parseColor(Branding.brand.colorsJson.generalBackgroundColor))
        binding.header.greetingTv.setTextColor(Color.parseColor(Branding.brand.colorsJson.generalBackgroundColor))
        binding.header.notifyBadgeText.setTextColor(Color.parseColor(Branding.brand.colorsJson.generalBackgroundColor))

        val layerDrawable = ResourcesCompat.getDrawable(
            resources,
            R.drawable.main_gradient_background_bubbles,
            null
        ) as LayerDrawable

        val gradientDrawable = layerDrawable.getDrawable(0) as GradientDrawable

        gradientDrawable.apply {
            colors = intArrayOf(
                Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor),
                Color.parseColor(Branding.brand.colorsJson.mainBrandColor)
            )
        }

        binding.allContent.background = layerDrawable
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainScreenFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}



