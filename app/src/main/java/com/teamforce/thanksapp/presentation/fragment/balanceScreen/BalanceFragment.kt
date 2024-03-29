package com.teamforce.thanksapp.presentation.fragment.balanceScreen

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teamforce.thanksapp.NotificationSharedViewModel
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentBalanceBinding
import com.teamforce.thanksapp.presentation.adapter.history.HistoryPageAdapter
import com.teamforce.thanksapp.presentation.adapter.transactions.UsersMiniListAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts
import com.teamforce.thanksapp.presentation.viewmodel.BalanceViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Extensions.setAvatarInTopAppBar
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


@AndroidEntryPoint
class BalanceFragment : BaseFragment<FragmentBalanceBinding>(FragmentBalanceBinding::inflate) {


    private val viewModel: BalanceViewModel by activityViewModels()
    private val sharedViewModel: NotificationSharedViewModel by activityViewModels()
    private var historyListAdapter: HistoryPageAdapter? = null
    private var transactionsListAdapter: UsersMiniListAdapter? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTransactionsAdapter()
        setHistoryAdapter()
        handleTopAppBar()
        if (viewModel.balance.value == null) {
            loadBalanceData()
            setBalanceData()
            viewModel.isLoading.distinctUntilChanged().observe(
                viewLifecycleOwner
            ) { isLoading ->
                binding.swipeRefreshLayout.isRefreshing = isLoading
            }
        } else {
            setBalanceData()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadBalanceData()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.internetError.observe(viewLifecycleOwner) {
            showSnackBarAboutNetworkProblem(view, requireContext())
        }

        viewModel.error.observe(viewLifecycleOwner) {
            if(it.isNotNullOrEmptyMy())
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        binding.balanceCardLayout.onMyCountCardClicked = {
            handleMyCountCardClick()
        }

        binding.balanceCardLayout.onMyRemainsCardClicked = {
            handleMyRemainsCardClick()
        }

        binding.allHistoryTv.setOnClickListener {
            findNavController().navigate(
                R.id.history_graph,
                null
            )
        }

    }

    private fun setTransactionsAdapter() {
        transactionsListAdapter = UsersMiniListAdapter()
        binding.transferList.adapter = transactionsListAdapter
        viewModel.getUsers()
        viewModel.users.observe(viewLifecycleOwner) {
            transactionsListAdapter?.submitList(it)
        }
        transactionsListAdapter?.let {
            it.onNewTransactionClicked = {
                findNavController().navigate(
                    R.id.transaction_graph,
                    null
                )
            }
            it.onSomeonesClicked = {
                val bundle = Bundle()
                bundle.putParcelable(Consts.PROFILE_DATA, it)
                findNavController().navigate(
                    R.id.transaction_graph,
                    bundle
                )
            }
        }

    }

    private fun setHistoryAdapter() {
        historyListAdapter = HistoryPageAdapter(
            viewModel.getUserId(), viewModel.getUsername(),
            ::onCancelClicked, ::onSomeonesClicked, ::onChallengeClicked
        )

        binding.historyList.apply {
            adapter = historyListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllHistory(requireContext()).collectLatest {
                historyListAdapter?.submitData(it)
            }
        }

        viewModel.cancellationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    historyListAdapter?.refresh()
                }
                else -> {}
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


    private fun handleMyCountCardClick() {
        val bundle = Bundle()
        bundle.putInt(Consts.CURRENT_POSITION_PAGE_IN_VIEW_PAGER, 1)
        findNavController().navigate(
            R.id.history_graph,
            bundle,
            OptionsTransaction().optionForTransaction
        )
    }

    private fun handleMyRemainsCardClick() {
        val bundle = Bundle()
        bundle.putInt(Consts.CURRENT_POSITION_PAGE_IN_VIEW_PAGER, 2)
        findNavController().navigate(
            R.id.history_graph,
            bundle,
            OptionsTransaction().optionForTransaction
        )
    }

    private fun showErrorAboutConnection() {
        //  binding.error.root.visible()
        binding.wholeScreen.invisible()
    }

    private fun hideErrorAboutConnection() {
        //  binding.error.root.invisible()
        binding.wholeScreen.visible()
    }


    private fun loadBalanceData() {
        viewModel.loadUserBalance()
    }

    private fun setBalanceData() {
        viewModel.balance.observe(viewLifecycleOwner) {
            hideErrorAboutConnection()
            binding.balanceCardLayout.setMyCountText(it.income.amount.toString())
            binding.balanceCardLayout.setRemainsText(it.distribute.amount.toString())
            binding.balanceCardLayout.setCancelledAmount(it.distribute.cancelled.toString())
            binding.balanceCardLayout.setApprovalAmount(it.income.frozen.toString())
            binding.balanceCardLayout.setDaysBeforeBurn(getDaysAmountFromNowToTarget(it.distribute.expireDate))
        }
    }

    private fun handleTopAppBar() {
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

        binding.nestedScroll.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val currentElevation = binding.header.toolbar.translationZ
            val targetElevation = if (scrollY > 5.dp) {
                2.dp.toFloat()
            } else {
                0.toFloat()
            }
            if (currentElevation != targetElevation) {
                binding.header.toolbar.translationZ = targetElevation
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


    companion object {

        const val TAG = "BalanceFragment"

        @JvmStatic
        fun newInstance() = BalanceFragment()
    }

    override fun applyTheme() {
        binding.header.notifyBadgeText.setTextColor(Color.parseColor(appTheme.generalBackgroundColor))
    }
}
