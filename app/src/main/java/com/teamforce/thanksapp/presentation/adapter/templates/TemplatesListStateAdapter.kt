package com.teamforce.thanksapp.presentation.adapter.templates

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.teamforce.thanksapp.data.api.ScopeRequestParams
import com.teamforce.thanksapp.presentation.fragment.templates.TemplatesListFragment

class TemplatesListStateAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> TemplatesListFragment.newInstance(ScopeRequestParams.TEMPLATES)
            1 -> TemplatesListFragment.newInstance(ScopeRequestParams.ORGANIZATION)
            2 -> TemplatesListFragment.newInstance(ScopeRequestParams.COMMON)
            else -> TemplatesListFragment.newInstance(ScopeRequestParams.TEMPLATES)
        }
    }

}