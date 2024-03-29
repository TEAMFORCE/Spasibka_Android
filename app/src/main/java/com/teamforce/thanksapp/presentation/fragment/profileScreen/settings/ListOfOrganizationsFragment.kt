package com.teamforce.thanksapp.presentation.fragment.profileScreen.settings

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.transition.TransitionSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialElevationScale
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.entities.profile.OrganizationModel
import com.teamforce.thanksapp.databinding.FragmentListOfOrganizationsBinding
import com.teamforce.thanksapp.databinding.ItemOrganizationBinding
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.CustomDialogFragment
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.NegativeButtonClickListener
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.PositiveButtonClickListener
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.SettingsCustomDialogFragment
import com.teamforce.thanksapp.presentation.viewmodel.AuthorizationType
import com.teamforce.thanksapp.presentation.viewmodel.profile.SettingsViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.glide.setImage
import com.teamforce.thanksapp.utils.glide.setImageRounded
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListOfOrganizationsFragment :
    BaseFragment<FragmentListOfOrganizationsBinding>(FragmentListOfOrganizationsBinding::inflate) {

    private val viewModel: SettingsViewModel by activityViewModels()
    private lateinit var adapter: ListDelegationAdapter<List<OrganizationModel>>
    private var currentOrg: OrganizationModel? = null
    private var authThroughVk: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentOrg = it.getParcelableExt(CURRENT_ORG, OrganizationModel::class.java)
        }
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.navContainer
            duration = 400.toLong()
            scrimColor = Color.TRANSPARENT
            //isDrawDebugEnabled = true
            setAllContainerColors(Color.WHITE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        viewModel.loadUserOrganizations()
        viewModel.organizations.observe(viewLifecycleOwner) {
            adapter.items = it?.filter { item -> !item.is_current }
        }

        binding.closeTv.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        binding.createOrgBtn.setOnClickListener {
            val extras = FragmentNavigatorExtras(it to TRANSITION_BTN)

            reenterTransition = MaterialElevationScale(true).apply {
                duration = 400.toLong()
            }
            findNavController().navigate(
                R.id.action_listOfOrganizationsFragment_to_createOrganizationFragment,
                null,
                null,
                extras
            )
        }
        setCurrentOrg()
        listeners()
    }

    private fun listeners() {
        viewModel.profile.observe(viewLifecycleOwner) {
            authThroughVk = it.profile.authThroughVk
        }

        viewModel.changeOrgResponse.observe(viewLifecycleOwner) {
            it?.let {
                val deviceId = Settings.Secure.getString(
                    requireContext().applicationContext.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
                viewModel.saveCredentialsForChangeOrg()
                viewModel.deletePushToken(deviceId)
                if (conditionChangingOrgThroughVk()) {
                    Log.d("ListOfOrgFragment", "Успешно сменили оргу через вк")
                    viewModel.clearChangeOrgResponse()
                    if (saveDataToSharedPreferences(it.token)) {
                        refreshApplication()
                    }
                } else if (it.tgCode != null || it.email != null) {
                    sendToastAboutVerifyCode()
                    viewModel.clearChangeOrgResponse()
                    findNavController().navigateSafely(
                        R.id.action_listOfOrganizationsFragment_to_changeOrganizationFragment,
                        null,
                        OptionsTransaction().optionForTransaction
                    )
                } else {
                    Toast.makeText(
                        requireContext(),
                        requireContext().getString(com.vk.auth.client.R.string.vk_confirmation_dialog_something_went_wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        viewModel.changeOrgError.observe(viewLifecycleOwner) {
            if (it != null) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveDataToSharedPreferences(authToken: String?): Boolean {
        return viewModel.saveDataForChangeOrg(authToken)
    }

    private fun refreshApplication() {
        requireActivity().apply {
            finish()
            overridePendingTransition(0, 0)
            startActivity(requireActivity().intent)
            overridePendingTransition(0, 0)
        }

    }


    private fun sendToastAboutVerifyCode() {
        if (viewModel.authorizationType == AuthorizationType.TELEGRAM) {
            Toast.makeText(
                requireContext(),
                R.string.Toast_verifyCode_hintTg,
                Toast.LENGTH_SHORT
            ).show()

        } else {
            Toast.makeText(
                requireContext(),
                R.string.Toast_verifyCode_hintEmail,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setCurrentOrg() {
        binding.apply {
            Glide.with(this@ListOfOrganizationsFragment)
                .load(currentOrg?.organization_photo?.addBaseUrl())
                .circleCrop()
                .error(R.drawable.ic_app_logo)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.orgPhotoIv)
            orgNameLabelTv.text = currentOrg?.name
            actionIcon.setImageDrawable(
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.copy_svg
                )
            )
            binding.orgNameLabelTv.text = currentOrg?.name
            actionIcon.setColorFilter(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
            mainCv.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.secondaryBrandColor)))
            currentOrgLabelTv.setTextColor(ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor)))
            currentOrgLabelTv.visible()
            mainCv.setOnClickListener {
                inviteMembers()
            }
        }
    }


    override fun applyTheme() {

    }


    private fun initRecycler() {
        binding.list.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
        adapter = ListDelegationAdapter(
            OrganizationAdapter(
                onCurrentOrgClick = ::inviteMembers,
                onOrgClick = ::changeOrg
            )
        )
        binding.list.adapter = adapter
    }

    private fun OrganizationAdapter(
        onOrgClick: (OrganizationModel) -> Unit,
        onCurrentOrgClick: () -> Unit
    ) =
        adapterDelegateViewBinding<OrganizationModel, OrganizationModel, ItemOrganizationBinding>(
            { layoutInflater, root ->
                ItemOrganizationBinding.inflate(
                    layoutInflater,
                    root,
                    false
                )
            },
        ) {
            binding.mainCv.setOnClickListener {
                if (item.is_current) onCurrentOrgClick()
                else onOrgClick(item)
            }
            bind {
                binding.orgNameLabelTv.text = item.name
                binding.orgPhotoIv.setImage(item.organization_photo)
                binding.actionIcon.setImageDrawable(getDrawable(R.drawable.ic_exchange))
                binding.currentOrgLabelTv.invisible()
                binding.mainCv.setCardBackgroundColor(
                    ColorStateList.valueOf(
                        Color.parseColor(
                            Branding.brand?.colorsJson?.generalBackgroundColor
                        )
                    )
                )
            }
        }

    private fun inviteMembers() {
        findNavController().navigateSafely(
            R.id.action_global_inviteMembersBottomSheetDialogFragment2
        )
    }

    private fun changeOrg(newOrg: OrganizationModel) {
        val settingsDialog = SettingsCustomDialogFragment(
            positiveTextBtn = requireContext().getString(R.string.settings_go_to),
            negativeTextBtn = requireContext().getString(R.string.settings_negative),
            title = requireContext().getString(R.string.settings_go_to_organization, newOrg.name),
            subtitle = requireContext().getString(R.string.onboarding_join_to_community_refresh)
        )
        val dialogFragment = CustomDialogFragment.newInstance(settingsDialog)
        dialogFragment.setPositiveButtonClickListener(object : PositiveButtonClickListener {
            override fun onPositiveButtonClick() {
                if (conditionChangingOrgThroughVk()) {
                    viewModel.changeOrg(newOrg.id, viewModel.getVkAccessToken())
                } else {
                    viewModel.changeOrg(newOrg.id)
                }
            }
        })
        dialogFragment.show(requireActivity().supportFragmentManager, "MyDialogFragment")
    }

    private fun conditionChangingOrgThroughVk(): Boolean =
        authThroughVk && viewModel.getVkAccessToken().isNotNullOrEmptyMy()

    companion object {

        const val CURRENT_ORG = "current_organization"
        const val TRANSITION_CARD = "list_of_org_card_second"
        const val TRANSITION_BTN = "list_of_org_btn_second"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListOfOrganizationsFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
