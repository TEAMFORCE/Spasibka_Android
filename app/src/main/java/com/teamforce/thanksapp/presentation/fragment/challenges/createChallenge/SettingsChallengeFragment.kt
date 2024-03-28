package com.teamforce.thanksapp.presentation.fragment.challenges.createChallenge

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentSettingsChallengeBinding
import com.teamforce.thanksapp.domain.models.challenge.createChallenge.TypesChallengeModel
import com.teamforce.thanksapp.presentation.base.BaseFullScreenDialogFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts
import com.teamforce.thanksapp.presentation.viewmodel.challenge.CreateChallengeViewModel
import com.teamforce.thanksapp.utils.getParcelableExt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize


@AndroidEntryPoint
class SettingsChallengeFragment : BaseFullScreenDialogFragment<FragmentSettingsChallengeBinding>(FragmentSettingsChallengeBinding::inflate) {

    private val viewModel: CreateChallengeViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSettingsChallenge()
        binding.continueBtn.setOnClickListener {
           saveChallengeSettings()
        }

        binding.closeBtn.setOnClickListener {
            findNavController().navigateUp()
        }


        listenersSwitches()
    }

    override fun applyTheme() {

    }

    private fun setSettingsChallenge(){
        viewModel.getChallengeSettings().apply {
            binding.switchShowContenders.isChecked = showContenders
            binding.switchSendSeveralReports.isChecked = multipleReports
            binding.switchChallengeTypeIsVoting.isChecked = challengeWithVoting
        }
    }



    private fun saveChallengeSettings(){
        binding.continueBtn.isEnabled = false
        binding.continueBtn.setBackgroundColor(requireContext().getColor(R.color.general_brand_secondary))

        viewModel.updateChallengeSettings { currentSettings ->
            currentSettings.copy(
                multipleReports = binding.switchSendSeveralReports.isChecked,
                showContenders = binding.switchShowContenders.isChecked,
                challengeWithVoting = binding.switchChallengeTypeIsVoting.isChecked
            )
        }
        setFragmentResult(SETTINGS_CHALLENGE_REQUEST_KEY, bundleOf(SETTINGS_CHALLENGE_BUNDLE_KEY to viewModel.getChallengeSettings()))
        Toast.makeText(
            requireContext(),
            requireContext().getString(R.string.successfullySavedSettingsForChallenge),
            Toast.LENGTH_SHORT
        ).show()
        findNavController().navigateUp()
    }



    private fun listenersSwitches() {
        binding.switchChallengeTypeIsVoting.setOnCheckedChangeListener(
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked) binding.switchShowContenders.isChecked = true

            })
        binding.switchShowContenders.setOnCheckedChangeListener(
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                if (!isChecked && binding.switchChallengeTypeIsVoting.isChecked) {
                    binding.switchShowContenders.isChecked = true
                }
            })

    }

    companion object {
        const val SETTINGS_CHALLENGE_REQUEST_KEY = "Settings Challenge Request Key"
        const val SETTINGS_CHALLENGE_BUNDLE_KEY = "Settings Challenge Bundle Key"

        @JvmStatic
        fun newInstance() =
            SettingsChallengeFragment().apply {
            }
    }
}