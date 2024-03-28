package com.teamforce.thanksapp.presentation.fragment.reactions

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.CommentInputLayoutBinding
import com.teamforce.thanksapp.databinding.FragmentCommentsBottomSheetBinding
import com.teamforce.thanksapp.domain.models.general.ObjectsComment
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.presentation.adapter.CommentAdapter
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.presentation.viewmodel.challenge.CommentsViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Extensions.observeOnce
import com.teamforce.thanksapp.utils.branding.Branding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class CommentsBottomSheetFragment :
    BaseBottomSheetDialogFragment<FragmentCommentsBottomSheetBinding>(
        FragmentCommentsBottomSheetBinding::inflate
    ) {

    private var _inputBinding: CommentInputLayoutBinding? = null
    val inputBinding
        get() = _inputBinding!!

    private val viewModel: CommentsViewModel by viewModels()
    private var listAdapter: CommentAdapter? = null

    private var objectsCommentId: Int? = null
    private var objectType: ObjectsComment? = null
    private var objectName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            objectsCommentId = it.getInt(Consts.OBJECTS_COMMENT_ID)
            objectName = it.getString(OBJECTS_COMMENT_NAME)
            objectType = it.getParcelableExt(
                Consts.OBJECTS_COMMENT_TYPE,
                ObjectsComment::class.java
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _inputBinding =
            CommentInputLayoutBinding.inflate(LayoutInflater.from(parentFragment?.context))
        return super.onCreateView(inflater, container, savedInstanceState)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        val density = requireContext().resources.displayMetrics.density
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        dialog.setOnShowListener { dialogI ->
            // Достаём корневые лэйауты из исходников
            val coordinator =
                (dialogI as BottomSheetDialog).findViewById<CoordinatorLayout>(com.google.android.material.R.id.coordinator)
            val containerLayout =
                dialogI.findViewById<FrameLayout>(com.google.android.material.R.id.container)

            inputBinding.cardInputComment.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                height = (120 * density).toInt()
                gravity = Gravity.BOTTOM
            }
            containerLayout?.addView(inputBinding.cardInputComment)

            // Перерисовываем лэйаут
            inputBinding.cardInputComment.post {
                (coordinator?.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    inputBinding.cardInputComment.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    // Устраняем разрыв между кнопкой и скролящейся частью
                    this.bottomMargin =
                        (inputBinding.cardInputComment.measuredHeight - 8 * density).toInt()
                    containerLayout?.requestLayout()
                }
            }
        }


        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            allowExpandBSDF(it)
        }
    }

    private fun allowExpandBSDF(dialog: Dialog) {
        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        val bottomSheetBehavior = (this.dialog as? BottomSheetDialog)?.behavior

        binding.commentsRv.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            //   val isScrollingUp = scrollY < oldScrollY
            val isScrollingDown = scrollY > oldScrollY

            //    val isListAtTop = !binding.commentsRv.canScrollVertically(-1)
            val isListAtBottom = !binding.commentsRv.canScrollVertically(1) // Низ списка
            // Проверяем, нужно ли включить скроллинг списка
            binding.commentsRv.isNestedScrollingEnabled =
                !(isListAtBottom && bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED && isScrollingDown)

        }



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
            if (loadState.refresh is LoadState.Error) showError()
            checkIfListEmpty()
        }
        binding.commentsRv.adapter = listAdapter?.withLoadStateHeaderAndFooter(
            header = HistoryLoadStateAdapter(),
            footer = HistoryLoadStateAdapter()
        )


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
        lifecycleScope.launchWhenStarted {
            viewModel.loadComments(objectsCommentId, typeOfObject).collectLatest {
                listAdapter?.submitData(it)
                hideError()
            }
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
        hideKeyboardFrom(requireContext(), binding.root)
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
        inputBinding.textFieldMessage.setEndIconOnClickListener {
            sendMessage()
        }

        inputBinding.messageValueEt.setKeyBoardInputCallbackListener { inputContentInfo, _, _ ->
            if (inputContentInfo != null) {
                if (objectsCommentId != null && objectType != null) {
                    sendGifInComment(
                        objectsCommentId!!,
                        objectType!!,
                        (inputContentInfo.linkUri ?: getFileFromContentUri(
                            requireContext(),
                            inputContentInfo.contentUri,
                            true
                        ).path).toString()
                    )
                }

            }
        }
    }

    private fun sendMessage() {
        if (inputBinding.messageValueEt.text?.trim()?.length!! > 0
            && objectsCommentId != null && objectType != null
        ) {
            createComment(
                objectsCommentId!!,
                objectType!!,
                inputBinding.messageValueEt.text.toString(),
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

    private fun clearInputField() = inputBinding.messageValueEt.text?.clear()

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onStop() {
        setFragmentResult(
            COMMENTS_REQUEST_KEY, bundleOf(
                COMMENTS_AMOUNT_BUNDLE_KEY to binding.commentsRv.adapter?.itemCount
            )
        )
        super.onStop()
    }

    override fun onDestroyView() {
        binding.commentsRv.adapter = null
        listAdapter = null
        super.onDestroyView()
    }

    private fun handleTopAppBar() {
        binding.header.toolbar.title = getString(R.string.comments_fragment_title)
        binding.header.closeBtn.visible()
        binding.header.closeBtn.setOnClickListener {
            dialog?.dismiss()
        }

        val subtitle = getSubtitleStringDependsOnType(objectType, objectName)
        subtitle?.let {
            binding.header.toolbar.subtitle = it
        }

        binding.commentsRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    private fun getSubtitleStringDependsOnType(
        objectType: ObjectsComment?,
        objectName: String?
    ): Spanned? {
        return objectType?.let {
            val subtitleTemplate = when (objectType) {
                ObjectsComment.CHALLENGE -> R.string.comments_fragment_subtitle_challenge
                ObjectsComment.REPORT_OF_CHALLENGE -> R.string.comments_fragment_subtitle_challenge_report
                ObjectsComment.OFFER -> R.string.comments_fragment_subtitle_offer
                ObjectsComment.TRANSACTION -> R.string.comments_fragment_subtitle_transaction
            }

            HtmlCompat.fromHtml(
                String.format(
                    getString(subtitleTemplate),
                    objectName.orEmpty()
                ), HtmlCompat.FROM_HTML_MODE_COMPACT
            )

        }
    }

    companion object {

        const val COMMENTS_REQUEST_KEY = "Comments Request Key"
        const val COMMENTS_AMOUNT_BUNDLE_KEY = "Comments Amount Bundle Key"
        const val OBJECTS_COMMENT_NAME = "Object Comment Name"

        @JvmStatic
        fun newInstance(
            objectsCommentId: Int,
            typeOfObject: ObjectsComment,
            objectName: String = ""
        ) =
            CommentsBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt(Consts.OBJECTS_COMMENT_ID, objectsCommentId)
                    putString(Consts.OBJECTS_COMMENT_ID, objectName)
                    putParcelable(Consts.OBJECTS_COMMENT_TYPE, typeOfObject)
                }
            }
    }
}