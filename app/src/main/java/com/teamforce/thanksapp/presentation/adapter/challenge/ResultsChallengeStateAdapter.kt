package com.teamforce.thanksapp.presentation.adapter.challenge

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.teamforce.thanksapp.domain.models.challenge.SectionOfChallenge
import com.teamforce.thanksapp.domain.models.challenge.SectionsOfChallengeType
import com.teamforce.thanksapp.domain.models.general.ObjectsComment
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.model.domain.ChallengeModelById
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengeListFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.DetailsMainChallengeFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.fragmentsViewPager2.ContendersChallengeFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.fragmentsViewPager2.MyResultChallengeFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.fragmentsViewPager2.WinnersChallengeFragment
import com.teamforce.thanksapp.presentation.fragment.reactions.CommentsBottomSheetFragment
import com.teamforce.thanksapp.presentation.fragment.reactions.ReactionsFragment

class ResultsChallengeStateAdapter(
    fragment: FragmentActivity,
    val tabsList: MutableList<SectionOfChallenge>,
    val challenge: ChallengeModelById
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return tabsList.count()
    }

    override fun createFragment(position: Int): Fragment {
        return returnFragmentByTabsList(tabsList[position].type)
    }

    private fun returnFragmentByTabsList(tabType: SectionsOfChallengeType): Fragment {

        return when (tabType) {
            SectionsOfChallengeType.DETAIL -> DetailsMainChallengeFragment.newInstance()
            SectionsOfChallengeType.COMMENTS -> CommentsBottomSheetFragment.newInstance(challenge.id, ObjectsComment.CHALLENGE)
            SectionsOfChallengeType.WINNERS -> WinnersChallengeFragment.newInstance(challenge.id, challenge.allowLikeWinners)
            SectionsOfChallengeType.CONTENDERS -> {
                ContendersChallengeFragment.newInstance(
                    challenge.id,
                    challenge.creatorId, challenge.typeOfChallenge, challenge.showContenders, challenge.active
                )
            }
            SectionsOfChallengeType.MY_RESULT -> MyResultChallengeFragment.newInstance(challenge.id)
            SectionsOfChallengeType.REACTIONS -> ReactionsFragment.newInstance(challenge.id, ObjectsToLike.CHALLENGE)
            SectionsOfChallengeType.DEPENDENCIES -> ChallengeListFragment.newInstance(challengeId = challenge.id)
        }

    }

}