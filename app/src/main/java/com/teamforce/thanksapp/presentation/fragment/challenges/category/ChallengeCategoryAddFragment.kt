package com.teamforce.thanksapp.presentation.fragment.challenges.category

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.databinding.FragmentChallengeCategoryAddBinding
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.serializable
import dagger.hilt.android.AndroidEntryPoint
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import timber.log.Timber

@AndroidEntryPoint
class ChallengeCategoryAddFragment :
    BaseBottomSheetDialogFragment<FragmentChallengeCategoryAddBinding>(
        FragmentChallengeCategoryAddBinding::inflate
    ) {

    private var selectedCategory: CategoryItem? = null
    private var listAdapter: ArrayAdapter<ListViewItem>? = null
    private val viewModel: ChallengeFilterCategoryViewModel by viewModels()
    private var section: ScopeRequestParams? = null

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            section = it.serializable(CategoryArgs.ARG_SECTION)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSaveClick()
                backToEdit()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.saveBtn.setOnClickListener {
            onSaveClick()
            backToEdit()
        }
        binding.closeBtn.setOnClickListener {
            this.dismiss()
        }

//        binding.backBtn.setOnClickListener {
//            backToEdit()
//        }

        val title = binding.categoryTitle.text.toString()
        binding.backBtn.setOnClickListener {
            viewModel.backToParentCategory(title)
        }
        initListAdapter()
        initViewModel()
        setKeyboardVisibilityEventListener()
    }

    private fun setKeyboardVisibilityEventListener() {
        KeyboardVisibilityEvent.setEventListener(
            requireActivity(),
            viewLifecycleOwner,
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    if (isOpen) {
                        (this@ChallengeCategoryAddFragment.dialog as? BottomSheetDialog)?.behavior?.state =
                            STATE_EXPANDED
                    } else {
                        (this@ChallengeCategoryAddFragment.dialog as? BottomSheetDialog)?.behavior?.state =
                            STATE_HALF_EXPANDED
                    }
                }
            }
        )
    }

    override fun applyTheme() {
    }

    private fun onSaveClick() {
        section?.let {
            viewModel.addCategory(
                selectedCategory = selectedCategory,
                section = it,
                text = binding.titleTextField.editText?.text.toString()
            )
        }
    }

    private fun backToEdit() {
        findNavController().navigateSafely(
            R.id.action_categoryAddFragment_to_categoryEditFragment,
            Bundle().apply {
                putSerializable(
                    CategoryArgs.ARG_SECTION,
                    section
                )
            })
    }

    private fun initListAdapter() {

        listAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
        )
        binding.orgFilterSpinner.setAdapter(listAdapter)
        binding.orgFilterSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                selectedCategory = listAdapter?.getItem(position)?.item
                Timber.d("selectedCategory = $selectedCategory")
            }
    }

    private fun initViewModel() {
        viewModel.pageTitle.observe(this, ::onTitleChanged)
        viewModel.backEvent.observe(this, ::onBackPressed)
        viewModel.data.observe(this, ::onDataLoaded)
        section?.let(viewModel::loadCategory)
    }

    fun onBackPressed(unused: Boolean) {
        dismiss()
    }

    fun onDataLoaded(categoryItems: List<CategoryItem>?) {

        categoryItems?.let {
            listAdapter?.addAll(categoryItems.allChildren().map { ListViewItem(it) })
            listAdapter?.notifyDataSetChanged()
        }
    }

    private fun onTitleChanged(pageTitle: String?) {
        Timber.d("onTitleChanged() called with: s = [ $pageTitle ]")
        pageTitle?.let(binding.categoryTitle::setText)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            ChallengeCategoryAddFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}

private data class ListViewItem(val item: CategoryItem) {
    override fun toString(): String = item.name
}
