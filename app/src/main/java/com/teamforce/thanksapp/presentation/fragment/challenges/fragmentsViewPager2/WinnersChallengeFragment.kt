package com.teamforce.thanksapp.presentation.fragment.challenges.fragmentsViewPager2

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentWinnersChallengeBinding
import com.teamforce.thanksapp.domain.models.general.ObjectsComment
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.presentation.adapter.challenge.WinnersAdapter
import com.teamforce.thanksapp.presentation.adapter.decorators.VerticalDividerItemDecorator
import com.teamforce.thanksapp.presentation.adapter.decorators.VerticalDividerLastItemForComments
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts.ALLOW_LIKE_WINNERS
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts.CHALLENGER_ID
import com.teamforce.thanksapp.presentation.viewmodel.challenge.ResultsChallengeViewModel
import com.teamforce.thanksapp.presentation.viewmodel.challenge.WinnersChallengeViewModel
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.showSnackBarAboutNetworkProblem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WinnersChallengeFragment :
    BaseFragment<FragmentWinnersChallengeBinding>(FragmentWinnersChallengeBinding::inflate) {

    private val resultChallengeViewModel: ResultsChallengeViewModel by activityViewModels()
    private val viewModel: WinnersChallengeViewModel by viewModels()
    private var idChallenge: Int? = null
    private var allowLikeWinners: Boolean? = null
    private var listAdapter: WinnersAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idChallenge = it.getInt(CHALLENGER_ID)
            allowLikeWinners = it.getBoolean(ALLOW_LIKE_WINNERS)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWinnersAdapter()
        loadWinners()
        setData()
        listeningResponse()

        viewModel.internetError.observe(viewLifecycleOwner) {
            showSnackBarAboutNetworkProblem(view, requireContext())
        }

        resultChallengeViewModel.needUpdateComment.observe(viewLifecycleOwner) {
            if (it) {
                updateCommentAmount(resultChallengeViewModel.commentPositionForUpdate())
                resultChallengeViewModel.setNeedUpdateCommentState(false)
            }
        }

        viewModel.likeError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateCommentAmount(position: Int) {
        loadWinners()
        listAdapter?.notifyItemChanged(position)
    }

    override fun applyTheme() {

    }


    private fun initWinnersAdapter() {
        listAdapter = WinnersAdapter(
            onLikeClicked = ::onLikeClicked,
            onCommentClicked = ::onCommentClicked,
            allowLikeWinners = allowLikeWinners,
            onLikeLongClicked = ::onLikeLongClicked
        )
        binding.winnersRv.adapter = listAdapter
        binding.winnersRv.addItemDecoration(VerticalDividerLastItemForComments())
        listAdapter?.let {
            binding.winnersRv.addItemDecoration(
                VerticalDividerItemDecorator(10, it.itemCount)
            )
        }
    }

    private fun onLikeLongClicked(reportId: Int) {
        val bundle = Bundle().apply {
            putInt(Consts.LIKE_TO_OBJECT_ID, reportId)
            putParcelable(Consts.LIKE_TO_OBJECT_TYPE, ObjectsToLike.REPORT_OF_CHALLENGE)
        }
        view?.findNavController()?.navigateSafely(R.id.action_global_reactionsFragment, bundle)

    }

    private fun loadWinners() {
        idChallenge?.let { viewModel.loadWinners(it) }
    }

    private fun setData() {
        viewModel.winners.observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                binding.noWinners.visibility = View.GONE
                (binding.winnersRv.adapter as WinnersAdapter).submitList(it)
            } else {
                binding.noWinners.visibility = View.VISIBLE
            }
        }
    }

    private fun listeningResponse() {
        viewModel.winnersError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
    }

    private fun onLikeClicked(reportId: Int, position: Int) {
        viewModel.pressLike(reportId, position)
        viewModel.likeResult.observe(viewLifecycleOwner) {
            it?.let { likeResponse ->
                listAdapter?.updateLikesCount(
                    likeResponse.position,
                    likeResponse.likesAmount,
                    likeResponse.isLiked
                )
            }
        }
    }

    private fun onCommentClicked(reportId: Int, position: Int) {
        resultChallengeViewModel.setCommentPositionForUpdate(position)

        val bundle = Bundle().apply {
            putInt(Consts.OBJECTS_COMMENT_ID, reportId)
            putParcelable(Consts.OBJECTS_COMMENT_TYPE, ObjectsComment.REPORT_OF_CHALLENGE)
        }
        view?.findNavController()
            ?.navigateSafely(R.id.action_global_commentsBottomSheetFragment, bundle)

    }

    override fun onResume() {
        super.onResume()
        loadWinners()
        setData()
    }


    override fun onDestroyView() {
        listAdapter = null
        binding.winnersRv.adapter = null
        super.onDestroyView()
    }

    companion object {

        @JvmStatic
        fun newInstance(challengeId: Int, allowLikeWinners: Boolean) =
            WinnersChallengeFragment().apply {
                arguments = Bundle().apply {
                    putInt(CHALLENGER_ID, challengeId)
                    putBoolean(ALLOW_LIKE_WINNERS, allowLikeWinners)
                }
            }
    }
}