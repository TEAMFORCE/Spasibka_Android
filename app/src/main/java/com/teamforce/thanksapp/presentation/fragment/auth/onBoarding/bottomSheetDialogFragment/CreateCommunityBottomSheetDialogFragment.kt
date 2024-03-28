package com.teamforce.thanksapp.presentation.fragment.auth.onBoarding.bottomSheetDialogFragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentCreateCommunityBottomSheetDialogBinding
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.presentation.viewmodel.onboarding.OnboardingViewModel
import com.teamforce.thanksapp.utils.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CreateCommunityBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<FragmentCreateCommunityBottomSheetDialogBinding>(
        FragmentCreateCommunityBottomSheetDialogBinding::inflate
    ) {

    private val sharedViewModel: OnboardingViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listenerInputField()
        binding.continueBtn.setOnClickListener {
            sharedViewModel.createCommunity(binding.nameOrgEt.text.toString())
        }
        binding.cancelButton.setOnClickListener {
            this.dismiss()
        }
        sharedViewModel.orgName.observe(viewLifecycleOwner) {
            if (it != null) {
                val bundle = Bundle()
                findNavController().navigateSafely(
                    R.id.action_createCommunityBottomSheetDialogFragment_to_settingsPeriodFragment,
                    bundle,
                    OptionsTransaction().optionForEditProfile
                )
            }
        }
        sharedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showProgressBar() else hideProgressBar()
        }
    }

    private fun hideProgressBar() {
        binding.progressBar.invisible()
        binding.continueBtn.isEnabled = true
    }

    private fun showProgressBar() {
        binding.progressBar.visible()
        binding.continueBtn.isEnabled = false
    }

    private fun listenerInputField() {
        binding.nameOrgEt.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.continueBtn.isEnabled = s.trim().isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }


    override fun applyTheme() {
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateCommunityBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}