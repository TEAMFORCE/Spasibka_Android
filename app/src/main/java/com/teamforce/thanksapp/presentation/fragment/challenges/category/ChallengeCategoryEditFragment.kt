package com.teamforce.thanksapp.presentation.fragment.challenges.category

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.text.color
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.databinding.DialogConfirmationBinding
import com.teamforce.thanksapp.databinding.FragmentChallengeCategoryEditBinding
import com.teamforce.thanksapp.databinding.ItemCategoryFilterSwipableBinding
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.serializable
import com.teamforce.thanksapp.utils.traverseViewsTheming
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ChallengeCategoryEditFragment : BaseBottomSheetDialogFragment<FragmentChallengeCategoryEditBinding>(FragmentChallengeCategoryEditBinding::inflate) {

    private lateinit var adapter: ListDelegationAdapter<List<DisplayableItem>>
    private val viewModel: ChallengeFilterCategoryViewModel by viewModels()
    private var section: ScopeRequestParams? = null

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            section = it.serializable(CategoryArgs.ARG_SECTION)
        }
    }

    private fun CategoryAdapter(onItemClick: (CategoryItem) -> Unit, onRemoveItemClick: (CategoryItem) -> Unit) =
        adapterDelegateViewBinding<CategoryItem, DisplayableItem, ItemCategoryFilterSwipableBinding>(
            { layoutInflater, root -> ItemCategoryFilterSwipableBinding.inflate(layoutInflater, root, false) },
        ) {

            bind {
                val hasCategories = item.categories.isNotEmpty()

                binding.name.text = item.name
                binding.arrow.visibility = hasCategories.toVisibility()
                // binding.divider.visibility = hasCategories.toVisibility()
                if (item.categories.isNotEmpty()) {
                    binding.dragItem.setOnClickListener { onItemClick(item) }
                } else {
                    binding.dragItem.setOnClickListener(null)
                }
                binding.swipeLayout.close(false)
                binding.remove.setOnClickListener {
                    binding.swipeLayout.close(true)
                    onRemoveItemClick(item)
                }
                binding.root.traverseViewsTheming()
                binding.swipeLinearBackground.backgroundTintList = ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
                binding.dragItem.backgroundTintList = ColorStateList.valueOf((Color.parseColor(Branding.brand?.colorsJson?.minorInfoSecondaryColor)))
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addBtn.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_categoryEditFragment_to_categoryAddFragment,
                Bundle().apply {
                    putSerializable(
                        CategoryArgs.ARG_SECTION,
                        section
                    )
                })
        }
        binding.saveBtn.setOnClickListener {
            this.dismiss()
        }
        binding.closeBtn.setOnClickListener {
            this.dismiss()
        }
        binding.backBtn.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
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

    private fun initViewModel() {
        viewModel.data.observe(this, ::onDataLoaded)
        viewModel.pageTitle.observe(this, ::onTitleChanged)
        section?.let(viewModel::loadCategory)
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
                onRemoveItemClick = ::remove
            )
        )
        binding.categoryRecyclerView.adapter = adapter
    }

    private fun remove(it: CategoryItem) {
        showConfirmationDialog(it)
    }

    private fun showConfirmationDialog(categoryItem: CategoryItem) {
        val builderDialog = AlertDialog.Builder(context)
        val inflater = requireActivity().layoutInflater
        val binding = DialogConfirmationBinding.inflate(inflater)
        builderDialog.setView(binding.root)

        val dialog = builderDialog.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val context = binding.root.context
        val spannableTitle = SpannableStringBuilder()
            .append(context.getText(R.string.remove_category))
            .append(" ")
            .color(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor)) { append(categoryItem.name) }
            .append("?")

        dialog.show()

        binding.title.text = spannableTitle
        binding.cancelBtn.setOnClickListener {
            dialog.cancel()
        }
        binding.okBtn.setOnClickListener {
            viewModel.removeSections(categoryItem.id, section)
            dialog.dismiss()
        }
        binding.okBtn.setBackgroundColor(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
        binding.cancelBtn.setTextColor(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
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

