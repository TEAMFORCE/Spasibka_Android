package com.teamforce.thanksapp.presentation.fragment.templates

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialElevationScale
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.databinding.FragmentTemplatesBinding
import com.teamforce.thanksapp.presentation.adapter.templates.TemplatesListStateAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.category.CategoryArgs
import com.teamforce.thanksapp.presentation.fragment.challenges.category.CategoryItem
import com.teamforce.thanksapp.presentation.fragment.challenges.category.SharedFilterCategoryViewModel
import com.teamforce.thanksapp.presentation.fragment.challenges.category.toVisibility
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TemplatesFragment :
    BaseFragment<FragmentTemplatesBinding>(FragmentTemplatesBinding::inflate) {

    private var pagerAdapter: TemplatesListStateAdapter? = null
    private var mediator: TabLayoutMediator? = null
    private var scopeRequestParams: ScopeRequestParams = ScopeRequestParams.valueOf(0)
    private val sharedFilterCategoryViewModel: SharedFilterCategoryViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        startPostponedEnterTransition()
        setPagerAdapter()

    }

    override fun applyTheme() {

    }

    private fun setPagerAdapter() {
        binding.apply {
            pagerAdapter =
                TemplatesListStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
            viewPager.adapter = pagerAdapter
            initTabLayout()
        }
    }

    private fun initTabLayout() {
        binding.apply {
            viewPager.adapter = pagerAdapter
            mediator = TabLayoutMediator(tabGroup, viewPager) { tab, pos ->
            }
            mediator?.attach()
        }

        binding.tabGroup.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                for (i in 0 until binding.tabGroup.tabCount) {
                    val mytab: TabLayout.Tab? = binding.tabGroup.getTabAt(i)

                    mytab?.view?.clipToPadding = false
                    if (tab == mytab) {
                        updateScope(ScopeRequestParams.valueOf(i))
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })


        binding.tabGroup.setTabTitles(listOf(
            requireContext().getString(R.string.templates_my),
            requireContext().getString(R.string.templates_ours),
            requireContext().getString(R.string.templates_common)
        ))

    }

    private fun updateScope(scope: ScopeRequestParams) {
        val filterButtonVisible = when (scope) {
            ScopeRequestParams.TEMPLATES -> true
            else -> false
        }
        binding.header.filterIv.visibility = filterButtonVisible.toVisibility()
        scopeRequestParams = scope
    }

    private fun handleTopAppBar(){
        binding.header.toolbar.title = requireContext().getString(R.string.templates_title)
        binding.header.filterIv.visible()
        binding.header.filterIv.setOnClickListener {
            val filterList = sharedFilterCategoryViewModel.onApplyFilters.value

            findNavController().navigateSafely(
                R.id.action_global_categoryFilterFragment,
                Bundle().apply {
                    putSerializable(
                        CategoryArgs.ARG_SECTION,
                        scopeRequestParams,
                    )
                    filterList?.let {
                        putIntegerArrayList(CategoryArgs.ARG_SECTIONS_IDS, ArrayList(it.map(CategoryItem::id)))
                    }
                })
        }
        binding.header.challengeMenuBtn.visible()
        binding.header.challengeMenuBtn.setOnClickListener {
            showSimpleDialog()
        }
    }

    private fun showSimpleDialog(){
        val items = arrayOf(getString(R.string.create_template_dialog), getString(R.string.create_challenge_dialog))

        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_ThanksApp_Dialog_Simple)
            .setItems(items) { dialog, which ->
                when(which){
                    0 -> {
                        findNavController().navigateSafely(R.id.action_global_createTemplateFragment)
                    }
                    1 -> {
                        findNavController().navigateSafely(R.id.action_global_createChallengeFragment)
                    }
                }
            }
            .show()
    }


    companion object {

        @JvmStatic
        fun newInstance() =
            TemplatesFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
