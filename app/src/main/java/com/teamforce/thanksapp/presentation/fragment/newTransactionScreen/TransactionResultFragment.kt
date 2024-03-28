package com.teamforce.thanksapp.presentation.fragment.newTransactionScreen

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.allViews
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentTransactionResultBinding
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Consts.AMOUNT_THANKS
import com.teamforce.thanksapp.utils.Consts.AVATAR_USER
import com.teamforce.thanksapp.utils.Consts.RECEIVER_NAME
import com.teamforce.thanksapp.utils.Consts.RECEIVER_TG
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme

class TransactionResultFragment :
    BaseFragment<FragmentTransactionResultBinding>(FragmentTransactionResultBinding::inflate) {

    private var amountThanks: Int? = null
    private var receiverTg: String? = null
    private var receiverName: String? = null
    private var receiverPhoto: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            amountThanks = it.getInt(AMOUNT_THANKS)
            receiverTg = it.getString(RECEIVER_TG)
            receiverName = it.getString(RECEIVER_NAME)
            receiverPhoto = it.getString(AVATAR_USER)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInfo(receiverName, receiverTg)
        binding.receiverAvatar.setAvatarImageOrInitials(receiverPhoto, receiverName ?: receiverTg)

        binding.btnToTheBeginning.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_global_eventsFragment,
                null, OptionsTransaction().optionForResultOut
            )

        }
        binding.btnToTheHistory.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_global_history_graph,
                null,
                OptionsTransaction().optionForResultOut
            )
        }
        binding.btnRepeat.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setTextInfo(name: String?, tgName: String?){
        binding.amount.text = amountThanks.toString()
        if(name.isNotNullOrEmptyMy()){
            binding.receiverTgName.visible()
            binding.receiverNameLabelTv.text = name
            binding.receiverAvatar.avatarInitials = name
            binding.receiverTgName.text = tgName
        }else{
            binding.receiverNameLabelTv.text = tgName
            binding.receiverAvatar.avatarInitials = tgName
            binding.receiverTgName.invisible()

        }
    }

    override fun applyTheme() {
        binding.root.allViews.filter { it is Themable }.forEach {
            (it as Themable).setThemeColor(
                appTheme
            )
        }
    }
}