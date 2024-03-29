package com.teamforce.thanksapp.presentation.fragment.challenges.challengeChains

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentChallengeChainsListBinding
import com.teamforce.thanksapp.presentation.adapter.challenge.ChallengePagerAdapter
import com.teamforce.thanksapp.presentation.adapter.challenge.challengeChains.ChallengeChainsPagerAdapter
import com.teamforce.thanksapp.presentation.adapter.decorators.VerticalDividerDecoratorForListWithBottomBar
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts
import com.teamforce.thanksapp.presentation.viewmodel.challenge.ChallengesViewModel
import com.teamforce.thanksapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ChallengeChainsListFragment : Fragment(R.layout.fragment_challenge_chains_list) {

    private val binding: FragmentChallengeChainsListBinding by viewBinding()
    private val viewModel: ChallengesViewModel by activityViewModels()

    private var listAdapter: ChallengeChainsPagerAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        initView()
        initAdapter()
        loadChallengeChains()
        listAdapter!!.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Error) {
                showSnackBarAboutNetworkProblem(
                    view,
                    requireContext()
                )
                showConnectionError()
            }else{
                hideConnectionError()
            }
        }
    }
    private fun initView() {
        binding.swipeRefreshLayout.setColorSchemeColors(requireContext().getColor(R.color.general_brand))
    }

    private fun initAdapter() {
        listAdapter = ChallengeChainsPagerAdapter()

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

        listAdapter?.onChallengeChainClicked = { challengeChainId: Long, clickedView: View ->
            val bundle = Bundle()
            bundle.apply {
                putLong(DetailsMainChallengeChainFragment.CHAIN_ID, challengeChainId)
            }
            val extras = FragmentNavigatorExtras( clickedView to "imageBackground")

            findNavController().navigate(
                R.id.action_global_detailsMainChallengeChainFragment,
                bundle,
                null,
                extras
            )

        }

    }

    private fun loadChallengeChains() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.challengeChains.collectLatest { challengeChains ->
                listAdapter?.submitData(challengeChains)
                hideConnectionError()
            }
        }
    }

    private fun showConnectionError() {
        binding.error.root.visible()
        binding.list.invisible()
    }

    private fun hideConnectionError() {
        binding.error.root.invisible()
        binding.list.visible()
    }

    private fun handleTopAppBar(){
        binding.header.toolbar.title = requireContext().getString(R.string.challengeChains)
        binding.header.challengeMenuBtn.visible()
        binding.header.challengeMenuBtn.setOnClickListener {
            showSimpleDialog()
        }
        binding.list.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val currentElevation = binding.header.toolbar.translationZ
            val targetElevation = if (scrollY > 20.dp) {
                2.dp.toFloat()
            } else {
                0.toFloat()
            }
            if (currentElevation != targetElevation) {
                binding.header.toolbar.translationZ = targetElevation
            }
        }
    }

    private fun showSimpleDialog(){
        val items = arrayOf(getString(R.string.create_challenge_dialog), getString(R.string.templates_title))

        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_ThanksApp_Dialog_Simple)
            .setItems(items) { dialog, which ->
                when(which){
                    0 -> {
                        findNavController().navigateSafely(R.id.action_global_createChallengeFragment)
                    }
                    1 -> {
                        findNavController().navigateSafely(R.id.action_global_templatesFragment)
                    }
                }
            }
            .show()
    }



    override fun onDestroyView() {
        binding.list.adapter = null
        listAdapter = null
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ChallengeChainsListFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}