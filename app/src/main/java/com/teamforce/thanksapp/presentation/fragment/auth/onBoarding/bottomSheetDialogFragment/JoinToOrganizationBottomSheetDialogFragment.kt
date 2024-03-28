package com.teamforce.thanksapp.presentation.fragment.auth.onBoarding.bottomSheetDialogFragment

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentCreateCommunityBottomSheetDialogBinding
import com.teamforce.thanksapp.databinding.FragmentJoinToOrganizationBottomSheetDialogBinding
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.presentation.fragment.auth.LoginFragment
import com.teamforce.thanksapp.presentation.viewmodel.onboarding.JoinToOrganizationViewModel
import com.teamforce.thanksapp.presentation.viewmodel.onboarding.OnboardingViewModel
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.isNullOrEmptyMy
import com.teamforce.thanksapp.utils.navigateSafely
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JoinToOrganizationBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<FragmentJoinToOrganizationBottomSheetDialogBinding>(
        FragmentJoinToOrganizationBottomSheetDialogBinding::inflate
    ) {

    private val viewModel: JoinToOrganizationViewModel by viewModels()
    private var sharedKey: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listenerInputField()
        binding.continueBtn.setOnClickListener {
            getSharedKeyFromInvitation(binding.inviteKeyEt.text.toString().toUri())
            sharedKey?.let { viewModel.getOrgByInvitation(it) }

        }
        binding.cancelButton.setOnClickListener {
            this.dismiss()
        }

        viewModel.orgByInvitation.observe(viewLifecycleOwner) {
            if (it != null) {
                this.dismiss()
                val bundle = Bundle()
                bundle.putString(LoginFragment.ARG_REF, sharedKey)
                findNavController().navigateSafely(
                    R.id.action_joinToOrganizationBottomSheetDialogFragment_to_loginFragment,
                    bundle,
                    OptionsTransaction().optionForEditProfile
                )
            }
        }

        viewModel.orgByInvitationError.observe(this) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
    }

    private fun listenerInputField() {
        binding.inviteKeyEt.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.continueBtn.isEnabled = s.trim().isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun getSharedKeyFromInvitation(invite: Uri) {
        sharedKey = invite.getQueryParameter(LoginFragment.ARG_REF)
        if(sharedKey.isNullOrEmptyMy()) sharedKey = invite.toString()
    }

    override fun applyTheme() {

    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            JoinToOrganizationBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}