package com.teamforce.thanksapp.presentation.fragment.awardsScreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.teamforce.thanksapp.NotificationSharedViewModel
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentAwardsContainerBinding
import com.teamforce.thanksapp.presentation.adapter.awards.AwardsContainerStateAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.theme.ThemableToggleButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AwardsContainerFragment : BaseFragment<FragmentAwardsContainerBinding>(
    FragmentAwardsContainerBinding::inflate) {

    private val sharedViewModel: NotificationSharedViewModel by activityViewModels()
    private var pagerAdapter: AwardsContainerStateAdapter? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        setPagerAdapter()
        disableHorizScrollForViewPager()
    }

    private fun setPagerAdapter() {
        binding.apply {
            pagerAdapter =
                AwardsContainerStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
            viewPager.adapter = pagerAdapter
            viewPager.isUserInputEnabled = false
        }

        // Set color for Initial checked element
        binding.toggleGroup.findViewById<ThemableToggleButton>(binding.toggleGroup.checkedButtonId).setColorByChecked(true)

        binding.toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            group.findViewById<ThemableToggleButton>(checkedId).setColorByChecked(isChecked)
            if (isChecked) {
                when (checkedId) {
                    R.id.all_awards_btn -> binding.viewPager.currentItem = 0
                    R.id.my_awards_btn -> binding.viewPager.currentItem = 1
                }
            }
        }

    }

    private fun handleTopAppBar() {
        binding.header.toolbar.title = requireContext().getString(R.string.awards_label)

        binding.header.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }


    private fun disableHorizScrollForViewPager(){
        binding.viewPager.isUserInputEnabled = false
    }

    override fun applyTheme() {
    }


    companion object {

        private const val TAG = "AwardsContainerFragment"

        @JvmStatic
        fun newInstance() =
            AwardsContainerFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}