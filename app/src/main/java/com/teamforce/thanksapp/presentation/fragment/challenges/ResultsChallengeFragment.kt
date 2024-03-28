package com.teamforce.thanksapp.presentation.fragment.challenges

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentResultsChallengeBinding
import com.teamforce.thanksapp.domain.models.challenge.ChallengeType
import com.teamforce.thanksapp.domain.models.challenge.SectionOfChallenge
import com.teamforce.thanksapp.domain.models.challenge.SectionsOfChallengeType
import com.teamforce.thanksapp.model.domain.ChallengeModelById
import com.teamforce.thanksapp.presentation.adapter.challenge.ResultsChallengeStateAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.fragmentsViewPager2.CreateReportFragment
import com.teamforce.thanksapp.presentation.fragment.reactions.CommentsBottomSheetFragment
import com.teamforce.thanksapp.presentation.viewmodel.challenge.DetailsMainChallengeViewModel
import com.teamforce.thanksapp.presentation.viewmodel.challenge.ResultsChallengeViewModel
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.parceleable
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ResultsChallengeFragment :
    BaseFragment<FragmentResultsChallengeBinding>(FragmentResultsChallengeBinding::inflate) {

    private val detailChallengeViewModel: DetailsMainChallengeViewModel by activityViewModels()
    private val resultChallengeViewModel: ResultsChallengeViewModel by activityViewModels()
    private var initSectionPage: SectionsOfChallengeType? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            initSectionPage = it.parceleable(DetailsMainChallengeFragment.INIT_SECTION_PAGE)
        }

        setFragmentResultListener(CommentsBottomSheetFragment.COMMENTS_REQUEST_KEY) { requestKey, bundle ->
            val result = bundle.getInt(
                CommentsBottomSheetFragment.COMMENTS_AMOUNT_BUNDLE_KEY
            )
            resultChallengeViewModel.setNeedUpdateCommentState(true)
        }

        setFragmentResultListener(CreateReportFragment.CREATE_REPORT_REQUEST_KEY) { requestKey, bundle ->
            val result = bundle.getBoolean(
                CreateReportFragment.CREATE_REPORT_DRAFT_UPDATED
            )
            resultChallengeViewModel.setNeedUpdateDraft(result)

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                detailChallengeViewModel.viewState.collectLatest { state ->
                    if(state.challenge != null){
                        setViewPager(state.challenge)
                        setSubtitle(state.challenge.name ?: "")
                    }else{
                        // TODO Данных о челлендже нет
                    }
                }
            }
        }
    }



    private fun setViewPager(challenge: ChallengeModelById) {
        val tabs = initTabs(challenge)
        binding.pager.adapter = ResultsChallengeStateAdapter(
            requireActivity(),
            tabs,
            challenge
        )
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
        }.attach()
        binding.tabLayout.setTabTitles(tabs.map { it.title })
        handleInitSectionPage(tabs)
    }

    private fun handleInitSectionPage(tabs: List<SectionOfChallenge>){
        var targetIndex: Int =  0
        tabs.forEachIndexed { index, sectionOfChallenge ->
            if(initSectionPage == sectionOfChallenge.type){
                targetIndex = index
                return@forEachIndexed
            }
        }
        binding.pager.setCurrentItem(targetIndex, true)
        initSectionPage = null
    }


    private fun initTabs(challenge: ChallengeModelById): MutableList<SectionOfChallenge> {
        val challengeSections = mutableListOf<SectionOfChallenge>()

        challengeSections.add(
            SectionOfChallenge(
                requireContext().getString(R.string.winners),
                SectionsOfChallengeType.WINNERS
            )
        )

        // Показ претендентов, если чалик с голосованием, то показываем всегда претендентов
        if (challenge.typeOfChallenge == ChallengeType.VOTING || challenge.showContenders) {
            challengeSections.add(
                SectionOfChallenge(
                    requireContext().getString(R.string.contenders),
                    SectionsOfChallengeType.CONTENDERS
                )
            )
        }

        if (!detailChallengeViewModel.viewState.value.myResult.isNullOrEmpty() || hasReportDraft(challenge.id)) {
            challengeSections.add(
                SectionOfChallenge(
                    requireContext().getString(R.string.myResult),
                    SectionsOfChallengeType.MY_RESULT
                )
            )
        }

//        if (challenge.dependencies.isNotEmpty()) {
//            challengeSections.add(
//                SectionOfChallenge(
//                    requireContext().getString(R.string.dependencies),
//                    SectionsOfChallengeType.DEPENDENCIES
//                )
//            )
//        }

        return challengeSections

    }

    private fun hasReportDraft(challengeId: Int): Boolean {
        val sharedPref = requireContext().getSharedPreferences("report${challengeId}", 0)
        return (!sharedPref.getString("commentReport${challengeId}", "").isNullOrEmpty() ||
                !sharedPref.getString("imageReport${challengeId}", "").isNullOrEmpty())

    }


    private fun handleTopAppBar() {
        binding.header.toolbar.title = getString(R.string.results_challenge_title)
        binding.header.closeBtn.visible()
        binding.header.closeBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setSubtitle(subtitle: String){
        binding.header.toolbar.subtitle = HtmlCompat.fromHtml(
            String.format(
                getString(R.string.results_challenge_subtitle),
                subtitle
            ), HtmlCompat.FROM_HTML_MODE_COMPACT
        )
    }


    override fun applyTheme() {

    }

    companion object {

        const val RESULT_CHALLENGE_REQUEST_KEY = "Result Challenge Request Key"
        const val RESULT_CHALLENGE_AMOUNT_COMMENTS_BUNDLE_KEY =
            "Result Challenge Amount Comments Bundle Key"


        @JvmStatic
        fun newInstance() =
            ResultsChallengeFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}