package com.teamforce.thanksapp.presentation.adapter.awards

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.teamforce.thanksapp.domain.models.awards.CategoryAwardsModel
import com.teamforce.thanksapp.presentation.fragment.awardsScreen.AwardsListFragment
import com.teamforce.thanksapp.utils.Consts


class AwardsListStateAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val categories: List<CategoryAwardsModel>,
    private val showOnlyMyAwards: Boolean
): FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = categories.size + 1

    override fun createFragment(position: Int): Fragment {
        return if(position == 0){
            AwardsListFragment.newInstance(
                showAllCategory = true,
                showOnlyMyAwards = showOnlyMyAwards
            )
        }else if (categories[position - 1].id != Consts.OTHER_CATEGORY_ID) {
            AwardsListFragment.newInstance(
                categories[position - 1].id,
                showOnlyMyAwards = showOnlyMyAwards
            )

        }else if (categories[position - 1].id == Consts.OTHER_CATEGORY_ID) {
            AwardsListFragment.newInstance(
                categories[position - 1].id,
                showOnlyMyAwards = showOnlyMyAwards
            )
        }else{
            AwardsListFragment.newInstance(
                showAllCategory = true,
                showOnlyMyAwards = showOnlyMyAwards
            )
        }
    }
}