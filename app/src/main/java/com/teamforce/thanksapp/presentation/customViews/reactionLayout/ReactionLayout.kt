package com.teamforce.thanksapp.presentation.customViews.reactionLayout


import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.teamforce.thanksapp.R

import com.teamforce.thanksapp.databinding.ReactionLayoutBinding
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.utils.blur

class ReactionLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), Themable {

    private var _binding: ReactionLayoutBinding? = null
    private val binding get() = checkNotNull(_binding) { "Binding is null" }

    var onLikeClicked: (() -> Unit)? = null
    var onLongLikeClicked: (() -> Unit)? = null
    var onCommentClicked: (() -> Unit)? = null

    private var likesCount: Int = 0
    private var isLiked: Boolean = false

    init {
        _binding = ReactionLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        binding.likeCard.blur(2f, context, binding.linear)
        binding.commentCard.blur(2f, context, binding.linear)
        setupListeners()
    }

    override fun setThemeColor(theme: ColorsModel) {

    }

    private fun setupListeners() {
        binding.likeCard.setOnClickListener {
            onLikeClicked?.invoke()
        }

        binding.likeCard.setOnLongClickListener {
            onLongLikeClicked?.invoke()
            return@setOnLongClickListener true
        }

        binding.commentCard.setOnClickListener {
            onCommentClicked?.invoke()
        }
    }

    fun updateLikeData(likesAmount: Int?) {
        setLikesAmount(likesAmount)
        isLiked = !isLiked
        changeLikeView()
    }

    private fun changeLikeView() {
        val drawableRes = if (isLiked) R.drawable.ic_like_filled else R.drawable.ic_like
        binding.likeIv.setImageResource(drawableRes)
    }

    fun setIsLiked(isLikedOuter: Boolean) {
        isLiked = isLikedOuter
        changeLikeView()
    }

    fun setLikesAmount(likesAmount: Int?) {
        likesCount = likesAmount ?: 0
        binding.likesAmountTv.text = likesAmount.toString()
    }

    fun setCommentsAmount(commentsAmount: Int) {
        binding.commentsAmountTv.text = commentsAmount.toString()
    }

}