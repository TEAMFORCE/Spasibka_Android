package com.teamforce.thanksapp.presentation.fragment.reactions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.insertFooterItem
import androidx.paging.map
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.textfield.TextInputLayout
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentCommentsBinding
import com.teamforce.thanksapp.domain.models.general.ObjectsComment
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.model.domain.CommentModel
import com.teamforce.thanksapp.presentation.adapter.CommentAdapter
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.viewmodel.challenge.CommentsViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Extensions.observeOnce
import com.teamforce.thanksapp.utils.branding.Branding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class CommentsFragment : BaseFragment<FragmentCommentsBinding>(FragmentCommentsBinding::inflate) {

    private val viewModel: CommentsViewModel by viewModels()
    private var listAdapter: CommentAdapter? = null

    private var objectsCommentId: Int? = null
    private var objectType: ObjectsComment? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            objectsCommentId = it.getInt(Consts.OBJECTS_COMMENT_ID)
            objectType = it.getParcelableExt(
                Consts.OBJECTS_COMMENT_TYPE,
                ObjectsComment::class.java
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAdapter = CommentAdapter(viewModel.getProfileId(), ::deleteComment, ::likeComment, ::onLikeLongClicked)
        binding.commentsRv.adapter = listAdapter
        if (objectsCommentId != null && objectType != null) {
            loadComment(
                objectsCommentId!!,
                objectType!!
            )
        }
        listeners()
        listAdapter!!.addLoadStateListener { loadState ->
            // TODO Ошибка может быть не только из за интернета, нужно обрабатывать разные случаи
            // Выводить сообщение от бека как минимум
            if (loadState.refresh is LoadState.Error) showError()
            checkIfListEmpty()
        }
        binding.commentsRv.adapter = listAdapter?.withLoadStateHeaderAndFooter(
            header = HistoryLoadStateAdapter(),
            footer = HistoryLoadStateAdapter()
        )

        listAdapter?.addLoadStateListener { state ->
            binding.refreshLayout.isRefreshing = state.refresh == LoadState.Loading
        }

        binding.refreshLayout.setOnRefreshListener {
            listAdapter?.refresh()
            binding.refreshLayout.isRefreshing = true
        }

    }
    private fun checkIfListEmpty() {
        if (binding.commentsRv.adapter?.itemCount == 0) {
            binding.stateInfoTv.visible()
            binding.stateInfoTv.text = getString(R.string.comments_fragment_empty_text_msg)
        } else {
            binding.stateInfoTv.invisible()
        }
    }

    override fun applyTheme() {
    }

    private fun loadComment(objectsCommentId: Int, typeOfObject: ObjectsComment) {
        lifecycleScope.launch {
            viewModel.loadComments(objectsCommentId, typeOfObject).collectLatest {
                binding.refreshLayout.isRefreshing = false
                listAdapter?.submitData(it)
                hideError()
            }
        }
        viewModel.createComments.observe(viewLifecycleOwner) {
            // TODO Переписать Handler на что то вменяемое
            Handler().postDelayed(Runnable {
                listAdapter?.itemCount?.plus(1)
                    ?.let { it1 -> binding.commentsRv.smoothScrollToPosition(it1) }
            }, 400)
        }
    }

    private fun showError() {
        binding.stateInfoTv.visible()
        binding.commentsRv.invisible()
    }

    private fun hideError() {
        binding.stateInfoTv.invisible()
        binding.commentsRv.visible()
    }


    private fun createComment(
        objectId: Int,
        typeOfObject: ObjectsComment,
        text: String,
        gifUrl: String?
    ) {
        viewModel.createComment(
            objectId,
            typeOfObject,
            text.replace("\\s+".toRegex(), " "),
            gifUrl = gifUrl
        )
        clearInputField()
        closeKeyboard()
    }

    private fun listeners() {
        inputMessage()

        viewModel.createComments.observe(viewLifecycleOwner) {
            listAdapter?.refresh()
            lifecycleScope.launch {
                delay(600)
                withContext(Dispatchers.Main) {
                    binding.commentsRv.smoothScrollToPosition(listAdapter?.itemCount?.plus(1) ?: 0)
                }
            }
        }

        viewModel.deleteComment.observe(viewLifecycleOwner) {
            listAdapter?.refresh()
        }

        viewModel.commentsLoadingError.observe(viewLifecycleOwner) {
            showError()
            binding.stateInfoTv.text = it
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        viewModel.likeResult.observe(viewLifecycleOwner){
            it?.let { likeResponse ->
                listAdapter?.updateLikesCount(likeResponse.position, likeResponse.likesAmount, likeResponse.isLiked)
            }
        }
        viewModel.likeError.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

    }

    private fun deleteComment(commentId: Int) {
        viewModel.deleteComment(commentId)
    }


    private fun likeComment(commentId: Int, positionElement: Int) {
        viewModel.pressLike(commentId, positionElement)
    }

    private fun onLikeLongClicked(commentId: Int) {
        val bundle = Bundle().apply {
            putInt(Consts.LIKE_TO_OBJECT_ID, commentId)
            putParcelable(Consts.LIKE_TO_OBJECT_TYPE, ObjectsToLike.COMMENT)
        }
        findNavController().navigateSafely(R.id.action_global_reactionsFragment, bundle)
    }

    private fun inputMessage() {
        binding.inputLayout.textFieldMessage.setEndIconOnClickListener {
            sendMessage()
        }

        binding.inputLayout.messageValueEt.setKeyBoardInputCallbackListener { inputContentInfo, _, _ ->
            if (inputContentInfo != null) {
                if (objectsCommentId != null && objectType != null) {
                    sendGifInComment(
                        objectsCommentId!!,
                        objectType!!,
                        (inputContentInfo.linkUri ?: getFileFromContentUri(requireContext(), inputContentInfo.contentUri, true).path).toString()
                    )
                }

            }
        }
    }

    private fun sendMessage() {
        if (binding.inputLayout.messageValueEt.text?.trim()?.length!! > 0
            && objectsCommentId != null && objectType != null
        ) {
            createComment(
                objectsCommentId!!,
                objectType!!,
                binding.inputLayout.messageValueEt.text.toString(),
                null
            )
        }
    }

    private fun sendGifInComment(
        objectsCommentId: Int,
        objectType: ObjectsComment,
        gifUrl: String
    ) {
        createComment(
            objectId = objectsCommentId,
            typeOfObject = objectType,
            text = "",
            gifUrl = gifUrl
        )
    }

    private fun clearInputField() = binding.inputLayout.messageValueEt.text?.clear()
    private fun closeKeyboard() {
        val view: View? = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    override fun onDestroyView() {
        binding.commentsRv.adapter = null
        listAdapter = null
        super.onDestroyView()
    }

    companion object {

        @JvmStatic
        fun newInstance(objectsCommentId: Int, typeOfObject: ObjectsComment) =
            CommentsFragment().apply {
                arguments = Bundle().apply {
                    putInt(Consts.OBJECTS_COMMENT_ID, objectsCommentId)
                    putParcelable(Consts.OBJECTS_COMMENT_TYPE, typeOfObject)
                }
            }
    }
}