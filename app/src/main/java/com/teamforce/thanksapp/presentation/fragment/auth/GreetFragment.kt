package com.teamforce.thanksapp.presentation.fragment.auth

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentGreetBinding
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.profileScreen.SettingsFragment
import com.teamforce.thanksapp.presentation.fragment.profileScreen.WhatKindOfDocument
import com.teamforce.thanksapp.presentation.viewmodel.auth.GreetViewModel
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.SpannableUtils.Companion.createClickableSpannable
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.navigateSafely
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class GreetFragment : BaseFragment<FragmentGreetBinding>(FragmentGreetBinding::inflate) {

    private val viewModel: GreetViewModel by viewModels()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {
            findNavController().navigateSafely(R.id.action_greetFragment_to_loginFragment)
        }
        binding.privacyPolicyAndUserAgreementTv.setOnClickListener {
            findNavController().navigateSafely(R.id.action_greetFragment_to_privacyPolicyFragment2)
        }

        binding.checkBox.setOnCheckedChangeListener { compoundButton, isChecked ->
            binding.button.isEnabled = isChecked
        }


        inflateAgreementString()
    }


    private fun inflateAgreementString() {
        binding.privacyPolicyAndUserAgreementTv.movementMethod = LinkMovementMethod.getInstance()

        val spannable = SpannableStringBuilder(
        ).append(
            createClickableSpannable(
                requireContext().getString(R.string.agree_with),
               Branding.brand.colorsJson.generalContrastColor
            ) {
            },
        ).append(
            createClickableSpannable(
                " " + requireContext().getString(R.string.privacy_policy_greeting) + " ",
                Branding.brand.colorsJson.mainBrandColor
            ) {
                // Клик по privacy policy
                val bundle = Bundle()
                bundle.putString(SettingsFragment.TYPE_OF_DOCUMENT, WhatKindOfDocument.PRIVACY_POLICY.name)
                findNavController().navigateSafely(
                    R.id.action_greetFragment_to_privacyPolicyFragment2,
                    bundle,
                    OptionsTransaction().optionForEditProfile
                )
            }
        ).append(
            createClickableSpannable(
                requireContext().getString(R.string.and_conditions),
                Branding.brand.colorsJson.generalContrastColor
            ) {
            }
        ).append(
            createClickableSpannable(
                " " + requireContext().getString(R.string.user_agreement_greeting),
                Branding.brand.colorsJson.mainBrandColor
            ) {
                // Клик по userAgreement
                val bundle = Bundle()
                bundle.putString(SettingsFragment.TYPE_OF_DOCUMENT, WhatKindOfDocument.USER_AGREEMENT.name)
                findNavController().navigateSafely(
                    R.id.action_greetFragment_to_privacyPolicyFragment2,
                    bundle,
                    OptionsTransaction().optionForEditProfile
                )
            }
        )

        binding.privacyPolicyAndUserAgreementTv.text = spannable
    }


    companion object {
        private const val XCODE = "xCode"
        private const val XID = "xId"
        private const val ORGID = "organization_id"


        @JvmStatic
        fun newInstance(xCode: String, xId: String, orgId: String) =
            GreetFragment().apply {
                arguments = Bundle().apply {
                    putString(XCODE, xCode)
                    putString(XID, xId)
                    putString(ORGID, orgId)
                }
            }
    }

    override fun applyTheme() {
    }

}