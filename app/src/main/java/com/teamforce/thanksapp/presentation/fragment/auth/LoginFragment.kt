package com.teamforce.thanksapp.presentation.fragment.auth

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.response.AuthResponse
import com.teamforce.thanksapp.databinding.FragmentLoginBinding
import com.teamforce.thanksapp.presentation.activity.ILoginAction
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.MainFlowFragment.Companion.LOGIN_FRAGMENT_SOURCE
import com.teamforce.thanksapp.presentation.viewmodel.AuthorizationType
import com.teamforce.thanksapp.presentation.viewmodel.LoginViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme
import com.teamforce.thanksapp.utils.glide.setImage
import com.vk.auth.api.models.AuthResult
import com.vk.auth.main.VkClientAuthCallback
import com.vk.auth.main.VkClientAuthLib
import com.vk.superapp.bridges.LogoutReason
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(
    FragmentLoginBinding::inflate
), View.OnClickListener, ILoginAction {

    private var authResultInner: AuthResult? = null


    private val authCallback = object : VkClientAuthCallback {
        override fun onAuth(authResult: AuthResult) {
            if(authResultInner == null){
                authResultInner = authResult
                Log.e("LoginFragment", "Auth ${authResult}")
                viewModel.saveVkAccessToken(authResult.accessToken)
                viewModel.authThroughVk(authResult.accessToken, sharedKey)
            }else{
//                Toast.makeText(requireContext(), "Пытался отправить запрос еще раз", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onLogout(logoutReason: LogoutReason) {
            Log.e("LoginFragment", "Logout ${logoutReason}")
        }
    }

    private var sharedKey: String? = null
    private val viewModel: LoginViewModel by viewModels()

    private var dataBundle: Bundle? = null
    private var username: String? = null

    private var listOfOrg: MutableList<AuthResponse.Organization> = mutableListOf()
    private var checkedOrgId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VkClientAuthLib.addAuthCallback(authCallback)
        arguments?.let {
            sharedKey =
                requireActivity().intent?.data?.getQueryParameter(ARG_REF) ?: it.getString(ARG_REF)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.logout.setOnClickListener {
            VkClientAuthLib.logout()
        }
        // Сохранить id текущей орг сразу после создания
        if (viewModel.isUserAuthorized() && viewModel.isCurrentOrgNull() && sharedKey.isNullOrEmptyMy()) {
            viewModel.clearAuthToken()
        } else if (viewModel.isUserAuthorized() && !viewModel.isCurrentOrgNull() && sharedKey.isNullOrEmptyMy()) {
            findNavController().navigateSafely(R.id.action_loginFragment_to_mainFlowFragment)
        } else if(viewModel.isUserAuthorized() && sharedKey.isNotNullOrEmptyMy() && viewModel.getCurrentLogin().isNotNullOrEmptyMy()){
            val id = Settings.Secure.getString(
                requireContext().applicationContext.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            binding.telegramEt.setText(viewModel.getCurrentLogin()!!)
            viewModel.logout(id)
            username = binding.telegramEt.text.toString().trim()
            viewModel.saveCurrentLogin(username!!)
            viewModel.authorizeUser(username!!, sharedKey)
        }

        checkAuth()
        checkVerifyCode()
        listenersLoginEt()

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            arrayListOf("")
        )
        setData(adapter)

        binding.orgFilterSpinner.setAdapter(adapter)
        binding.orgFilterSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                checkedOrgId = listOfOrg[id.toInt()].organization_id
                if(username != null){
                    viewModel.chooseOrg(
                        login = username!!,
                        orgId = checkedOrgId,
                        userId = listOfOrg[id.toInt()].user_id
                    )
                }else if(authResultInner != null){
                    viewModel.chooseOrgThroughVk(
                        accessToken = authResultInner!!.accessToken,
                        orgId = checkedOrgId,
                        userId = listOfOrg[id.toInt()].user_id
                    )
                }
            }


        binding.apply {
            getCodeBtn.setOnClickListener(this@LoginFragment)

            helperText.setOnClickListener {
                dataBundle?.let { it1 -> setHelperText(it1) }
                setHelperLink()
            }
            codeEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (s?.trim()?.length == 4) {
                        when (viewModel.authorizationType) {
                            AuthorizationType.TELEGRAM -> {
                                viewModel.verifyCodeTelegram(
                                    orgId = checkedOrgId,
                                    codeFromTg = binding.codeEt.text?.trim().toString(),
                                    sharedKey = sharedKey
                                )
                            }

                            AuthorizationType.EMAIL -> {
                                Log.d("Token", "Я по почте захожу")
                                viewModel.verifyCodeEmail(
                                    orgId = checkedOrgId,
                                    codeFromTg = binding.codeEt.text?.trim().toString(),
                                    sharedKey = sharedKey
                                )
                            }

                            else -> {
                                Log.d("Token", "Ни один статус не прошел CheckCodeFragment OnClick")
                            }
                        }
                    }
                }

                override fun afterTextChanged(p0: Editable?) {}
            })

        }

        viewModel.internetError.observe(viewLifecycleOwner) {
            showSnackBarAboutNetworkProblem(view, requireContext())
        }

        viewModel.authVk.observe(viewLifecycleOwner){
            it?.let { response ->
                if(response.token != null){
                    finishLogin(response.token, "")
                }else if(response.organizations != null){

                }else{
                    Toast.makeText(requireContext(), response.errors, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun listenersLoginEt(){
        binding.getCodeBtn.isEnabled = false
        binding.telegramEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                s?.let {
                    binding.getCodeBtn.isEnabled = it.trim().isNotEmpty()
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun setData(adapter: ArrayAdapter<String>) {
        viewModel.organizations.observe(viewLifecycleOwner) {
            it?.let {
                adapter.clear()
                it.forEach { orgModel ->
                    adapter.add(orgModel.organization_name)
                }
                listOfOrg.addAll(it)
                binding.orgFilterContainer.visible()
                binding.orgFilterSpinner.visible()

            }
        }
    }

    private fun checkAuth() {
        viewModel.authResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
//                    binding.textField.error = "Пользователь не найден"
//                    binding.textField.isErrorEnabled = true
                }

                is Result.Success -> {
                    if (result.value && username != null) {
                        if (viewModel.needChooseOrg.value == false) {
                            binding.orgFilterSpinner.invisible()
                            binding.orgFilterContainer.invisible()
                            dataBundle = sendToastAboutVerifyCode()
                            binding.helperText.visible()
                            setEditTextCode()
                            hideGetCodeBtn()
                        } else {
                            binding.orgFilterSpinner.visible()
                            binding.orgFilterContainer.visible()
                            hideGetCodeBtn()
                        }
                    }
                }

                else -> {
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.getCodeBtn.isClickable = !it
        }
    }

    private fun checkVerifyCode() {
        viewModel.verifyResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Error -> {
                    Toast.makeText(
                        requireContext(),
                        String.format(getString(R.string.incorrect_code)), Toast.LENGTH_LONG
                    ).show()
                }

                is Result.Success -> {
                    if (binding.codeEt.text?.trim().toString().isNotEmpty()) {
                        Log.d("Token", "verifyResult in  CheckCode ${binding.codeEt.text}")
                        hideKeyboard()
                        finishLogin(result.value.authtoken, result.value.telegramOrEmail)
                    }
                }

                else -> {}

            }
        }
    }

    private fun hideKeyboard() {
        val view: View? = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun finishLogin(authToken: String?, telegramOrEmail: String?) {
        viewModel.userDataRepository.saveCredentials(authToken, telegramOrEmail, username)
        viewModel.loadUserProfile()
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            if (!profile.profile.organizationId.toString().isNullOrEmptyMy())
                goToMainFlowFragment()
            else
                findNavController().navigateSafely(
                    R.id.action_loginFragment_to_createCommunityFragment
                )
        }

    }

    private fun goToMainFlowFragment() {
        val bundle = Bundle().apply { putBoolean(LOGIN_FRAGMENT_SOURCE, true) }
        findNavController().navigateSafely(R.id.action_loginFragment_to_mainFlowFragment, bundle)

    }

    private fun hideGetCodeBtn() {
        binding.getCodeBtn.invisible()
        binding.tvPromptForLoginField.invisible()
    }

    private fun setEditTextCode() {
        binding.textFieldCode.visible()
        binding.codeEt.requestFocus()
    }

    private fun setHelperText(data: Bundle) {
        val helperTextView = binding.helperText
        if (data.getString(Consts.BUNDLE_TG_OR_EMAIL) == "1") {
            helperTextView.text =
                String.format(
                    getString(R.string.helperTextAboutEmail),
                    binding.telegramEt.text.toString()
                )
        } else {
            helperTextView.text = String.format(
                getString(R.string.helperTextAboutTg),
                data.getString(Consts.LINK_TO_BOT_Name, "null")
            )
        }
    }

    private fun setHelperLink() {
        val id = Settings.Secure.getString(
            requireContext().applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        binding.apply {
            helperLink.isClickable = true
            helperLink.visible()
            helperLink.setOnClickListener {
                viewModel.logout(id)
                helperLink.invisible()
                helperText.invisible()
                helperText.text = view?.context?.getString(R.string.helperTextStandard)
                    ?.let { it1 -> String.format(it1) }
                textFieldCode.invisible()
                telegramEt.text = null
                getCodeBtn.visible()
            }
        }
    }

    private fun sendToastAboutVerifyCode(): Bundle {
        val emailOrTelegram = Bundle()
        emailOrTelegram.putString(Consts.BUNDLE_TG_OR_EMAIL, binding.telegramEt.text.toString())
        if (viewModel.authorizationType == AuthorizationType.TELEGRAM) {
            Toast.makeText(
                requireContext(),
                R.string.Toast_verifyCode_hintTg,
                Toast.LENGTH_LONG
            ).show()
            emailOrTelegram.putString(Consts.BUNDLE_TG_OR_EMAIL, "0")
            emailOrTelegram.putString(Consts.LINK_TO_BOT_Name, Consts.LINK_TO_BOT)
        } else {
            Toast.makeText(
                requireContext(),
                R.string.Toast_verifyCode_hintEmail,
                Toast.LENGTH_LONG
            ).show()
            emailOrTelegram.putString(Consts.BUNDLE_TG_OR_EMAIL, "1")
            // emailOrTelegram.putString(Consts.BUNDLE_EMAIL, UserDataRepository.getInstance()?.email.toString())
        }
        return emailOrTelegram
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.get_code_btn) {
            username = binding.telegramEt.text.toString().trim()
            viewModel.saveCurrentLogin(username!!)
            viewModel.authorizeUser(binding.telegramEt.text?.trim().toString(), sharedKey)
        }
    }

    override fun showCheckCode() {
        TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        binding.telegramEt.text = null
        VkClientAuthLib.removeAuthCallback(authCallback)
        super.onDestroyView()
    }

    override fun applyTheme() {
        val gradientDrawable = ResourcesCompat.getDrawable(
            resources,
            R.drawable.background_for_status_bar,
            null
        ) as GradientDrawable
        gradientDrawable.apply {
            colors = intArrayOf(
                Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor),
                Color.parseColor(Branding.brand.colorsJson.mainBrandColor)
            )
        }

        binding.root.apply {
            background = gradientDrawable
        }
       // binding.helperText.setTextColor(Color.parseColor(appTheme.mainBrandColor))
        binding.helperLink.setTextColor(Color.parseColor(appTheme.mainBrandColor))
    }

    companion object {
        const val ARG_REF = "invite"
    }
}
