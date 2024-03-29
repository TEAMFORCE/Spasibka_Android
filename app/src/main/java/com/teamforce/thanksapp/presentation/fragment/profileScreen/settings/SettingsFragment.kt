package com.teamforce.thanksapp.presentation.fragment.profileScreen

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialElevationScale
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.entities.profile.OrganizationModel
import com.teamforce.thanksapp.databinding.FragmentSettingsBinding
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.profileScreen.settings.ListOfOrganizationsFragment
import com.teamforce.thanksapp.presentation.viewmodel.profile.SettingsViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Locals.ContextUtils
import com.teamforce.thanksapp.utils.Locals.Lang
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme
import com.vk.auth.api.models.AuthResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val viewModel: SettingsViewModel by activityViewModels()

    private var listOfOrg: MutableList<OrganizationModel> = mutableListOf()
    private var licenseEnd: String? = null
    private var currentLang: String? = null
    private var currentOrg: OrganizationModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.navContainer
            duration = 400.toLong()
            scrimColor = Color.WHITE
            // isDrawDebugEnabled = true
            setAllContainerColors(Color.WHITE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        requestData()
        setData()
        binding.currentOrgCard.setOnClickListener {
            if (currentOrg != null) {
                val bundle = Bundle()
                bundle.putParcelable(ListOfOrganizationsFragment.CURRENT_ORG, currentOrg)
//            val extras = FragmentNavigatorExtras(it to ListOfOrganizationsFragment.TRANSITION_CARD,
//            binding.exitBtn to TRANSITION_BTN)
                val extras = FragmentNavigatorExtras(it to ListOfOrganizationsFragment.TRANSITION_CARD)

                reenterTransition = MaterialElevationScale(true).apply {
                    duration = 400.toLong()
                }
                findNavController().navigate(
                    R.id.action_settingsFragment_to_listOfOrganizationsFragment,
                    bundle,
                    null,
                    extras
                )
            }
        }


        binding.langLinear.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(Consts.CURRENT_LANGUAGE, currentLang)
            findNavController().navigateSafely(
                R.id.action_settingsFragment_to_pickerLangFragment,
                bundle,
                OptionsTransaction().optionForEditProfile
            )
        }
        binding.feedback.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_settingsFragment_to_feedback,
                null,
                OptionsTransaction().optionForEditProfile
            )
        }

        binding.infoLinear.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(LICENSE_END, licenseEnd)
            findNavController().navigateSafely(
                R.id.action_settingsFragment_to_infoAppFragment,
                bundle,
                OptionsTransaction().optionForEditProfile
            )
        }
        binding.privacyPolicyLinear.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(TYPE_OF_DOCUMENT, WhatKindOfDocument.PRIVACY_POLICY.name)
            findNavController().navigateSafely(
                R.id.action_settingsFragment_to_privacyPolicyFragment,
                bundle,
                OptionsTransaction().optionForEditProfile
            )
        }

        binding.userAgreementLinear.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(TYPE_OF_DOCUMENT, WhatKindOfDocument.USER_AGREEMENT.name)
            findNavController().navigateSafely(
                R.id.action_settingsFragment_to_privacyPolicyFragment,
                bundle,
                OptionsTransaction().optionForEditProfile
            )
        }

        binding.inviteLinear.setOnClickListener {
            // Пригласить участников
            findNavController().navigateSafely(
                R.id.action_global_inviteMembersBottomSheetDialogFragment2
            )
        }

        binding.exitBtn.setOnClickListener {
            showAlertDialogForExit()
        }

        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

    }

    private fun showAlertDialogForExit() {
        val id = Settings.Secure.getString(
            requireContext().applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(resources.getString(R.string.wouldYouLikeToExit))

            .setNegativeButton(resources.getString(R.string.decline)) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                dialog.cancel()
                viewModel.logout(id)
                activityNavController().navigateSafely(R.id.action_global_signFlowFragment)
            }
            .show()
    }

    private fun setCurrentLang() {
        // В дальнейшем будем получать язык от бека
        val currentLangInApp = ContextUtils.getCurrentLangInApp(requireContext())
        if (currentLangInApp == Lang.SYSTEM.lang) {
            currentLang = Lang.SYSTEM.lang
            binding.indicateLangTv.text = context?.getString(R.string.likeTheSystem)
        } else {
            binding.indicateLangTv.text = when (currentLangInApp) {
                Lang.RUSSIAN.lang -> {
                    currentLang = Lang.RUSSIAN.lang
                    context?.getString(R.string.russian)
                }

                Lang.ENGLISH.lang -> {
                    currentLang = Lang.ENGLISH.lang
                    context?.getString(R.string.english)
                }

                else -> {
                    currentLang = Lang.SYSTEM.lang
                    context?.getString(R.string.likeTheSystem)
                }
            }
        }
    }

    private fun requestData() {
        viewModel.loadUserOrganizations()
        viewModel.loadOrganizationSettings()
        viewModel.loadUserProfile()
    }

    private fun setData() {
        setCurrentLang()
        viewModel.organisationSettings.observe(viewLifecycleOwner) {
            licenseEnd = it?.licenseEnd
        }

        viewModel.organizations.observe(viewLifecycleOwner) {
            it?.let {
                listOfOrg.clear()
                it.forEach { orgModel ->
                    listOfOrg.add(orgModel)
                    if (orgModel.is_current) {
                        currentOrg = orgModel
                        Glide.with(this)
                            .load(orgModel.organization_photo?.addBaseUrl())
                            .circleCrop()
                            .error(R.drawable.ic_app_logo)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(binding.orgPhotoIv)
                        binding.orgNameLabelTv.text = orgModel.name
                    }
                }
            }
        }

        viewModel.profile.observe(viewLifecycleOwner) {
            if (it.profile.isHeadOfACurrentCommunity) binding.inviteLinear.visible()
            else binding.inviteLinear.invisible()
        }
    }

    private fun handleTopAppBar() {
        binding.header.toolbar.title = requireContext().getString(R.string.settings_label)
        binding.header.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }


    companion object {

        const val TYPE_OF_DOCUMENT = "typeOfDocument"
        const val LICENSE_END = "licenseEnd"

        @JvmStatic
        fun newInstance() =
            SettingsFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun applyTheme() {
        // Все ручные изменения top app bar
        // Уведомления
//        binding.header.notifyBadgeText.setTextColor(Color.parseColor(appTheme.mainBrandColor))
//        binding.header.notifyActive.backgroundTintList =
//            ColorStateList.valueOf(Color.parseColor(appTheme.mainBrandColor))
        binding.currentOrgCard.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor(appTheme.secondaryBrandColor)))
    }
}

enum class WhatKindOfDocument(name: String) {
    PRIVACY_POLICY("privacy_policy"), USER_AGREEMENT("user_agreement")
}