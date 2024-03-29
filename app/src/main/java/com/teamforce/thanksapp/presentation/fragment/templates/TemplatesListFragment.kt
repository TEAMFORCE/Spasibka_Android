package com.teamforce.thanksapp.presentation.fragment.templates

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import by.kirich1409.viewbindingdelegate.viewBinding
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.databinding.FragmentTemplatesListBinding
import com.teamforce.thanksapp.domain.models.templates.TemplateForBundleModel
import com.teamforce.thanksapp.presentation.adapter.decorators.VerticalDividerDecoratorForListWithBottomBar
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.adapter.templates.TemplatesPagerAdapter
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts.SCOPE_TEMPLATES
import com.teamforce.thanksapp.presentation.fragment.challenges.category.CategoryArgs
import com.teamforce.thanksapp.presentation.fragment.challenges.category.CategoryItem
import com.teamforce.thanksapp.presentation.fragment.challenges.category.SharedFilterCategoryViewModel
import com.teamforce.thanksapp.presentation.viewmodel.templates.TemplatesViewModel
import com.teamforce.thanksapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TemplatesListFragment : Fragment(R.layout.fragment_templates_list) {

    private val binding: FragmentTemplatesListBinding by viewBinding()
    private val viewModel: TemplatesViewModel by viewModels()
    private val sharedFilterCategoryViewModel: SharedFilterCategoryViewModel by activityViewModels()

    private var listAdapter: TemplatesPagerAdapter? = null
    private var scopeOfTemplates: ScopeRequestParams? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            scopeOfTemplates = it.serializable(SCOPE_TEMPLATES)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        sharedFilterCategoryViewModel.clearFilters()
        loadTemplates(scopeOfTemplates)
        sharedFilterCategoryViewModel.onApplyFilters.observe(viewLifecycleOwner, ::onApplyFilters)

        listAdapter!!.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Error) {
                showSnackBarAboutNetworkProblem(
                    view,
                    requireContext()
                )
                showConnectionError()
            }
        }
    }

    private fun onApplyFilters(categoryItems: List<CategoryItem>?) {
        loadTemplates(scopeOfTemplates, categoryItems?.map { it.id } ?: emptyList())
    }

    private fun loadTemplates(scopeTemplates: ScopeRequestParams?, categoryItems: List<Int> = emptyList()) {
        if (scopeTemplates != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                binding.swipeRefreshLayout.isRefreshing = false
                viewModel.loadTemplates(scopeTemplates, categoryItems)
                    .collectLatest { templates ->
                        listAdapter?.submitData(templates)
                        hideConnectionError()
                    }
            }
        }
    }

    private fun showConnectionError() {
        binding.error.root.visible()
        binding.list.invisible()
    }

    private fun hideConnectionError() {
        binding.error.root.invisible()
        binding.list.visible()
    }

    private fun initAdapter() {
        listAdapter = TemplatesPagerAdapter()


        binding.list.apply {
            this.adapter = listAdapter?.withLoadStateHeaderAndFooter(
                header = HistoryLoadStateAdapter(),
                footer = HistoryLoadStateAdapter()
            )
            this.addItemDecoration(
                VerticalDividerDecoratorForListWithBottomBar(
                    8,
                    listAdapter!!.itemCount
                )
            )
        }

        listAdapter?.addLoadStateListener { state ->
            binding.swipeRefreshLayout.isRefreshing = state.refresh == LoadState.Loading
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            listAdapter?.refresh()
            binding.swipeRefreshLayout.isRefreshing = true
        }

        listAdapter?.onTemplateClicked = { data: TemplateForBundleModel ->
            // Переход на экран чалика и прокидывание в него данных о шаблоне
            val bundle = Bundle()
            bundle.apply {
                putParcelable(ChallengesConsts.CHALLENGE_TEMPLATE, data)
            }
            findNavController().navigateSafely(
                R.id.action_global_createChallengeFragment,
                bundle,
                OptionsTransaction().optionForTransactionWithSaveBackStack
            )
        }

        listAdapter?.onTemplateEditClicked = { data: TemplateForBundleModel ->
            // Переход на экран чалика и прокидывание в него данных о шаблоне
            val bundle = Bundle()
            bundle.apply {
                putParcelable(ChallengesConsts.CHALLENGE_TEMPLATE, data)
                putSerializable(CategoryArgs.ARG_SECTION, scopeOfTemplates)
            }
            findNavController().navigateSafely(
                R.id.action_global_createTemplateFragment,
                bundle,
                OptionsTransaction().optionForTransactionWithSaveBackStack
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharedFilterCategoryViewModel.clearFilters()
        binding.list.adapter = null
        listAdapter = null
    }

    companion object {

        @JvmStatic
        fun newInstance(scopeTemplates: ScopeRequestParams) =
            TemplatesListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(SCOPE_TEMPLATES, scopeTemplates)
                }
            }
    }
}