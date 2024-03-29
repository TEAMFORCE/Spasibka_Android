package com.teamforce.thanksapp.presentation.fragment.notifications

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.teamforce.thanksapp.NotificationSharedViewModel
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentNotificationsBinding
import com.teamforce.thanksapp.domain.models.challenge.SectionsOfChallengeType
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.adapter.notifications.NotificationPageAdapter
import com.teamforce.thanksapp.presentation.adapter.notifications.TypeOfReaction
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.BenefitConsts
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts
import com.teamforce.thanksapp.presentation.fragment.challenges.DetailsMainChallengeFragment
import com.teamforce.thanksapp.presentation.fragment.feedScreen.AdditionalInfoFeedItemFragment
import com.teamforce.thanksapp.presentation.viewmodel.NotificationsViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import java.util.concurrent.CancellationException

@AndroidEntryPoint
class NotificationsFragment :
    BaseFragment<FragmentNotificationsBinding>(FragmentNotificationsBinding::inflate) {

    private val viewModel: NotificationsViewModel by viewModels()
    private val sharedViewModel: NotificationSharedViewModel by activityViewModels()
    private var listAdapter: NotificationPageAdapter? = null


    private fun onViewStateError(throwable: Throwable?) {
        println("onError $throwable")
        hideConnectionError()
        Toast.makeText(
            requireContext(),
            resources.getText(R.string.smthWentWrong),
            Toast.LENGTH_LONG,
        ).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        listAdapter = NotificationPageAdapter(
            onChallengeClicked = ::onChallengeClicked,
            onUserClicked = ::onUserClicked,
            onTransactionClicked = ::onTransactionClicked,
            onOfferClicked = ::onOfferClicked
        )

        binding.notificationsList.apply {
            adapter = listAdapter?.withLoadStateHeaderAndFooter(
                header = HistoryLoadStateAdapter(),
                footer = HistoryLoadStateAdapter(),

                )
            layoutManager = LinearLayoutManager(requireContext())
        }

        listAdapter?.addLoadStateListener { state ->
            binding.swipeRefreshLayout.isRefreshing = state.refresh == LoadState.Loading
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            listAdapter?.refresh()
            binding.swipeRefreshLayout.isRefreshing = true
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getNotifications().safeViewState(
                onSuccess = {
                    hideConnectionError()
                    listAdapter?.submitData(it)
                },
                onError = ::onViewStateError
            )
        }

        listAdapter!!.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Error) {
                showSnackBarAboutNetworkProblem(
                    view,
                    requireContext()
                )
                showConnectionError()
            }
        }

    }

    private fun handleTopAppBar() {
        binding.header.toolbar.title = requireContext().getString(R.string.notifications_label)
        binding.header.closeBtn.visible()
        binding.header.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.header.closeBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        sharedViewModel.checkNotifications()

        sharedViewModel.state.observe(viewLifecycleOwner) { amountUnreadNotify ->
            if (amountUnreadNotify > 0) {
                binding.header.toolbar.subtitle = String.format(
                    getString(R.string.notifications_subtitle),
                    requireContext().resources.getQuantityString(
                        R.plurals.plurals_new_notifications,
                        amountUnreadNotify,
                        amountUnreadNotify
                    )
                )
                listAdapter?.setUnreadCountNotify(amountUnreadNotify)
            }
        }

        binding.notificationsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    private fun showConnectionError() {
        binding.error.root.visible()
        binding.notificationsList.invisible()
    }

    private fun hideConnectionError() {
        println("hideConnectionError")
        binding.error.root.invisible()
        binding.notificationsList.visible()
    }

    override fun onDestroyView() {
        binding.notificationsList.adapter = null
        listAdapter = null
        super.onDestroyView()

    }

    private fun onUserClicked(userId: Int) {
        findNavController().navigateSafely(
            R.id.action_global_someonesProfileFragment,
            bundleOf(Consts.USER_ID to userId),
            OptionsTransaction().optionForAdditionalInfoFeedFragment
        )
    }

    private fun onTransactionClicked(
        transactionId: Int,
        message: CharSequence,
        typeOfReaction: TypeOfReaction?
    ) {

        if (typeOfReaction != null) {
            val showComment = typeOfReaction == TypeOfReaction.COMMENTS
            val showReaction = typeOfReaction == TypeOfReaction.REACTIONS
            findNavController().navigateSafely(
                R.id.action_notificationsFragment_to_additionalInfoFeedItemFragment2,
                bundleOf(
                    Consts.TRANSACTION_ID to transactionId,
                    AdditionalInfoFeedItemFragment.FEED_ITEM_SHOW_REACTION to showReaction,
                    AdditionalInfoFeedItemFragment.FEED_ITEM_SHOW_COMMENT to showComment,
                    Consts.MESSSAGE to message
                ),
                OptionsTransaction().optionForEditProfile

            )
        } else {
            findNavController().navigateSafely(
                R.id.action_global_history_graph,
                bundleOf(
                    Consts.TRANSACTION_ID to transactionId,
                    Consts.MESSSAGE to message
                ),
                OptionsTransaction().optionForEditProfile

            )
        }

    }

    private fun onOfferClicked(offerId: Int?, marketplaceId: Int) {
        if (offerId != null) {
            findNavController().navigateSafely(
                R.id.action_global_benefitDetailMainFragment,
                bundleOf(
                    BenefitConsts.MARKET_ID to marketplaceId,
                    BenefitConsts.OFFER_ID to offerId
                ),
                OptionsTransaction().optionForAdditionalInfoFeedFragment
            )
        } else {
            findNavController().navigateSafely(
                R.id.action_global_benefitFragment,
                bundleOf(
                    BenefitConsts.MARKET_ID to marketplaceId
                ),
                OptionsTransaction().optionForAdditionalInfoFeedFragment
            )
        }

    }

    private fun onChallengeClicked(challengeId: Int, sectionsOfChallengeType: SectionsOfChallengeType) {
        val bundle = Bundle().apply {
            putInt(ChallengesConsts.CHALLENGER_ID, challengeId)
            putParcelable(DetailsMainChallengeFragment.INIT_SECTION_PAGE, sectionsOfChallengeType)
        }
        findNavController().navigateSafely(
            R.id.action_global_detailsMainChallengeFragment,
            bundle,
            OptionsTransaction().optionForAdditionalInfoFeedFragment
        )
    }

    override fun onDestroy() {
        sharedViewModel.dropNotificationCounter()
        super.onDestroy()
    }

    override fun applyTheme() {
    }
}

private suspend fun <T : Any> StateFlow<T>.safeViewState2(
    onSuccess: suspend (T) -> Unit,
    onError: ((Throwable?) -> Unit)? = null
) {
    try {
        collectLatest {
            try {
                onSuccess(it)
            } catch (e: Throwable) {
                Timber.e("toViewState1", e)
                onError?.invoke(e)
            }
        }
    } catch (e: Throwable) {
        Timber.e("toViewState2", e)
        onError?.invoke(e)
    }
}

private suspend fun <T : Any> Flow<PagingData<T>>.safeViewState(
    onSuccess: suspend (PagingData<T>) -> Unit,
    onError: ((Throwable?) -> Unit)? = null
) {
    collectLatest {
        try {
            onSuccess(it)
        } catch (e: CancellationException) {
            Timber.w(e)
        } catch (e: Throwable) {
            Timber.e(e)
            onError?.invoke(e)
        }
    }
}

