package com.teamforce.thanksapp.presentation.fragment.challenges.createChallenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentSelectAccountsDialogBinding
import com.teamforce.thanksapp.domain.models.challenge.createChallenge.CreateChallengeSettingsModel
import com.teamforce.thanksapp.domain.models.challenge.createChallenge.DebitAccountModel
import com.teamforce.thanksapp.presentation.adapter.challenge.createChallenge.DebitAccountsAdapter
import com.teamforce.thanksapp.presentation.viewmodel.challenge.CreateChallengeViewModel
import com.teamforce.thanksapp.utils.getParcelableExt
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectAccountsDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSelectAccountsDialogBinding? = null
    private val binding get() = checkNotNull(_binding) { "Binding is null" }

    private var settingsChallenge: CreateChallengeSettingsModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            settingsChallenge = it.getParcelableExt(
                SELECT_ACCOUNT_BUNDLE_KEY,
                CreateChallengeSettingsModel::class.java
            )
        }
    }

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentSelectAccountsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRv()
        settingsChallenge?.let {
            (binding.accountsListRv.adapter as DebitAccountsAdapter).submitList(
                it.accounts
            )
        }
    }

    private fun initRv() {
        with(binding.accountsListRv) {
            adapter = DebitAccountsAdapter(::onAccountClicked)
        }
    }

    private fun onAccountClicked(account: DebitAccountModel) {
        // viewModel.saveDebitAccount(account)
        setFragmentResult(
            SELECT_ACCOUNT_REQUEST_KEY, bundleOf(
                SELECT_ACCOUNT_BUNDLE_KEY to account
            )
        )

        this.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        const val SELECT_ACCOUNT_REQUEST_KEY = "Select account Request Key"
        const val SELECT_ACCOUNT_BUNDLE_KEY = "Select account Bundle Key"

        @JvmStatic
        fun newInstance() =
            SelectAccountsDialogFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}