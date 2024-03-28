package com.teamforce.thanksapp.presentation.fragment.auth.onBoarding

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentLastOnBoardingScreenBinding
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.navigateSafely


class LastOnBoardingScreenFragment :
    BaseFragment<FragmentLastOnBoardingScreenBinding>(FragmentLastOnBoardingScreenBinding::inflate) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.actionBtn.setOnClickListener {
            findNavController().navigateSafely(R.id.action_lastOnBoardingScreenFragment_to_mainFlowFragment,
            null,
            OptionsTransaction().optionForLastScreenOnBoarding)
        }
        binding.inviteBtn.setOnClickListener {
            findNavController().navigateSafely(R.id.action_lastOnBoardingScreenFragment_to_inviteMembersBottomSheetDialogFragment)
        }
    }

    override fun applyTheme() {

    }


    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LastOnBoardingScreenFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}