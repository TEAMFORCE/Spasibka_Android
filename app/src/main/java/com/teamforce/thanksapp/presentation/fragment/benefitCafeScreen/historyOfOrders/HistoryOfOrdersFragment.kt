package com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.historyOfOrders

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentHistoryOfOrdersBinding
import com.teamforce.thanksapp.presentation.adapter.benefitCafe.HistoryOfOrdersAdapter
import com.teamforce.thanksapp.presentation.adapter.decorators.VerticalDividerDecoratorForListWithBottomBar
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.BenefitConsts.MARKET_ID
import com.teamforce.thanksapp.presentation.viewmodel.benefit.HistoryOfOrdersViewModel
import com.teamforce.thanksapp.utils.ViewLifecycleDelegate
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.showSnackBarAboutNetworkProblem
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryOfOrdersFragment : BaseFragment<FragmentHistoryOfOrdersBinding>(FragmentHistoryOfOrdersBinding::inflate) {

    private val viewModel: HistoryOfOrdersViewModel by activityViewModels()

    private var marketId: Int? = null

    private val listAdapter by ViewLifecycleDelegate {
        HistoryOfOrdersAdapter(::onOrderClicked)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            marketId = it.getInt(MARKET_ID)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        setupAdapter(view)
        if(marketId != null) loadOrders(marketId!!)

        binding.fabFilter.setOnClickListener {
            findNavController().navigateSafely(R.id.action_historyOfOrdersFragment_to_filtersHistoryOfOrders)
        }
    }

    override fun applyTheme() {

    }

    private fun setupAdapter(view: View) {
        binding.list.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
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

        listAdapter.addLoadStateListener { state ->
            binding.refreshLayout.isRefreshing = state.refresh == LoadState.Loading
        }

        binding.refreshLayout.setOnRefreshListener {
            listAdapter.refresh()
            binding.refreshLayout.isRefreshing = true
        }

        listAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Error) {
                showSnackBarAboutNetworkProblem(
                    view,
                    requireContext()
                )
            }
        }
    }

    private fun loadOrders(marketId: Int) {
        viewModel.statusList.observe(viewLifecycleOwner){
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getOrders(marketId).collectLatest {
                    binding.list.visible()
                    listAdapter.submitData(it)
                }
            }
        }


    }

    private fun onOrderClicked(offerId: Int) {

    }


    private fun handleTopAppBar(){
        binding.header.toolbar.title = requireContext().getString(R.string.historyOffers)
        binding.header.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        binding.list.adapter = null
        super.onDestroyView()
    }


    companion object {

        @JvmStatic
        fun newInstance() =
            HistoryOfOrdersFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}