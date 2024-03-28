package com.teamforce.thanksapp.presentation.fragment.profileScreen.settings

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.view.allViews
import com.teamforce.thanksapp.BuildConfig
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentInfoAppBinding
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme
import com.teamforce.thanksapp.utils.*
import java.net.URI


class InfoAppFragment : BaseFragment<FragmentInfoAppBinding>(FragmentInfoAppBinding::inflate) {


    private var versionOfOS: String? = null
    private var modelPhone: String? = null
    private var versionOfApp: String? = null
    private var domen: String? = null
    private var region: String? = null
    private var licenseEnd: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            licenseEnd = it.getString(LICENSE_END)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        getInfo()
        setInfo()
    }

    private fun handleTopAppBar(){
        binding.header.toolbar.title = requireContext().getString(R.string.aboutApp)
        binding.header.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun getInfo() {
        //android.os.Build.VERSION.SDK_IN
        // android.os.Build.VERSION_CODES.LOLLIPOP
        versionOfOS = Build.VERSION.RELEASE
        modelPhone = getDeviceName()
        versionOfApp = BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE
        domen = getDomainName(BuildConfig.URL_PORT)
        region = "Россия"
    }

    private fun setInfo() {
        with(binding) {
            verAppValue.text = versionOfApp
            verOsValue.text =
                getNameOfAndroidVersion(
                    versionOfOS ?: requireContext().getString(R.string.undefined)
                )
            modelPhoneValue.text = modelPhone
            domenValue.text = domen
            regionValue.text = region
            setLicenseEnd()
        }
    }

    private fun setLicenseEnd(){
        if(licenseEnd == null)  binding.licenseValue.invisible()
        else {
            binding.licenseValue.visible()
            binding.licenseValue.text = requireContext().getString(R.string.licenseEnd, parseDate(licenseEnd, requireContext()))
        }
    }

    private fun getNameOfAndroidVersion(versionOfAndroid: String): String {
        return when (versionOfAndroid) {
            "6" -> "Android ${versionOfAndroid} Marshmallow"
            "7" -> "Android ${versionOfAndroid} Nougat"
            "8" -> "Android ${versionOfAndroid} Oreo"
            "9" -> "Android ${versionOfAndroid} Pie"
            "10" -> "Android ${versionOfAndroid} Queen Cake"
            "11" -> "Android ${versionOfAndroid} Red Velvet Cake"
            "12" -> "Android ${versionOfAndroid} Snow Cone"
            "13" -> "Android ${versionOfAndroid} Tiramisu"
            else -> versionOfAndroid
        }
    }

    fun getDomainName(url: String?): String {
        val uri = URI(url)
        val domain: String = uri.getHost()
        return if (domain.startsWith("www.")) domain.substring(4) else domain
    }

    /** Returns the consumer friendly device name  */
    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else capitalize(manufacturer) + " " + model
    }

    private fun capitalize(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true
        val phrase = StringBuilder()
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(c.uppercaseChar())
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase.append(c)
        }
        return phrase.toString()
    }


    companion object {
        const val TAG = "InfoAppFragment"
        const val LICENSE_END = "licenseEnd"

    }

    override fun applyTheme() {
        binding.root.apply {
            allViews.filter { it is Themable }.forEach {
                (it as Themable).setThemeColor(
                    appTheme
                )
            }
        }
    }
}