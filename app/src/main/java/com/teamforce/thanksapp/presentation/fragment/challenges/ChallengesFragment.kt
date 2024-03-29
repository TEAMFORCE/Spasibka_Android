package com.teamforce.thanksapp.presentation.fragment.challenges

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialContainerTransform.FadeMode
import com.google.android.material.transition.platform.Hold
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialElevationScale
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentChallengesBinding
import com.teamforce.thanksapp.presentation.adapter.challenge.ChallengeListStateAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.utils.dp
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChallengesFragment :
    BaseFragment<FragmentChallengesBinding>(FragmentChallengesBinding::inflate) {

    private var pagerAdapter: ChallengeListStateAdapter? = null
    private var mediator: TabLayoutMediator? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            duration = 400.toLong()
            scrimColor = Color.WHITE
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        handleTopAppBar()
        setPagerAdapter()
        view.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    private fun setPagerAdapter() {
        binding.apply {
            pagerAdapter =
                ChallengeListStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
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

        binding.tabGroup.setTabTitles(listOf(
            requireContext().getString(R.string.allChallenge),
            getString(R.string.activeChallenge),
            getString(R.string.delayedChallenges),
        ))

    }



    override fun onDestroyView() {
        mediator?.detach()
        mediator = null
        binding.viewPager.adapter = null
        pagerAdapter = null
        super.onDestroyView()

    }

    override fun applyTheme() {


    }

    private fun handleTopAppBar() {
        binding.header.toolbar.title = requireContext().getString(R.string.challenge_label)
        binding.header.challengeMenuBtn.visible()
        binding.header.challengeMenuBtn.setOnClickListener {
            showSimpleDialog()
        }
    }

    private fun showSimpleDialog(){
        val items = arrayOf(getString(R.string.create_challenge_dialog), getString(R.string.templates_title))

        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_ThanksApp_Dialog_Simple)
            .setItems(items) { dialog, which ->
                when(which){
                    0 -> {
                        findNavController().navigateSafely(R.id.action_global_createChallengeFragment)
                    }
                    1 -> {
                        findNavController().navigateSafely(R.id.action_global_templatesFragment)
                    }
                }
            }
            .show()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ChallengesFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

}