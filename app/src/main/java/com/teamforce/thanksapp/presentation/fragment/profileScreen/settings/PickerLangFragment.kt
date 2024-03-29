package com.teamforce.thanksapp.presentation.fragment.profileScreen.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.teamforce.thanksapp.databinding.FragmentPickerLangBinding
import com.teamforce.thanksapp.presentation.activity.MainActivity
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.viewmodel.profile.SettingsViewModel
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.Extensions.getCurrentSystemLang
import com.teamforce.thanksapp.utils.Locals.ContextUtils
import com.teamforce.thanksapp.utils.Locals.Lang
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.visible
import java.util.*


class PickerLangFragment :
    BaseFragment<FragmentPickerLangBinding>(FragmentPickerLangBinding::inflate) {

    private val viewModel: SettingsViewModel by activityViewModels()


    private var currentLang: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentLang = it.getString(Consts.CURRENT_LANGUAGE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (currentLang != null) {
            setCurrentLang(currentLangInner = currentLang!!)
        }
        binding.langRusLinear.setOnClickListener {
            updateLocale(Lang.RUSSIAN)
        }
        binding.langBritishLinear.setOnClickListener {
            updateLocale(Lang.ENGLISH)
        }
        binding.langSysLinear.setOnClickListener {
            updateLocale(Lang.SYSTEM, true)
        }

        viewModel.changeLangError.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
    }

    override fun applyTheme() {

    }

    private fun setCurrentLang(currentLangInner: String) {
        when (currentLangInner) {
            Lang.SYSTEM.lang -> {
                binding.indicateLangSysTv.visible()
                binding.indicateLangRusTv.invisible()
                binding.indicateLangBritishTv.invisible()
            }
            Lang.RUSSIAN.lang -> {
                binding.indicateLangRusTv.visible()

                binding.indicateLangSysTv.invisible()
                binding.indicateLangBritishTv.invisible()
            }
            Lang.ENGLISH.lang -> {
                binding.indicateLangBritishTv.visible()

                binding.indicateLangRusTv.invisible()
                binding.indicateLangSysTv.invisible()
            }
        }
    }

    private fun updateLocale(lang: Lang, likeInSystem: Boolean = false) {
        when (lang) {
            Lang.RUSSIAN, Lang.ENGLISH, Lang.DEFAULT -> {
                // TODO Ждать ответ сервера и только потом менять язык
                updateLocaleInProfileSettings(lang)
                currentLang = lang.lang
                ContextUtils.updateLocale(requireContext(), Locale(lang.lang), likeInSystem)
            }
            Lang.SYSTEM -> {
                val currSystemLang = getCurrentSystemLang()
                updateLocaleInProfileSettings(currSystemLang)
                currentLang = currSystemLang.lang
                ContextUtils.updateLocale(
                    requireContext(),
                    Locale(currSystemLang.lang),
                    likeInSystem
                )
            }
        }
        viewModel.changeLang.observe(viewLifecycleOwner){
            if(it == true){
                val refresh = Intent(requireContext(), MainActivity::class.java)
                requireActivity().finish()
                startActivity(refresh)
            }
        }
    }

    private fun updateLocaleInProfileSettings(lang: Lang) {
        viewModel.updateLangInProfileSettings(lang)
    }


    override fun onResume() {
        super.onResume()
        if (currentLang != null) {
            setCurrentLang(currentLangInner = currentLang!!)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        currentLang = savedInstanceState?.getString(Consts.CURRENT_LANGUAGE)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(Consts.CURRENT_LANGUAGE, currentLang)
    }


    companion object {

        private const val TAG = "PickerLangFragment"

        @JvmStatic
        fun newInstance(currentLang: String) =
            PickerLangFragment().apply {
                arguments = Bundle().apply {
                    putString(Consts.CURRENT_LANGUAGE, currentLang)
                }
            }
    }
}