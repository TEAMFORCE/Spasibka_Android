package com.teamforce.thanksapp.presentation.fragment.challenges

import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.util.Log
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.map
import by.kirich1409.viewbindingdelegate.viewBinding
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentChallengeListBinding
import com.teamforce.thanksapp.presentation.adapter.challenge.ChallengePagerAdapter
import com.teamforce.thanksapp.presentation.adapter.decorators.VerticalDividerDecoratorForListWithBottomBar
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.viewmodel.challenge.ChallengesViewModel
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.showSnackBarAboutNetworkProblem
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class ChallengeListFragment : Fragment(R.layout.fragment_challenge_list) {

    private val binding: FragmentChallengeListBinding by viewBinding()
    private val viewModel: ChallengesViewModel by activityViewModels()

    private var activeOnly: Int = 0
    private var delayedChallenges: Int = 0
    private var chainId: Long = 0
    private var challengeId: Int = 0
    private var listAdapter: ChallengePagerAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            activeOnly = it.getInt(ACTIVE_ONLY)
            delayedChallenges = it.getInt(DELAYED_CHALLENGES)
            chainId = it.getLong(CHAIN_ID)
            challengeId = it.getInt(CHALLENGE_ID)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        loadChallenges(activeOnly = activeOnly, delayedChallenges = delayedChallenges, chainId)
        listAdapter!!.addLoadStateListener { loadState ->
            if (listAdapter!!.itemCount == 0) {
                showEmptyView()
            } else {
                hideEmptyView()
            }

            if (loadState.refresh is LoadState.Error) {
                showSnackBarAboutNetworkProblem(
                    view,
                    requireContext()
                )
                showConnectionError()
            } else {
                hideConnectionError()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.needUpdateList.collectLatest { needUpdate ->
                if (needUpdate) {
                    binding.swipeRefreshLayout.post {
                        listAdapter?.refresh()
                        viewModel.setNeedUpdateListValue(false)
                    }
                }
            }
        }
    }

    private fun showEmptyView() {
        binding.emptyView.root.visible()
        binding.list.invisible()
    }

    private fun hideEmptyView() {
        binding.emptyView.root.invisible()
        binding.list.visible()
    }

    private fun showConnectionError() {
        binding.error.root.visible()
        binding.list.invisible()
    }

    private fun hideConnectionError() {
        binding.error.root.invisible()
        binding.list.visible()
    }

    private fun initAdapter() {
        listAdapter = ChallengePagerAdapter()

        binding.list.apply {
            this.adapter = listAdapter?.withLoadStateHeaderAndFooter(
                header = HistoryLoadStateAdapter(),
                footer = HistoryLoadStateAdapter()
            )
            this.addItemDecoration(
                VerticalDividerDecoratorForListWithBottomBar(
                    8,
                    listAdapter!!.itemCount
                )
            )
        }

        listAdapter?.addLoadStateListener { state ->
            binding.swipeRefreshLayout.isRefreshing = state.refresh == LoadState.Loading
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            listAdapter?.refresh()
            binding.swipeRefreshLayout.isRefreshing = true
        }

        listAdapter?.onChallengeClicked = { challengeId: Int, clickedView: View, titleView: View ->
            val bundle = Bundle().apply {
                putInt(ChallengesConsts.CHALLENGER_ID, challengeId)
            }
            clickedView.findNavController().navigateSafely(
                R.id.action_global_detailsMainChallengeFragment,
                bundle,
                OptionsTransaction().optionForEditProfile
            )

        }

    }

    private fun loadChallenges(activeOnly: Int, delayedChallenges: Int, chainId: Long) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            if (chainId != 0L) {
                binding.swipeRefreshLayout.isRefreshing = false
                viewModel.challengesFromChain(chainId).collectLatest { challenges ->
                    listAdapter?.submitData(challenges)
                    hideConnectionError()
                }
            } else if (challengeId != 0) {
                binding.swipeRefreshLayout.isRefreshing = false
                viewModel.dependentChallenges(challengeId).collectLatest { challenges ->
                    listAdapter?.submitData(challenges)
                    hideConnectionError()
                }
            } else if (activeOnly == 1) {
                binding.swipeRefreshLayout.isRefreshing = false
                viewModel.activeChallenge.collectLatest { challenges ->
                    listAdapter?.submitData(challenges)
                    hideConnectionError()
                }
            } else if (delayedChallenges == 1) {
                binding.swipeRefreshLayout.isRefreshing = false
                viewModel.delayedChallenge.collectLatest { challenges ->
                    listAdapter?.submitData(challenges)
                    hideConnectionError()
                }
            } else if (activeOnly == 0 && delayedChallenges == 0 && chainId == 0L) {
                binding.swipeRefreshLayout.isRefreshing = false
                viewModel.allChallenge.collectLatest { challenges ->
                    listAdapter?.submitData(challenges)
                    hideConnectionError()
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.list.adapter = null
        listAdapter = null
        super.onDestroyView()
    }

    companion object {
        private const val ACTIVE_ONLY = "active_only"
        private const val DELAYED_CHALLENGES = "delayed_challenges"
        private const val CHAIN_ID = "chain_id"
        private const val CHALLENGE_ID = "challenge_id"

        @JvmStatic
        fun newInstance(
            activeOnly: Int = 0,
            delayedChallenges: Int = 0,
            chainId: Long = 0L,
            challengeId: Int = 0
        ) =
            ChallengeListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ACTIVE_ONLY, activeOnly)
                    putInt(DELAYED_CHALLENGES, delayedChallenges)
                    putLong(CHAIN_ID, chainId)
                    putInt(CHALLENGE_ID, challengeId)
                }
            }
    }
}