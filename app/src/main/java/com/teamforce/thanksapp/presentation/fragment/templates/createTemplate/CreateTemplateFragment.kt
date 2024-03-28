package com.teamforce.thanksapp.presentation.fragment.templates.createTemplate

import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.teamforce.photopicker.PhotoPickerFragment
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.databinding.FragmentCreateTemplateBinding
import com.teamforce.thanksapp.domain.models.challenge.ChallengeType
import com.teamforce.thanksapp.domain.models.templates.TemplateForBundleModel
import com.teamforce.thanksapp.presentation.adapter.transactions.AdapterItem
import com.teamforce.thanksapp.presentation.adapter.transactions.ImageAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.customViews.photoSelectionView.PhotoSelectionListener
import com.teamforce.thanksapp.presentation.customViews.photoSelectionView.toItems
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts
import com.teamforce.thanksapp.presentation.fragment.challenges.category.CategoryArgs
import com.teamforce.thanksapp.presentation.fragment.challenges.category.CategoryItem
import com.teamforce.thanksapp.presentation.fragment.challenges.category.SharedCategoryViewModel
import com.teamforce.thanksapp.presentation.fragment.newTransactionScreen.ItemMoveCallback
import com.teamforce.thanksapp.presentation.viewmodel.templates.CreateTemplateViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Extensions.toMultipart
import com.teamforce.thanksapp.utils.branding.Branding
import dagger.hilt.android.AndroidEntryPoint
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

@AndroidEntryPoint
class CreateTemplateFragment :
    BaseFragment<FragmentCreateTemplateBinding>(FragmentCreateTemplateBinding::inflate),
    PhotoPickerFragment.Callback {

    private val viewModel: CreateTemplateViewModel by activityViewModels()
    private val sharedCategoryViewModel: SharedCategoryViewModel by activityViewModels()

    private var section: ScopeRequestParams? = null
    private var templatesForChanging: TemplateForBundleModel? = null
    private var ihaveUsedTemplateFromBundle: Boolean = false


    private val settingsForCreateTemplate = SettingsTemplateFragment.SettingsForCreateTemplate()


    override fun onDestroyView() {
        super.onDestroyView()
        activity?.window
            ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO Deprecated API need to replace it with something else
        activity?.window
            ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.navContainer
            duration = 400.toLong()
            scrimColor = Color.WHITE
            setAllContainerColors(requireContext().getColor(R.color.general_background))
        }
        arguments?.let {
            templatesForChanging = it.parceleable(ChallengesConsts.CHALLENGE_TEMPLATE)
            section = it.serializable(CategoryArgs.ARG_SECTION)
        }

        setFragmentResultListener(SettingsTemplateFragment.SETTINGS_TEMPLATE_REQUEST_KEY) { requestKey, bundle ->
            val result = bundle.getParcelableExt(
                SettingsTemplateFragment.SETTINGS_TEMPLATE_BUNDLE_KEY,
                SettingsTemplateFragment.SettingsForCreateTemplate::class.java
            )
            result?.let {
                settingsForCreateTemplate.challengeType = it.challengeType
                settingsForCreateTemplate.severalReports = it.severalReports
                settingsForCreateTemplate.showContenders = it.showContenders
                settingsForCreateTemplate.scopeTemplates = it.scopeTemplates
                section = it.scopeTemplates
            }
        }

        templatesForChanging?.let {
            savingReceivedSettings(it)
        }
        section?.let {
            settingsForCreateTemplate.scopeTemplates = it
        }

    }

    private fun savingReceivedSettings(templateForBundleModel: TemplateForBundleModel) {
        settingsForCreateTemplate.scopeTemplates = templateForBundleModel.scopeOfTemplates
        settingsForCreateTemplate.challengeType =
            if (templateForBundleModel.challengeWithVoting) ChallengeType.VOTING else ChallengeType.DEFAULT
        settingsForCreateTemplate.showContenders = templateForBundleModel.showContenders
        settingsForCreateTemplate.severalReports = templateForBundleModel.multipleReports
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDataAboutChangingTemplate()
        binding.tvAdditionalSettings.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable(
                    SettingsTemplateFragment.SETTINGS_TEMPLATE_BUNDLE_KEY,
                    settingsForCreateTemplate
                )
            }
            findNavController().navigateSafely(
                R.id.action_createTemplateFragment_to_settingsTemplateFragment,
                bundle,
                OptionsTransaction().optionForEditProfile
            )
        }
        binding.closeBtn.setOnClickListener { activity?.onBackPressedDispatcher?.onBackPressed() }

        viewModel.internetError.observe(viewLifecycleOwner) {
            showSnackBarAboutNetworkProblem(view, requireContext())
        }
        if (templatesForChanging?.sections?.isEmpty() == true) sharedCategoryViewModel.clearSelected()
        onApplySelections()
        sharedCategoryViewModel.onApplySelections.observe(viewLifecycleOwner, ::onApplySelections)
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) binding.progressBar.visible()
            else binding.progressBar.invisible()
        }

        binding.createBtnSticky.setOnClickListener {
            createTemplate()
        }
        binding.createBtn.setOnClickListener {
            createTemplate()
        }
        binding.categoryBtn.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_createTemplateFragment_to_categoriesAllFragment,
                Bundle().apply {
                    putSerializable(
                        CategoryArgs.ARG_SECTION,
                        settingsForCreateTemplate.scopeTemplates
                    )
                    putIntegerArrayList(
                        CategoryArgs.ARG_SECTIONS_IDS,
                        ArrayList(sharedCategoryViewModel.getSelected())
                    )
                })

        }

        KeyboardVisibilityEvent.setEventListener(
            requireActivity(),
            viewLifecycleOwner,
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    if (isOpen) {
                        binding.createBtnSticky.visible()
                        binding.createBtn.invisible()
                    } else {
                        binding.createBtnSticky.invisible()
                        binding.createBtn.visible()
                    }
                }
            }
        )
        viewModel.screenState.observe(viewLifecycleOwner) { screenState ->
            if (screenState.updateTemplate) {
                binding.screenTitleTv.text = requireContext().getString(R.string.template_edit)
                binding.screenSubtitleTv.text =
                    requireContext().getString(R.string.template_change_template_data)
                binding.createBtn.text = requireContext().getString(R.string.template_save_changes)
                binding.createBtnSticky.text =
                    requireContext().getString(R.string.template_save_changes)
                binding.createBtnSticky.setOnClickListener {
                    updateTemplate()
                }
                binding.createBtn.setOnClickListener {
                    updateTemplate()
                }
            } else {
                binding.screenTitleTv.text = requireContext().getString(R.string.templates_new)
                binding.screenSubtitleTv.text =
                    requireContext().getString(R.string.templates_youNeedToFillInfo)
                binding.createBtn.text = requireContext().getString(R.string.template_save)
                binding.createBtnSticky.setOnClickListener {
                    createTemplate()
                }
                binding.createBtn.setOnClickListener {
                    createTemplate()
                }
            }
        }
        initDragRecycler()
    }

    private fun setSelectedSections(sections: List<String>) {
        binding.chipGroup.removeAllViews()
        for (i in sections.indices) {
            val chip: Chip = LayoutInflater.from(binding.chipGroup.context)
                .inflate(
                    R.layout.chip_tag_in_create_template,
                    binding.chipGroup,
                    false
                ) as Chip
            with(chip) {
                text = sections[i]
                setEnsureMinTouchTargetSize(true)
                minimumWidth = 0
            }
            binding.chipGroup.addView(chip)
        }
        themingChips()
    }

    private fun themingChips() {
        binding.chipGroup.children.forEach { chip ->
            with(chip as Chip) {
                chipBackgroundColor =
                    ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.secondaryBrandColor))
                setTextColor(ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor)))
            }
        }
    }

    private fun onApplySelections(categoryItems: List<CategoryItem>? = null) {
        if (sharedCategoryViewModel.getSelectedWithName().isEmpty()) {
            binding.sectionsLinear.invisible()
        } else {
            binding.sectionsLinear.visible()
            setSelectedSections(sharedCategoryViewModel.getSelectedWithName())
        }
    }

    private fun setDataAboutChangingTemplate() {
        if (templatesForChanging != null) {
            viewModel.setUpdateTemplate(true)
            sharedCategoryViewModel.applySelection(templatesForChanging!!.sections)
            if (!ihaveUsedTemplateFromBundle) {
                binding.titleEt.setText(templatesForChanging!!.title)
                binding.descriptionEt.setText(templatesForChanging!!.description)
//                viewModel.saveSettingsForCreateTemplate(
//                    challengeWithVoting = templatesForChanging!!.challengeWithVoting,
//                    severalReports = templatesForChanging!!.multipleReports,
//                    showContenders = templatesForChanging!!.showContenders,
//                    scopeTemplates = templatesForChanging!!.scopeOfTemplates
//                )
                ihaveUsedTemplateFromBundle = true
            }
        } else {
            viewModel.setUpdateTemplate(false)
        }
    }

    override fun applyTheme() {
    }


    private fun createTemplate() {
        if (!binding.titleEt.text.isNullOrEmpty() &&
            !binding.descriptionEt.text.isNullOrEmpty()
        ) {
            binding.createBtn.isEnabled = false
            binding.createBtnSticky.isEnabled = false
            viewModel.createTemplate(
                name = binding.titleEt.text?.trim().toString(),
                description = binding.descriptionEt.text?.trim().toString(),
                photos = binding.photoSelection.getPhotos(),
                sections = sharedCategoryViewModel.getSelected(),
                settingsForCreateTemplate = settingsForCreateTemplate
            )
            createTemplateObserver()
        } else {
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.allFieldsAreRequired),
                Toast.LENGTH_SHORT
            ).show()
            binding.titleTextField.error = requireContext().getString(R.string.requiredField)
            binding.descriptionTextField.error = requireContext().getString(R.string.requiredField)
        }
    }

    private fun updateTemplate() {
        if (!binding.titleEt.text.isNullOrEmpty() &&
            !binding.descriptionEt.text.isNullOrEmpty() &&
            templatesForChanging != null
        ) {
            binding.createBtn.isEnabled = false
            binding.createBtnSticky.isEnabled = false
            viewModel.updateTemplate(
                templateId = templatesForChanging!!.id,
                name = binding.titleEt.text?.trim().toString(),
                description = binding.descriptionEt.text?.trim().toString(),
                sections = sharedCategoryViewModel.getSelected(),
                settingsForCreateTemplate = settingsForCreateTemplate,
                photos = binding.photoSelection.getPhotos(),
                fileList = binding.photoSelection.getFileList()
            )
            createTemplateObserver()
        } else {
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.allFieldsAreRequired),
                Toast.LENGTH_SHORT
            ).show()
            binding.titleTextField.error = requireContext().getString(R.string.requiredField)
            binding.descriptionTextField.error = requireContext().getString(R.string.requiredField)
        }
    }

    private fun createTemplateObserver() {
        viewModel.isSuccessOperation.observe(viewLifecycleOwner) {
            if (it) {
                binding.createBtn.isEnabled = true
                binding.createBtnSticky.isEnabled = true
                if (viewModel.screenState.value!!.updateTemplate) {
                    Toast.makeText(
                        requireContext(),
                        requireContext().getString(R.string.template_was_updated),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        requireContext().getString(R.string.template_was_created),
                        Toast.LENGTH_LONG
                    ).show()
                }
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }

        viewModel.createTemplateError.observe(viewLifecycleOwner) {
            try {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.smthWentWrong),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "CreateChallenge Error")
            }

        }
    }



    private fun initDragRecycler() {
        templatesForChanging?.photos?.let { binding.photoSelection.updateRecyclerViewStringList(it) }

        binding.photoSelection.photoSelectionListener = object : PhotoSelectionListener {
            override fun onAttachImageClicked(maxSelection: Int, alreadySelected: Int) {
                openPicker(maxSelection, alreadySelected)
            }
        }
    }



    override fun onImagesPicked(photos: ArrayList<Uri>) {
        binding.photoSelection.updateRecyclerView(photos)
    }


    private fun openPicker(maxSelection: Int, alreadySelected: Int) {
        PhotoPickerFragment.newInstance(
            multiple = true,
            allowCamera = true,
            maxSelection = maxSelection,
            alreadySelected = alreadySelected,
            mainColor = Branding.brand.colorsJson.mainBrandColor
        ).show(childFragmentManager, "photoPicker")
    }


    companion object {
        const val TAG = "Create Template Fragment"

        @JvmStatic
        fun newInstance() =
            CreateTemplateFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
