package com.teamforce.thanksapp.presentation.customViews.reactionLayout

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ReactionLayoutBinding
import com.teamforce.thanksapp.databinding.ReactionLayoutForListItemBinding
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.utils.blur
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.invisible


class ReactionLayoutForListItem  @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), Themable {

    private var _binding: ReactionLayoutForListItemBinding? = null
    private val binding get() = checkNotNull(_binding) { "Binding is null" }

    var onLikeClicked: (() -> Unit)? = null
    var onLongLikeClicked: (() -> Unit)? = null
    var onCommentClicked: (() -> Unit)? = null

    private var _likesCounter: Int = 0
    private var _commentsCounter: Int = 0

    private var isLiked: Boolean = false

    private var hideCommentsButton: Boolean = false

    private var themeInner: ColorsModel = Branding.brand.colorsJson


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ReactionLayoutForListItem)
        hideCommentsButton = typedArray.getBoolean(R.styleable.ReactionLayoutForListItem_hideCommentsButton, false)
        typedArray.recycle()

        _binding = ReactionLayoutForListItemBinding.inflate(LayoutInflater.from(context), this, true)
        setupListeners()

        if (hideCommentsButton) hideCommentsBtn()
    }

    override fun setThemeColor(theme: ColorsModel) {
        themeInner = theme
    }

    private fun setupListeners() {
        binding.likeBtn.setOnClickListener {
            onLikeClicked?.invoke()
        }

        binding.likeBtn.setOnLongClickListener {
            if(_likesCounter > 0) onLongLikeClicked?.invoke()
            return@setOnLongClickListener true
        }

        binding.commentBtn.setOnClickListener {
            onCommentClicked?.invoke()
        }
    }

    private fun hideCommentsBtn(){
        binding.commentBtn.visibility = View.GONE
    }

    private fun changeLikeView() {
        val drawableRes = if (isLiked){
            R.drawable.ic_like_filled
        } else {
            R.drawable.ic_like
        }
        if(isLiked){
            binding.likeIv.setColorFilter(Color.parseColor(themeInner.minorErrorColor))
            binding.likeBtn.setCardBackgroundColor(Color.parseColor(themeInner.minorErrorSecondaryColor))
        }else{
            binding.likeIv.setColorFilter(Color.parseColor(themeInner.generalContrastSecondaryColor))
            binding.likeBtn.setCardBackgroundColor(Color.parseColor(themeInner.minorInfoSecondaryColor))
        }

        binding.likeIv.setImageResource(drawableRes)
    }

    fun setIsLiked(isLikedOuter: Boolean) {
        isLiked = isLikedOuter
        changeLikeView()
    }

    fun setLikesAmount(likesAmount: Int?) {
        _likesCounter = likesAmount ?: 0
        if(_likesCounter > 0){
            binding.likeTvCounter.text = _likesCounter.toString()
            binding.likeTvCounter.visibility = View.VISIBLE
        }else{
            binding.likeTvCounter.visibility = View.GONE
        }
    }

    fun setCommentsAmount(commentsAmount: Int?) {
        _commentsCounter = commentsAmount ?: 0
        if(_commentsCounter > 0){
            binding.commentTvCounter.text = _commentsCounter.toString()
            binding.commentTvCounter.visibility = View.VISIBLE
        }else{
            binding.commentTvCounter.visibility = View.GONE
        }
    }

}