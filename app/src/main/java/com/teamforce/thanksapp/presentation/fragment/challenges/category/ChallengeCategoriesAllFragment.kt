package com.teamforce.thanksapp.presentation.fragment.challenges.category

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.databinding.FragmentChallengeCategoriesAllBinding
import com.teamforce.thanksapp.databinding.ItemCategoryFilterBinding
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.serializable
import com.teamforce.thanksapp.utils.traverseViewsTheming
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ChallengeCategoriesAllFragment :
    BaseBottomSheetDialogFragment<FragmentChallengeCategoriesAllBinding>(FragmentChallengeCategoriesAllBinding::inflate) {

    private var categoryIds: List<Int>? = emptyList()
    private lateinit var adapter: ListDelegationAdapter<List<DisplayableItem>>
    private val viewModel: ChallengeFilterCategoryViewModel by viewModels()
    private val sharedCategoryViewModel: SharedCategoryViewModel by activityViewModels()
    private var section: ScopeRequestParams? = null

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            section = it.serializable(CategoryArgs.ARG_SECTION)
            categoryIds = it.getIntegerArrayList(CategoryArgs.ARG_SECTIONS_IDS)?.toList()
        }
    }

    private fun CategoryAdapter(onItemClick: (CategoryItem) -> Unit, onCheckedClick: (CategoryItem) -> Unit) =
        adapterDelegateViewBinding<CategoryItem, DisplayableItem, ItemCategoryFilterBinding>(
            { layoutInflater, root -> ItemCategoryFilterBinding.inflate(layoutInflater, root, false) },
        ) {
            binding.checkbox.setOnClickListener {
                onCheckedClick(item)
            }
            bind {
                binding.name.text = item.name

                val hasCategories = item.categories.isNotEmpty()
                binding.arrow.visibility = hasCategories.toVisibility()
                binding.checkbox.isChecked = item.isChecked
                if (item.categories.isNotEmpty()) {
                    binding.root.setOnClickListener { onItemClick(item) }
                } else {
                    binding.root.setOnClickListener { onCheckedClick(item) }
                }
                binding.root.traverseViewsTheming()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editBtn.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_categoriesAllFragment_to_categoryEditFragment,
                Bundle().apply {
                    putSerializable(
                        CategoryArgs.ARG_SECTION,
                        section
                    )
                })
        }
        binding.saveBtn.setOnClickListener {
            sharedCategoryViewModel.applySelection(viewModel.getSelected())
            this.dismiss()
        }
        binding.closeBtn.setOnClickListener {
            this.dismiss()
        }

        val title = binding.categoryTitle.text.toString()
        binding.backBtn.setOnClickListener {
            viewModel.backToParentCategory(title)
        }

        initRecycler()
        initViewModel()
    }

    override fun applyTheme() {
    }

    private fun close() {
    }

    private fun initViewModel() {
        viewModel.data.observe(this, ::onDataLoaded)

        viewModel.pageTitle.observe(this, ::onTitleChanged)

        section?.let {
            viewModel.loadCategory(section!!, categoryIds)
        }
    }

    private fun onTitleChanged(pageTitle: String?) {
        Timber.d("onTitleChanged() called with: s = [ $pageTitle ]")
        pageTitle?.let(binding.categoryTitle::setText)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onDataLoaded(displayableItems: List<CategoryItem>?) {
        displayableItems?.let {

            binding.backBtn.visibility = (it.firstOrNull()?.parentId !== null).toVisibility()

            adapter.items = displayableItems
            adapter.notifyDataSetChanged()
        }
    }

    private fun initRecycler() {
        adapter = ListDelegationAdapter(
            CategoryAdapter(
                onItemClick = viewModel::onItemClick,
                onCheckedClick = viewModel::onCheckClick
            )
        )
        binding.categoryRecyclerView.adapter = adapter
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            ChallengeCategoriesAllFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}

