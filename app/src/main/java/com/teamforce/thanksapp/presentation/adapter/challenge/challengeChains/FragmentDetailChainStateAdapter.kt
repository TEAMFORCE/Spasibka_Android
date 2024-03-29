package com.teamforce.thanksapp.presentation.adapter.challenge.challengeChains

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentDetailChainStateAdapter(
    fragment: FragmentActivity,
): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 1
    }

    override fun createFragment(position: Int): Fragment {
        TODO("Not yet implemented")
    }
}