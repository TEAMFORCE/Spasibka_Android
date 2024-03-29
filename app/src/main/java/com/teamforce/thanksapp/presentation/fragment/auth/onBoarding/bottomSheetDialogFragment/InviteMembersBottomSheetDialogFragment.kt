package com.teamforce.thanksapp.presentation.fragment.auth.onBoarding.bottomSheetDialogFragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentInviteMembersBottomSheetDialogBinding
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.presentation.viewmodel.onboarding.InviteMembersViewModel
import com.teamforce.thanksapp.utils.Consts.COMMUNITY_ID
import com.teamforce.thanksapp.utils.copyTextToClipboard
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InviteMembersBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<FragmentInviteMembersBottomSheetDialogBinding>(
        FragmentInviteMembersBottomSheetDialogBinding::inflate
    ) {


    private val viewModel: InviteMembersViewModel by viewModels()
    private var communityId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            communityId = it.getString(COMMUNITY_ID)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getInvitationData(communityId)
        viewModel.invitationData.observe(viewLifecycleOwner) { data ->
            data?.let {
                showInviteKeyField()
                binding.inviteKeyEt.setText(it.invite_link)
                binding.titleTv.text = requireContext().getString(
                    R.string.onboarding_invitation_title,
                    it.organization_name
                )
            }
        }
        viewModel.invitationError.observe(viewLifecycleOwner) { booleanError ->
            if(booleanError == true) {
                binding.titleTv.text = requireContext().getString(
                    R.string.onboarding_invitation_error)
                hideInviteKeyField()
            }
        }
        binding.inviteKeyTextInput.setEndIconOnClickListener {
            copyTextToClipboard(
                binding.inviteKeyEt.text.toString(),
                requireContext().getString(R.string.onboarding_invitation_key),
                requireContext()
            )
        }
        binding.continueBtn.setOnClickListener {
            this.dismiss()
        }

        viewModel.isLoading.observe(viewLifecycleOwner){ isLoading ->
            if(isLoading) showProgressBar() else hideProgressBar()
        }
    }

    private fun hideInviteKeyField(){
        binding.inviteKeyTextInput.invisible()
        binding.inviteKeyLabel.invisible()
    }

    private fun showInviteKeyField(){
        binding.inviteKeyTextInput.visible()
        binding.inviteKeyLabel.visible()
    }

    private fun hideProgressBar() {
        binding.shimmerLayoutTitle.invisible()
        binding.shimmerLayoutTextField.invisible()
        binding.inviteKeyTextInput.visible()
        binding.titleTv.visible()
    }

    private fun showProgressBar() {
        binding.shimmerLayoutTitle.startShimmer()
        binding.shimmerLayoutTextField.startShimmer()
        binding.shimmerLayoutTitle.visible()
        binding.shimmerLayoutTextField.visible()
    }

    override fun applyTheme() {

    }

    companion object {

        @JvmStatic
        fun newInstance(communityId: Int) =
            InviteMembersBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(COMMUNITY_ID, communityId.toString())
                }
            }
    }
}