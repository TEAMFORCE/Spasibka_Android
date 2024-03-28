package com.teamforce.thanksapp.presentation.fragment.templates.createTemplate

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.distinctUntilChanged
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.databinding.FragmentSettingsTemplateBinding
import com.teamforce.thanksapp.domain.models.challenge.ChallengeType
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.viewmodel.templates.CreateTemplateViewModel
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.parceleable
import com.teamforce.thanksapp.utils.visible
import kotlinx.parcelize.Parcelize


class SettingsTemplateFragment : BaseFragment<FragmentSettingsTemplateBinding>(FragmentSettingsTemplateBinding::inflate) {

    private val viewModel: CreateTemplateViewModel by activityViewModels()

    private var scopeOfTemplates = ScopeRequestParams.TEMPLATES

    private var outerSettingsForTemplate: SettingsForCreateTemplate? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            outerSettingsForTemplate = it.parceleable(SETTINGS_TEMPLATE_BUNDLE_KEY)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        outerSettingsForTemplate?.let {
            setSettingsTemplatesBaseOnTemplate(it)
        }

        viewModel.loadUserProfile()
        binding.closeBtn.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        binding.continueBtn.setOnClickListener {
            val settings = SettingsForCreateTemplate(
                challengeType = if(binding.switchChallengeTypeIsVoting.isChecked) ChallengeType.VOTING else ChallengeType.DEFAULT,
                severalReports = binding.switchSendSeveralReports.isChecked,
                showContenders = binding.switchShowContenders.isChecked,
                scopeTemplates = scopeOfTemplates
            )
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.successfullySavedSettingsForChallenge),
                Toast.LENGTH_SHORT
            ).show()
            setFragmentResult(
                SETTINGS_TEMPLATE_REQUEST_KEY, bundleOf(
                    SETTINGS_TEMPLATE_BUNDLE_KEY to settings)
            )
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        listenersSwitches()
    }

    override fun applyTheme() {

    }

    private fun setSettingsTemplatesBaseOnTemplate(settingsForCreateTemplate: SettingsForCreateTemplate) {
        binding.switchShowContenders.isChecked = settingsForCreateTemplate.showContenders
        binding.switchSendSeveralReports.isChecked = settingsForCreateTemplate.severalReports
        binding.switchChallengeTypeIsVoting.isChecked =
            settingsForCreateTemplate.challengeType == ChallengeType.VOTING
        when (settingsForCreateTemplate.scopeTemplates) {
            ScopeRequestParams.TEMPLATES -> {
                binding.switchShowOnlyMe.isChecked = true
                scopeOfTemplates = ScopeRequestParams.TEMPLATES
                binding.switchShowMyOrg.isChecked = false
                binding.switchShowEveryone.isChecked = false
            }
            ScopeRequestParams.ORGANIZATION -> {
                binding.switchShowMyOrg.isChecked = true
                scopeOfTemplates = ScopeRequestParams.ORGANIZATION
                binding.switchShowOnlyMe.isChecked = false
                binding.switchShowEveryone.isChecked = false
            }
            ScopeRequestParams.COMMON -> {
                binding.switchShowEveryone.isChecked = true
                scopeOfTemplates = ScopeRequestParams.COMMON
                binding.switchShowOnlyMe.isChecked = false
                binding.switchShowMyOrg.isChecked = false
            }
        }
    }

    private fun listenersSwitches() {

        viewModel.profile.distinctUntilChanged().observe(viewLifecycleOwner){
            it?.let { profileModel ->
                setVisibilityOfScopeSettings(profileModel)
            }
        }

        binding.switchShowOnlyMe.setOnCheckedChangeListener(
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    scopeOfTemplates = ScopeRequestParams.TEMPLATES
                    binding.switchShowMyOrg.isChecked = false
                    binding.switchShowEveryone.isChecked = false
                }
            })
        binding.switchShowMyOrg.setOnCheckedChangeListener(
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    scopeOfTemplates = ScopeRequestParams.ORGANIZATION
                    binding.switchShowOnlyMe.isChecked = false
                    binding.switchShowEveryone.isChecked = false
                }
            })
        binding.switchShowEveryone.setOnCheckedChangeListener(
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    scopeOfTemplates = ScopeRequestParams.COMMON
                    binding.switchShowOnlyMe.isChecked = false
                    binding.switchShowMyOrg.isChecked = false
                }
            })

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

    private fun setVisibilityOfScopeSettings(profileModel: ProfileModel){
        if(profileModel.profile.isHeadOfACurrentCommunity){
            binding.showToOrgCard.visible()
        }else{
            binding.showToOrgCard.invisible()
        }

        if(profileModel.profile.superuser){
            binding.showToEveryoneCard.visible()
        }else{
            binding.showToEveryoneCard.invisible()
        }

    }
    @Parcelize
    data class SettingsForCreateTemplate(
        var challengeType: ChallengeType = ChallengeType.DEFAULT,
        var severalReports: Boolean = true,
        var scopeTemplates: ScopeRequestParams = ScopeRequestParams.TEMPLATES,
        var showContenders: Boolean = true
    ) : Parcelable


    companion object {

        const val SETTINGS_TEMPLATE_REQUEST_KEY = "Settings Template Request Key"
        const val SETTINGS_TEMPLATE_BUNDLE_KEY = "Settings Template Bundle Key"

        @JvmStatic
        fun newInstance() =
            SettingsTemplateFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}