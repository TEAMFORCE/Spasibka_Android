package com.teamforce.thanksapp.presentation.fragment.newTransactionScreen

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.allViews
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentFeedbackBinding
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme
import com.teamforce.thanksapp.utils.copyTextToClipboardViaIntent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedbackFragment : BaseFragment<FragmentFeedbackBinding>(FragmentFeedbackBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarTransaction.setupWithNavController(findNavController())

        binding.mailto.setOnClickListener {
            copyTextToClipboardViaIntent(
                binding.mailto.text.toString(),
                requireContext()
            )
        }
        binding.telegram.setOnClickListener {
            try {
                val telegram = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/${binding.telegram.text}"))
                telegram.setPackage("org.telegram.messenger")
                startActivity(telegram)
            } catch (e: Exception) {
                Toast.makeText(context, "Telegram app is not installed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun applyTheme() {
        binding.root.allViews.filter { it is Themable }.forEach {
            (it as Themable).setThemeColor(
                appTheme
            )
        }
        binding.toolbarTransaction.setBackgroundColor(Color.parseColor(appTheme.mainBrandColor))
    }
}
