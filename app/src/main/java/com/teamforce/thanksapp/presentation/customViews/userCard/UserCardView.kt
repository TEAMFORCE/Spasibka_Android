package com.teamforce.thanksapp.presentation.customViews.userCard

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.google.android.material.card.MaterialCardView
import com.teamforce.thanksapp.databinding.UserCardViewBinding
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.utils.*


class UserCardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr), Themable {

    private var _binding: UserCardViewBinding? = null
    private val binding get() = checkNotNull(_binding) { "Binding is null" }

    var onUserClick: (() -> Unit)? = null

    init {
        _binding = UserCardViewBinding.inflate(LayoutInflater.from(context), this, true)
        setListeners()
    }

    private fun setListeners(){
        binding.userItem.setOnClickListener {
            onUserClick?.invoke()
        }
    }


    fun setUserData(userName: String?, userTgName: String?) {
        val showFirstLine = !userName?.trim().isNullOrEmpty()
        val showSecondLine = !userTgName.isNullOrEmpty()

        if (showFirstLine) {
            binding.userNameLabelTv.visible()
            binding.userNameLabelTv.text = userName
        } else {
            binding.userNameLabelTv.invisible()
        }

        if (showSecondLine) {
            binding.userTgName.visible()
            binding.userTgName.text = userTgName?.username()
        } else {
            binding.userTgName.invisible()
        }

    }

    fun setUserImage(image: String?, name: String?, tgName: String?){
        val initials = if(name.isNullOrEmpty()) tgName else name
        binding.userAvatar.setAvatarImageOrInitials(image, initials)
    }



    override fun setThemeColor(theme: ColorsModel) {
            binding.userNameLabelTv.setTextColor(Color.parseColor(theme.generalContrastColor))
            binding.userTgName.setTextColor(Color.parseColor(theme.minorSuccessColor))
            binding.userAvatar.avatarInitialsBackgroundGradientColorStart = Color.parseColor(theme.mainBrandColor)
            binding.userAvatar.avatarInitialsBackgroundGradientColorEnd = Color.parseColor(theme.secondaryBrandColor)
    }


}
