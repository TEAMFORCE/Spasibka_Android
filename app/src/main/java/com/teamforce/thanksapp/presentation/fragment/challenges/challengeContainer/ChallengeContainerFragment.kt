package com.teamforce.thanksapp.presentation.fragment.challenges.challengeContainer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentChallengeContainerBinding
import com.teamforce.thanksapp.presentation.adapter.challenge.ChallengePagerAdapter
import com.teamforce.thanksapp.presentation.adapter.challenge.challengeChains.ChainMiniAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts
import com.teamforce.thanksapp.presentation.fragment.challenges.challengeChains.DetailsMainChallengeChainFragment
import com.teamforce.thanksapp.presentation.viewmodel.challenge.challengeContainer.ChallengeContainerViewModel
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.dp
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.screenState.OneTimeEvent
import com.teamforce.thanksapp.utils.screenState.consumeOnce
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ChallengeContainerFragment :
    BaseFragment<FragmentChallengeContainerBinding>(FragmentChallengeContainerBinding::inflate) {


    private val viewModel: ChallengeContainerViewModel by activityViewModels()
    private var challengeAdapter: ChallengePagerAdapter = ChallengePagerAdapter()

    private var chainAdapter: ChainMiniAdapter = ChainMiniAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        setAdapters()
        setBtnListeners()
        if((binding.challengeList.adapter as ChallengePagerAdapter).itemCount == 0){
            loadChallenges()
        }

        if(viewModel.screenState.value.chains.isEmpty()) viewModel.loadChainsOnlyFirstPage()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.screenState.collectLatest { state ->
                    renderUiState(state)
                }
            }
        }


    }

    private fun loadChallenges(){
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED){
                viewModel.challenges().collectLatest {
                    (binding.challengeList.adapter as ChallengePagerAdapter).submitData(it)
                }
            }
        }
    }

    private fun setAdapters() {
        binding.challengeList.adapter = challengeAdapter

        binding.chainList.adapter = chainAdapter
        challengeAdapter.addLoadStateListener { state ->
            binding.swipeRefreshLayout.isRefreshing = state.refresh == LoadState.Loading
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadChallenges()
        }
        chainAdapter.onChallengeChainClicked = { challengeChainId: Long, clickedView: View ->
            onChainClick(challengeChainId, clickedView)
        }
        challengeAdapter.onChallengeClicked =
            { challengeId: Int, clickedView: View, titleView: View ->
                onChallengeClick(challengeId, clickedView, titleView)
            }

        challengeAdapter.addLoadStateListener { state ->
            when(state.refresh){
                is LoadState.NotLoading -> {
                    if(challengeAdapter.snapshot().isEmpty()){
                        binding.challengeList.invisible()
                        binding.challengeEmptyView.root.visible()
                    }else{
                        binding.challengeList.visible()
                        binding.challengeEmptyView.root.invisible()
                    }
                }
                LoadState.Loading -> {
                    binding.challengeList.invisible()
                    binding.challengeEmptyView.root.visible()
                }
                is LoadState.Error -> {}
            }
        }
    }

    private fun onChallengeClick(challengeId: Int, clickedView: View, titleView: View) {
        val bundle = Bundle().apply {
            putInt(ChallengesConsts.CHALLENGER_ID, challengeId)
        }
        clickedView.findNavController().navigateSafely(
            R.id.action_global_detailsMainChallengeFragment,
            bundle,
            OptionsTransaction().optionForEditProfile,
        )
    }

    private fun onChainClick(challengeChainId: Long, clickedView: View) {
        val bundle = Bundle()
        bundle.apply {
            putLong(DetailsMainChallengeChainFragment.CHAIN_ID, challengeChainId)
        }
        findNavController().navigateSafely(
            R.id.action_global_detailsMainChallengeChainFragment,
            bundle,
            OptionsTransaction().optionForEditProfile,
        )
    }

    private fun renderUiState(state: ChallengeContainerViewModel.ChallengeContainerScreenState) {
        with(state) {
            error?.let {
                displayError(it)
            }

            if (!isLoading){
                binding.chainList.invisible()
                binding.chainEmptyView.root.visible()
            }else{
                binding.chainList.visible()
                binding.chainEmptyView.root.invisible()
            }

            if(chains.isNotEmpty()){
                chainAdapter.submitList(chains)
                binding.chainList.visible()
                binding.chainEmptyView.root.invisible()
            }else{
                binding.chainList.invisible()
                binding.chainEmptyView.root.visible()
            }
        }

    }

    private fun displayError(error: OneTimeEvent<String>) {
        error.consumeOnce { failure ->
            Toast.makeText(
                requireContext(),
                failure,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setBtnListeners() {
        binding.allChainsBtn.setOnClickListener {
            findNavController().navigateSafely(R.id.action_global_challengeChainsListFragment)
        }

        binding.allChallengeBtn.setOnClickListener {
            // challenge_list
            val extras = FragmentNavigatorExtras(binding.challengeLinear to "challenge_list")

            it.findNavController().navigateSafely(
                R.id.action_global_challengesFragment,
                null,
                null,
                extras
            )
            // findNavController().navigateSafely(R.id.action_global_challengesFragment)
        }
    }


    private fun handleTopAppBar() {
        binding.header.toolbar.title = requireContext().getString(R.string.challenge_label)
        binding.header.challengeMenuBtn.visible()
        binding.header.challengeMenuBtn.setOnClickListener {
            showSimpleDialog()
        }

        binding.nestedScroll.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->

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

        private fun showSimpleDialog() {
            val items = arrayOf(
                getString(R.string.create_challenge_dialog),
                getString(R.string.templates_title)
            )

            MaterialAlertDialogBuilder(requireContext(), R.style.Theme_ThanksApp_Dialog_Simple)
                .setItems(items) { dialog, which ->
                    when (which) {
                        0 -> {
                            findNavController().navigateSafely(R.id.action_global_createChallengeFragment, null, OptionsTransaction().optionForEditProfile)
                        }
                        1 -> {
                            findNavController().navigateSafely(R.id.action_global_templatesFragment)
                        }
                    }
                }
                .show()
        }


        override fun applyTheme() {

        }

        companion object {
            @JvmStatic
            fun newInstance() =
                ChallengeContainerFragment().apply {
                    arguments = Bundle().apply {
                    }
                }
        }
    }