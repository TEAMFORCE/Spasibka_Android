package com.teamforce.thanksapp.presentation.fragment.historyScreen

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentHistoryListBinding
import com.teamforce.thanksapp.presentation.adapter.decorators.VerticalDividerDecoratorForListWithBottomBar
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.adapter.history.HistoryPageAdapter
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts
import com.teamforce.thanksapp.presentation.viewmodel.history.HistoryListViewModel
import com.teamforce.thanksapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class HistoryListFragment : Fragment(R.layout.fragment_history_list) {

    private val binding: FragmentHistoryListBinding by viewBinding()
    private val viewModel: HistoryListViewModel by activityViewModels()
    private var listAdapter: HistoryPageAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sentOnly = arguments?.getInt(SENT_ONLY_KEY)!!
        val receivedOnly = arguments?.getInt(RECEIVED_ONLY)!!

        listAdapter = HistoryPageAdapter(
            viewModel.getUserId(), viewModel.getUsername(),
            ::onCancelClicked, ::onSomeonesClicked, ::onChallengeClicked
        )

        binding.list.apply {
            this.adapter = listAdapter?.withLoadStateHeaderAndFooter(
                header = HistoryLoadStateAdapter(),
                footer = HistoryLoadStateAdapter()
            )
            layoutManager = LinearLayoutManager(requireContext())
            this.addItemDecoration(
                VerticalDividerDecoratorForListWithBottomBar(
                    8,
                    listAdapter!!.itemCount
                )
            )

        }

        listAdapter?.addLoadStateListener { state ->
            binding.refreshLayout.isRefreshing = state.refresh == LoadState.Loading
        }

        binding.refreshLayout.setOnRefreshListener {
            loadData(sentOnly, receivedOnly)
            listAdapter?.refresh()
            binding.refreshLayout.isRefreshing = true
        }

        loadData(sentOnly, receivedOnly)

        viewModel.cancellationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    listAdapter?.refresh()
                }
                else -> {}
            }
        }

        viewModel.filterHasBeenUpdated.observe(viewLifecycleOwner){
            if(it) loadData(sentOnly, receivedOnly)
        }

        viewModel.internetError.observe(viewLifecycleOwner) {
            showSnackBarAboutNetworkProblem(view, requireContext())
        }

        listAdapter!!.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Error) {
                showSnackBarAboutNetworkProblem(
                    view,
                    requireContext()
                )
               // showConnectionError()
            }
        }
    }

    private fun loadData(sentOnly: Int, receivedOnly: Int){
        viewLifecycleOwner.lifecycleScope.launch {
            if (sentOnly == -1 && receivedOnly == -1) {
                binding.refreshLayout.isRefreshing = false
                viewModel.getAllHistory(requireContext()).collectLatest {
                    listAdapter?.submitData(it)
                    hideConnectionError()
                }
            } else if (sentOnly == 1 && receivedOnly == -1) {
                binding.refreshLayout.isRefreshing = false
                viewModel.getSent(requireContext()).collectLatest {
                    listAdapter?.submitData(it)
                    hideConnectionError()

                }
            } else {
                binding.refreshLayout.isRefreshing = false
                viewModel.getReceived(requireContext()).collectLatest {
                    listAdapter?.submitData(it)
                    hideConnectionError()
                }
            }
        }
    }

    private fun showConnectionError(){
        binding.error.root.visible()
        binding.list.invisible()
    }

    private fun hideConnectionError(){
        binding.error.root.invisible()
        binding.list.visible()
    }

    override fun onDestroyView() {
        binding.list.adapter = null
        listAdapter = null
        super.onDestroyView()
    }

    companion object {
        private const val SENT_ONLY_KEY = "sent_only"
        private const val RECEIVED_ONLY = "received_only"

        @JvmStatic
        fun newInstance(
            sentOnly: Int,
            receivedOnly: Int
        ) = HistoryListFragment().apply {
            arguments = Bundle().apply {
                putInt(SENT_ONLY_KEY, sentOnly)
                putInt(RECEIVED_ONLY, receivedOnly)
            }
        }
    }

    private fun onCancelClicked(id: Int) {
        showAlertDialogForCancelTransaction(id)
    }

    private fun onChallengeClicked(challengeId: Int) {
        val bundle = Bundle()
        bundle.putInt(ChallengesConsts.CHALLENGER_ID, challengeId)
        findNavController().navigateSafely(
            R.id.action_global_detailsMainChallengeFragment,
            bundle,
            OptionsTransaction().optionForAdditionalInfoFeedFragment
        )
    }

    private fun onSomeonesClicked(userId: Int) {
        val bundle = Bundle()
        bundle.putInt(Consts.USER_ID, userId)
        findNavController().navigateSafely(
            R.id.action_global_someonesProfileFragment,
            bundle,
            OptionsTransaction().optionForAdditionalInfoFeedFragment
        )
    }

    private fun showAlertDialogForCancelTransaction(idTransaction: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(requireContext().resources?.getString(R.string.cancelTransaction))

            .setNegativeButton(requireContext().resources.getString(R.string.decline)) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(requireContext().resources.getString(R.string.accept)) { dialog, which ->
                dialog.cancel()
                viewModel.cancelUserTransaction(idTransaction)
            }
            .show()
    }
}