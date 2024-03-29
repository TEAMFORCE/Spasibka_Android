package com.teamforce.thanksapp.presentation.fragment.awardsScreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentAwardsBinding
import com.teamforce.thanksapp.domain.models.awards.CategoryAwardsModel
import com.teamforce.thanksapp.presentation.adapter.awards.AwardsListStateAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.viewmodel.awards.AwardsViewModel
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AwardsFragment : BaseFragment<FragmentAwardsBinding>(FragmentAwardsBinding::inflate) {

    private val viewModel: AwardsViewModel by viewModels()

    private var pagerAdapter: AwardsListStateAdapter? = null
    private var mediator: TabLayoutMediator? = null

    private var showOnlyMyAwards: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            showOnlyMyAwards = it.getBoolean(AWARDS_FRAGMENT_MY_AWARDS)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadAwards(showOnlyMyAwards)
        viewModel.awards.observe(viewLifecycleOwner) {
            if(it.isNullOrEmpty()){
                showEmptyView()
            }else{
                setPagerAdapter(it)
                hideEmptyView()
            }
            binding.refreshLayout.isRefreshing = false
        }

        binding.refreshLayout.setOnRefreshListener {
            viewModel.loadAwards(showOnlyMyAwards)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) showProgress()
            else hideProgress()
        }
    }

    private fun showEmptyView(){
        binding.refreshLayout.invisible()
        binding.tabGroup.invisible()
    }

    private fun hideEmptyView(){
        binding.refreshLayout.visible()
        binding.tabGroup.visible()
    }

    private fun showProgress() {
        binding.tabGroup.invisible()
        binding.shimmerLayout.visible()
        binding.shimmerLayout.startShimmer()
    }

    private fun hideProgress() {
        binding.shimmerLayout.invisible()
        binding.shimmerLayout.stopShimmer()
        binding.tabGroup.visible()
    }

    private fun setPagerAdapter(categories: List<CategoryAwardsModel>) {
        binding.apply {
            pagerAdapter =
                AwardsListStateAdapter(
                    childFragmentManager,
                    viewLifecycleOwner.lifecycle,
                    categories,
                    showOnlyMyAwards
                )
            viewPager.adapter = pagerAdapter
            viewPager.isUserInputEnabled = false
            initTabLayout(categories)
        }

    }

    private fun initTabLayout(categories: List<CategoryAwardsModel>) {
        binding.apply {
            viewPager.adapter = pagerAdapter

            mediator = TabLayoutMediator(tabGroup, viewPager) { tab, pos ->
            }
            mediator?.attach()
        }

        val tabTitle = getListCategoriesTitles(categories)
        binding.tabGroup.setTabTitles(tabTitle)


    }

    private fun getListCategoriesTitles(categories: List<CategoryAwardsModel>): List<String> {
        val result = mutableListOf<String>(requireContext().getString(R.string.awards_all))
        categories.map {
            result.add(it.name)
        }
        result.add(requireContext().getString(R.string.awards_other))
        return result
    }

    override fun applyTheme() {
    }

    companion object {
        private const val TAG = "AwardsFragment"
        private const val AWARDS_FRAGMENT_MY_AWARDS = "${TAG} my awards"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param onlyMyAwards Show only received awards.
         * @return A new instance of fragment AwardsFragment.
         */
        @JvmStatic
        fun newInstance(onlyMyAwards: Boolean) =
            AwardsFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(AWARDS_FRAGMENT_MY_AWARDS, onlyMyAwards)
                }
            }
    }
}