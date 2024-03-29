package com.teamforce.thanksapp.presentation.fragment.challenges.challengeChains

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentQuestStepsListBinding
import com.teamforce.thanksapp.domain.models.challenge.ChallengeCondition
import com.teamforce.thanksapp.domain.models.challenge.challengeChains.StepModel
import com.teamforce.thanksapp.presentation.adapter.challenge.challengeChains.QuestStepAdapter
import com.teamforce.thanksapp.presentation.adapter.decorators.VerticalDividerDecoratorForListWithBottomBar
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengeListFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts
import com.teamforce.thanksapp.presentation.fragment.challenges.challengeChains.DetailsMainChallengeChainFragment.Companion.CHAIN_ID
import com.teamforce.thanksapp.presentation.fragment.challenges.challengeChains.DetailsMainChallengeChainFragment.Companion.CHAIN_NAME
import com.teamforce.thanksapp.presentation.viewmodel.challenge.challengeChain.DetailsMainChallengeChainViewModel
import com.teamforce.thanksapp.presentation.viewmodel.challenge.challengeChain.QuestStepsListViewModel
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.dp
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel

@AndroidEntryPoint
class QuestStepsListFragment : Fragment(R.layout.fragment_quest_steps_list) {

    private val binding: FragmentQuestStepsListBinding by viewBinding()
    private val viewModel: QuestStepsListViewModel by viewModels()
    private val sharedViewModel: DetailsMainChallengeChainViewModel by activityViewModels()


    private val listAdapter = QuestStepAdapter(::onItemClick)

    private var chainId: Long? = null
    private var chainName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            chainId = it.getLong(CHAIN_ID)
            chainName = it.getString(CHAIN_NAME)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        binding.stepsRv.apply {
            adapter = listAdapter
        }

        chainId?.let {
            viewModel.loadSteps(it)
        }

        viewModel.steps.observe(viewLifecycleOwner) {

            (binding.stepsRv.adapter as QuestStepAdapter).submitList(it)
            saveSteps()
        }
    }

    private fun onItemClick(item: StepModel.Task) {
        val bundle = Bundle().apply { putInt(ChallengesConsts.CHALLENGER_ID, item.id.toInt()) }
        view?.findNavController()?.navigateSafely(
            R.id.action_global_detailsMainChallengeFragment,
            bundle,
            OptionsTransaction().optionForEditProfile
        )
    }

    private fun saveSteps(){
        val currentSteps = (binding.stepsRv.adapter as QuestStepAdapter).currentList
        sharedViewModel.saveSteps(currentSteps)
    }



    private fun handleTopAppBar() {
        binding.header.toolbar.title = getString(R.string.challenge_chain_steps_2)
        binding.header.closeBtn.visible()
        binding.header.closeBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.header.toolbar.subtitle = HtmlCompat.fromHtml(
            String.format(
                getString(R.string.challenge_chain_name_prefix),
                chainName.orEmpty()
            ), HtmlCompat.FROM_HTML_MODE_COMPACT
        )


        binding.stepsRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val currentElevation = binding.header.toolbar.translationZ
                val targetElevation = if (recyclerView.computeVerticalScrollOffset() > 20.dp) {
                    3.dp.toFloat()
                } else {
                    0.toFloat()
                }

                if (currentElevation != targetElevation) {
                    binding.header.toolbar.translationZ = targetElevation
                }
            }
        })
    }


    companion object {

        private const val TAG = "QuestStepsListFragment"


        @JvmStatic
        fun newInstance(chainId: Long) =
            QuestStepsListFragment().apply {
                arguments = Bundle().apply {
                    putLong(CHAIN_ID, chainId)
                }
            }
    }
}