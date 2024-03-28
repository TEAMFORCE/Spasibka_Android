package com.teamforce.thanksapp.presentation.fragment.awardsScreen

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentAwardsListBinding
import com.teamforce.thanksapp.domain.models.awards.AwardsModel
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.StepModel
import com.teamforce.thanksapp.presentation.adapter.awards.AwardsCategoryAdapter
import com.teamforce.thanksapp.presentation.adapter.challenge.challengeChains.QuestStepAdapter
import com.teamforce.thanksapp.presentation.adapter.decorators.HorizontalDecoratorForLastItem
import com.teamforce.thanksapp.presentation.adapter.decorators.VerticalDividerDecoratorForListWithBottomBar
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts
import com.teamforce.thanksapp.presentation.viewmodel.awards.AwardsListViewModel
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.navigateSafely
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AwardsListFragment :
    BaseFragment<FragmentAwardsListBinding>(FragmentAwardsListBinding::inflate) {

    private val viewModel: AwardsListViewModel by activityViewModels()

    private val listAdapter = AwardsCategoryAdapter(::onItemClick)

    private var categoryId: Long? = null
    private var showAll: Boolean = false
    private var onlyMyAwards: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryId = it.getLong(AWARDS_LIST_CATEGORY_ID)
            showAll = it.getBoolean(AWARDS_LIST_SHOW_ALL)
            onlyMyAwards = it.getBoolean(AWARDS_LIST_MY_AWARDS)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.awardsRv.adapter = listAdapter

    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        if (categoryId == null || showAll) {
            if (onlyMyAwards) {
                viewModel.loadMyAwards()
                viewModel.myAwards.distinctUntilChanged().observe(viewLifecycleOwner) {
                    (binding.awardsRv.adapter as AwardsCategoryAdapter).submitList(it)
                }
            } else {
                viewModel.loadAwards(onlyMyAwards)
                viewModel.awards.distinctUntilChanged().observe(viewLifecycleOwner) {
                    (binding.awardsRv.adapter as AwardsCategoryAdapter).submitList(it)
                }
            }
        } else {
            if (onlyMyAwards) {
                viewModel.loadMyAwardsById(categoryId!!)
            } else {
                viewModel.loadAwardsById(categoryId!!, onlyMyAwards)
            }

            viewModel.awardsById.distinctUntilChanged().observe(viewLifecycleOwner) {
                (binding.awardsRv.adapter as AwardsCategoryAdapter).submitList(it[categoryId!!])
            }
        }

    }


    private fun onItemClick(item: AwardsModel) {
        val bundle = Bundle().apply { putParcelable(AwardDetailFragment.AWARD_DETAIL_OBJECT, item) }
        findNavController().navigateSafely(
            R.id.action_global_awardDetailFragment,
            bundle,
            OptionsTransaction().optionForAdditionalInfoFeedFragment
        )

    }

    override fun applyTheme() {

    }

    companion object {
        private const val TAG = "AwardsListFragment"
        private const val AWARDS_LIST_CATEGORY_ID = "awards list category id"
        private const val AWARDS_LIST_SHOW_ALL = "awards list show all"
        private const val AWARDS_LIST_MY_AWARDS = "${TAG} my awards"

        @JvmStatic
        fun newInstance(
            categoryId: Long? = null,
            showAllCategory: Boolean = false,
            showOnlyMyAwards: Boolean = false
        ) =
            AwardsListFragment().apply {
                arguments = Bundle().apply {
                    categoryId?.let { putLong(AWARDS_LIST_CATEGORY_ID, it) }
                    putBoolean(AWARDS_LIST_SHOW_ALL, showAllCategory)
                    putBoolean(AWARDS_LIST_MY_AWARDS, showOnlyMyAwards)
                }
            }
    }
}