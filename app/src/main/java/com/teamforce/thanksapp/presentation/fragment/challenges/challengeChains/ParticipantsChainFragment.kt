package com.teamforce.thanksapp.presentation.fragment.challenges.challengeChains

import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentParticipantsChainBinding
import com.teamforce.thanksapp.presentation.adapter.challenge.challengeChains.ParticipantChainAdapter
import com.teamforce.thanksapp.presentation.adapter.decorators.VerticalDividerDecoratorForListWithBottomBar
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.challengeChains.DetailsMainChallengeChainFragment.Companion.CHAIN_ID
import com.teamforce.thanksapp.presentation.fragment.profileScreen.SomeonesProfileFragment
import com.teamforce.thanksapp.presentation.viewmodel.challenge.challengeChain.ParticipantsChainViewModel
import com.teamforce.thanksapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ParticipantsChainFragment : BaseFragment<FragmentParticipantsChainBinding>(FragmentParticipantsChainBinding::inflate) {

    private val viewModel: ParticipantsChainViewModel by viewModels()

    private var chainId: Long? = null

    private var chainName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            chainId = it.getLong(CHAIN_ID)
            chainName = it.getString(DetailsMainChallengeChainFragment.CHAIN_NAME)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        val listAdapter = ParticipantChainAdapter()
        setAdapter(listAdapter)
        chainId?.let {
            loadData(chainId = it, listAdapter)
        }

    }

    override fun applyTheme() {

    }

    private fun setAdapter(listAdapter: ParticipantChainAdapter) {
        binding.participantsRv.apply {
            this.adapter = listAdapter.withLoadStateHeaderAndFooter(
                header = HistoryLoadStateAdapter(),
                footer = HistoryLoadStateAdapter()
            )
            this.addItemDecoration(
                VerticalDividerDecoratorForListWithBottomBar(
                    8,
                    listAdapter.itemCount
                )
            )
        }

        listAdapter.addLoadStateListener { loadState ->
            if(listAdapter.itemCount == 0){
                showEmptyView()
            }else {
                hideEmptyView()
            }

            if (loadState.refresh is LoadState.Error) {
                showConnectionError()
            } else {
                hideConnectionError()
            }
        }

        listAdapter.addLoadStateListener { state ->
            binding.swipeRefreshLayout.isRefreshing = state.refresh == LoadState.Loading
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            listAdapter.refresh()
            binding.swipeRefreshLayout.isRefreshing = true
        }

        listAdapter.onSomeonesClicked = { userId: Long, clickedView: View ->
            val bundle = Bundle().apply {
                putInt(SomeonesProfileFragment.USER_ID, userId.toInt())
            }
            clickedView.findNavController().navigateSafely(
                R.id.action_global_someonesProfileFragment,
                bundle,
                OptionsTransaction().optionForTransactionWithSaveBackStack
            )

        }

    }

    private fun loadData(chainId: Long, listAdapter: ParticipantChainAdapter){
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.loadParticipants(chainId).collectLatest {
                listAdapter.submitData(it)
                hideConnectionError()
            }
        }
    }

    override fun onDestroyView() {
        binding.participantsRv.adapter = null
        super.onDestroyView()
    }

    private fun showEmptyView(){
        binding.emptyView.root.visible()
        binding.participantsRv.invisible()
    }

    private fun hideEmptyView(){
        binding.emptyView.root.invisible()
        binding.participantsRv.visible()
    }

    private fun showConnectionError() {
        binding.error.root.visible()
        binding.participantsRv.invisible()
    }

    private fun hideConnectionError() {
        binding.error.root.invisible()
        binding.participantsRv.visible()
    }

    private fun handleTopAppBar() {
        binding.header.toolbar.title = getString(R.string.challenge_chain_participants)
        binding.header.closeBtn.visible()
        binding.header.closeBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.header.toolbar.subtitle = HtmlCompat.fromHtml(
            String.format(
                getString(R.string.challenge_chain_name_prefix),
                chainName.orEmpty()
            ), HtmlCompat.FROM_HTML_MODE_COMPACT
        )


        binding.participantsRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val currentElevation = binding.header.toolbar.translationZ
                val targetElevation = if (recyclerView.computeVerticalScrollOffset() > 20.dp) {
                    3.dp.toFloat()
                } else {
                    0.toFloat()
                }

                if (currentElevation != targetElevation) {
                    binding.header.toolbar.translationZ = targetElevation
                }
            }
        })
    }

    companion object {


        @JvmStatic
        fun newInstance(chainId: Long = 0L) =
            ParticipantsChainFragment().apply {
                arguments = Bundle().apply {
                    putLong(CHAIN_ID, chainId)
                }
            }
    }
}