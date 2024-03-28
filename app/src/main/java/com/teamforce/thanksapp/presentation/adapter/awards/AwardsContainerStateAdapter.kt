package com.teamforce.thanksapp.presentation.adapter.awards

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.teamforce.thanksapp.presentation.fragment.awardsScreen.AwardsFragment


class AwardsContainerStateAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
): FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AwardsFragment.newInstance(false)
            1 -> AwardsFragment.newInstance(true)
            else -> AwardsFragment.newInstance(false)
        }
    }
}