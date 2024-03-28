package com.teamforce.thanksapp.presentation.fragment.challenges.category

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.databinding.FragmentChallengeCategoryFilterBinding
import com.teamforce.thanksapp.databinding.ItemCategoryFilterBinding
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.category.CategoryArgs.ARG_SECTION
import com.teamforce.thanksapp.presentation.fragment.challenges.category.CategoryArgs.ARG_SECTIONS_IDS
import com.teamforce.thanksapp.utils.serializable
import com.teamforce.thanksapp.utils.traverseViewsTheming
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChallengeFilterCategoryFragment :
    BaseBottomSheetDialogFragment<FragmentChallengeCategoryFilterBinding>(FragmentChallengeCategoryFilterBinding::inflate) {

    private var categoryIds: List<Int>? = emptyList()
    private var section: ScopeRequestParams? = null
    private lateinit var adapter: ListDelegationAdapter<List<DisplayableItem>>
    private val viewModel: ChallengeFilterCategoryViewModel by viewModels()
    private val sharedFilterCategoryViewModel: SharedFilterCategoryViewModel by activityViewModels()

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            section = it.serializable(ARG_SECTION)
            categoryIds = it.getIntegerArrayList(ARG_SECTIONS_IDS)?.toList()
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
                // binding.divider.visibility = hasCategories.toVisibility()
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


        binding.applyBtn.setOnClickListener {
            sharedFilterCategoryViewModel.onApplyFilters(viewModel.getSelected())
            dismiss()
        }
        binding.resetBtn.setOnClickListener {
            sharedFilterCategoryViewModel.clearFilters()
            this.dismiss()
        }
        val title = binding.title.text.toString()
        binding.backBtn.setOnClickListener {
            viewModel.backToParentCategory(title)
        }

        initRecycler()
        initViewModel()
    }

    override fun applyTheme() {
    }

    private fun initViewModel() {
        viewModel.data.observe(this, ::onDataLoaded)
        section?.let {
            viewModel.loadCategory(it, categoryIds)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onDataLoaded(displayableItems: List<CategoryItem>?) {
        displayableItems?.let {

            binding.backBtn.visibility = (it.firstOrNull()?.parentId !== null).toVisibility()

            adapter.items = displayableItems
            adapter.notifyDataSetChanged()

            val filledList = displayableItems.isNotEmpty()

            binding.applyBtn.visibility = filledList.toVisibility()
            binding.resetBtn.visibility = filledList.toVisibility()
            binding.emptyListText.visibility = (!filledList).toVisibility()
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
            ChallengeCategoryEditFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}

fun Boolean.toVisibility() = if (this) View.VISIBLE else View.INVISIBLE
