package com.teamforce.thanksapp.presentation.fragment.auth

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentChangeOrganizationBinding
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.viewmodel.AuthorizationType
import com.teamforce.thanksapp.presentation.viewmodel.auth.ChangeOrganizationViewModel
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.Result
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.showSnackBarAboutNetworkProblem
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChangeOrganizationFragment : BaseFragment<FragmentChangeOrganizationBinding>(FragmentChangeOrganizationBinding::inflate) {

    private var xCode: String? = null
    private var xId: String? = null
    private var xEmail: String? = null
    private var orgId: String? = null



    private val viewModel: ChangeOrganizationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.apply {
            xCode = getXcode()
            orgId = getOrgId()
            xId = getXId()
            xEmail = getXEmail()
            authorizationType = getAuthType()
        }
        viewModel.deleteCredentialsForChangeOrg()
        checkVerifyCode()

        binding.apply {
            codeEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (s?.trim()?.length == 4) {
                        when (viewModel.authorizationType) {
                            AuthorizationType.TELEGRAM.name -> {
                                if(xId != null && xCode != null){
                                    viewModel.changeOrgWithTelegram(
                                        telegramId = xId!!,
                                        xCode = xCode!!,
                                        orgCode = orgId!!,
                                        codeFromTg = binding.codeEt.text?.trim().toString()
                                    )
                                }

                            }
                            AuthorizationType.EMAIL.name -> {
                                Log.d("Token", "Я по почте захожу")
                                if(xEmail != null && xCode != null){
                                    viewModel.changeOrgWithEmail(
                                        emailId = xEmail!!,
                                        xCode = xCode!!,
                                        orgCode = orgId!!,
                                        codeFromEmail = binding.codeEt.text?.trim().toString()
                                    )
                                }
                            }
                            else -> {
                                Log.d("Token", "Ни один статус не прошел CheckCodeFragment OnClick")
//                                if(xId != null && xCode != null){
//                                    viewModel.changeOrgWithTelegram(
//                                        telegramId = xId!!,
//                                        xCode = xCode!!,
//                                        orgCode = orgId!!,
//                                        codeFromTg = binding.codeEt.text?.trim().toString()
//                                    )
//                                }
                            }
                        }
                    }
                }

                override fun afterTextChanged(p0: Editable?) {}
            })
        }

        viewModel.internetError.observe(viewLifecycleOwner){
            showSnackBarAboutNetworkProblem(view, requireContext())
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigateSafely(R.id.action_changeOrganizationFragment_to_profileGraph,
            null,
            OptionsTransaction().optionForBackChangeOrg)
        }

    }

    override fun applyTheme() {
        binding.mainViewLinear.backgroundTintList = ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
        binding.backBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.secondaryBrandColor))
        binding.backBtn.setTextColor(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
        binding.textFieldCode.setStartIconTintList(ColorStateList.valueOf(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor)))
    }

    private fun finishLogin(authToken: String?, telegramOrEmail: String?) {
        viewModel.loadUserProfile()
        viewModel.profile.observe(viewLifecycleOwner){
            if(saveDataToSharedPreferences(authToken, telegramOrEmail)) {
                refreshApplication()
            }
        }
//
//        CoroutineScope(Dispatchers.Main).launch {
//            val job =  CoroutineScope(Dispatchers.IO).launch { // launch a new coroutine and keep a reference to its Job
//                saveDataToSharedPreferences(authToken, telegramOrEmail)
//            }
//            println("Hello")
//            job.join()
//            refreshApplication()
//        }
    }

    private fun saveDataToSharedPreferences(authToken: String?, telegramOrEmail: String?): Boolean{
        return viewModel.saveDataForChangeOrg(authToken, telegramOrEmail)
    }

    private fun checkVerifyCode() {
        viewModel.verifyResult.observe(viewLifecycleOwner) { result ->
            when (result) {
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
        viewModel.verifyError.observe(viewLifecycleOwner){ result ->
            when(result) {
                is Result.Error -> {
                    Toast.makeText(
                        requireContext(),
                        String.format(getString(R.string.incorrect_code)), Toast.LENGTH_LONG
                    ).show()
                }
                else -> {}
            }
        }
    }

    private fun refreshApplication() {
        requireActivity().apply {
            finish()
            overridePendingTransition(0, 0)
            startActivity(requireActivity().intent)
            overridePendingTransition(0, 0)
        }

    }

    private fun hideKeyboard(){
        val view: View? = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    companion object {
        private const val XCODE = "xCode"
        private const val XID = "xId"
        private const val ORGID = "organization_id"


        @JvmStatic
        fun newInstance(xCode: String, xId: String, orgId: String) =
            ChangeOrganizationFragment().apply {
                arguments = Bundle().apply {
                    putString(XCODE, xCode)
                    putString(XID, xId)
                    putString(ORGID, orgId)
                }
            }
    }
}