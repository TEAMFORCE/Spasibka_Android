package com.teamforce.thanksapp.presentation.fragment.reactions

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentReactionsBinding
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.presentation.adapter.reactions.ReactionsPageAdapter
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.presentation.viewmodel.reactions.ReactionsViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Consts.LIKE_TO_OBJECT_ID
import com.teamforce.thanksapp.utils.Consts.LIKE_TO_OBJECT_TYPE
import com.teamforce.thanksapp.utils.Consts.USER_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReactionsFragment : BaseBottomSheetDialogFragment<FragmentReactionsBinding>(FragmentReactionsBinding::inflate) {

    private val viewModel: ReactionsViewModel by viewModels()

    private var listAdapter: ReactionsPageAdapter? = null
    private var likeToObjectId: Int? = null
    private var objectType: ObjectsToLike? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            likeToObjectId = it.getInt(LIKE_TO_OBJECT_ID)
            objectType = it.getParcelableExt(LIKE_TO_OBJECT_TYPE, ObjectsToLike::class.java)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAdapter = ReactionsPageAdapter()
        binding.reactionsRv.adapter = listAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            if(likeToObjectId != null && objectType != null){
                viewModel.loadReactions(likeToObjectId!!, objectType!!).collectLatest { reactions ->
                    listAdapter?.submitData(reactions)
                }
            }
        }
        listAdapter!!.onReactionClicked = { userId: Int ->
            val bundle = Bundle()
            bundle.putInt(USER_ID, userId)
            findNavController().navigateSafely(
                R.id.action_global_someonesProfileFragment,
                bundle,
                OptionsTransaction().optionForTransactionWithSaveBackStack
            )
        }

        listAdapter!!.addLoadStateListener { loadState ->
            when (loadState.refresh) {
                is LoadState.Loading -> showProgress()
                is LoadState.Error -> {
                    hideProgress()
                }
                is LoadState.NotLoading -> hideProgress()
            }
        }
    }

    private fun showProgress(){
        binding.reactionsRv.invisible()
        binding.shimmerLayout.visible()
        binding.shimmerLayout.startShimmer()
    }

    private fun hideProgress(){
        binding.shimmerLayout.invisible()
        binding.shimmerLayout.stopShimmer()
        binding.reactionsRv.visible()
    }

    override fun applyTheme() {

    }

    override fun onDestroyView() {
        binding.reactionsRv.adapter = null
        listAdapter = null
        super.onDestroyView()
    }

    companion object {

        @JvmStatic
        fun newInstance(likeToObjectId: Int, likeToObjectType: ObjectsToLike) =
            ReactionsFragment().apply {
                arguments = Bundle().apply {
                    putInt(LIKE_TO_OBJECT_ID, likeToObjectId)
                    putParcelable(LIKE_TO_OBJECT_TYPE, likeToObjectType)
                }
            }
    }
}