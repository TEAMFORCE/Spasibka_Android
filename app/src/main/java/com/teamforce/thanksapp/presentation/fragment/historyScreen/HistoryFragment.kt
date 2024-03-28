package com.teamforce.thanksapp.presentation.fragment.historyScreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.teamforce.thanksapp.NotificationSharedViewModel
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentHistoryBinding
import com.teamforce.thanksapp.presentation.adapter.decorators.VerticalDividerDecoratorForListWithBottomBar
import com.teamforce.thanksapp.presentation.adapter.history.HistoryGroupPageAdapter
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.adapter.history.PagerAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.viewmodel.history.HistoryListViewModel
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.showSnackBarAboutNetworkProblem
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>(FragmentHistoryBinding::inflate) {

    private var pagerAdapter: PagerAdapter? = null
    private var mediator: TabLayoutMediator? = null

    private val viewModel: HistoryListViewModel by activityViewModels()

    private val listAdapter = HistoryGroupPageAdapter(::onUserClicked)

    private var page: Int? = null
    private var transactionId: Int? = null
    private var message: CharSequence? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            page = it.getInt(Consts.CURRENT_POSITION_PAGE_IN_VIEW_PAGER)
            transactionId = it.getInt(Consts.TRANSACTION_ID)
            message = it.getCharSequence(Consts.MESSSAGE)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        if (transactionId != null && message != null) {
            val bundle = Bundle()
            bundle.putInt(Consts.TRANSACTION_ID, transactionId!!)
            bundle.putCharSequence(Consts.MESSSAGE, message!!)
            findNavController().navigate(
                R.id.action_historyFragment_to_additionalInfoTransactionBottomSheetFragment2,
                bundle
            )
            transactionId = null
        }

        binding.apply {

            pagerAdapter = PagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
            viewPager.adapter = pagerAdapter

            initTabLayout()
        }

        binding.list.apply {
            this.adapter = listAdapter.withLoadStateHeaderAndFooter(
                header = HistoryLoadStateAdapter(),
                footer = HistoryLoadStateAdapter()
            )
            layoutManager = LinearLayoutManager(requireContext())
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

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            binding.refreshLayout.isRefreshing = false
            viewModel.getHistoryGroup(requireContext()).collectLatest {
                listAdapter.submitData(it)
            }
        }
        listAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Error) {
                showSnackBarAboutNetworkProblem(
                    view,
                    requireContext()
                )
            }
        }

        binding.isGrouped.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.viewPager.invisible()
                binding.tabGroup.invisible()
                binding.refreshLayout.visible()
            } else {
                binding.viewPager.visible()
                binding.tabGroup.visible()
                binding.refreshLayout.invisible()
            }
        }
    }

    private fun onUserClicked(userId: Long) {
        val bundle = Bundle()
        bundle.putInt(Consts.USER_ID, userId.toInt())
        findNavController().navigateSafely(
            R.id.action_global_someonesProfileFragment,
            bundle,
            OptionsTransaction().optionForAdditionalInfoFeedFragment
        )
    }


    private fun initTabLayout() {
        binding.apply {
            viewPager.adapter = pagerAdapter

            mediator = TabLayoutMediator(tabGroup, viewPager) { tab, pos ->
            }
            mediator?.attach()

        }

        val tabTitle = listOf(
            requireContext().getString(R.string.allHistory),
            requireContext().getString(R.string.received),
            requireContext().getString(R.string.sended)
        )
        binding.tabGroup.setTabTitles(tabTitle)

        page?.let { page ->
            binding.viewPager.setCurrentItem(page, false)
        }
    }

    override fun onDestroyView() {
        mediator?.detach()
        mediator = null
        binding.viewPager.adapter = null
        pagerAdapter = null
        super.onDestroyView()

    }

    private fun handleTopAppBar() {
        binding.header.toolbar.title = requireContext().getString(R.string.history_label)
        binding.header.filterIv.visible()
        binding.header.filterIv.setOnClickListener {
            findNavController().navigateSafely(R.id.action_historyFragment_to_filterHistoryTransactionsBottomSheetFragment)
        }
        binding.header.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }


    companion object {
        const val TAG = "HistoryFragment"
    }

    override fun applyTheme() {

    }
}
