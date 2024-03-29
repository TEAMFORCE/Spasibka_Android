package com.teamforce.thanksapp.presentation.fragment.profileScreen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.allViews
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.Hold
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentEditProfileBottomSheetBinding
import com.teamforce.thanksapp.domain.models.profile.ContactModel
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.utils.DateUtils
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view.datePicker.DatePickerIos
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view.popup.DatePickerIosPopup
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.presentation.viewmodel.profile.EditProfileViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Extensions.observeOnce
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme
import com.vk.auth.api.models.AuthResult
import com.vk.auth.main.VkClientAuthCallback
import com.vk.auth.main.VkClientAuthLib
import com.vk.superapp.bridges.LogoutReason
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditProfileFragment : BaseFragment<FragmentEditProfileBottomSheetBinding>(FragmentEditProfileBottomSheetBinding::inflate) {

    private var authResultInner: AuthResult? = null

    private val authCallback = object : VkClientAuthCallback {
        override fun onAuth(authResult: AuthResult) {
            if (authResultInner == null) {
                authResultInner = authResult
                Log.e("SettingsFragment", "Auth ${authResult}")
                viewModel.authThroughVk(authResult.accessToken)
            } else {
//                Toast.makeText(requireContext(), "Пытался отправить запрос еще раз", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onLogout(logoutReason: LogoutReason) {
            Log.e("LoginFragment", "Logout ${logoutReason}")
        }
    }

    private val viewModel: EditProfileViewModel by activityViewModels()

    private var profile: ProfileModel? = null


    private var emailContact: ContactModel? = null
    private var phoneContact: ContactModel? = null

    // TODO Телефон при вставке не в том формате


    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                addPhotoFromIntent(useGallery = true, useCamera = true)
            } else {
                showDialogAboutPermissions()
            }
        }

    private val resultLauncher =
        registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful && result.uriContent != null) {
                val pathCroppedPhoto = result.getUriFilePath(requireContext())
                val pathOrigPhoto =
                    result.originalUri?.let { getFilePathFromUri(requireContext(), it, true) }
                Log.d("Token", "OrigPhoto - ${pathOrigPhoto}")
                Log.d("Token", "CroppedPhoto - ${pathCroppedPhoto}")
                val imageUri = result.uriContent
                if (imageUri != null && pathCroppedPhoto != null && pathOrigPhoto != null) {
                    uriToMultipart(imageUri, pathOrigPhoto, pathCroppedPhoto)
                }

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            profile = it.getParcelableExt(Consts.PROFILE_DATA, ProfileModel::class.java)
        }
        VkClientAuthLib.addAuthCallback(authCallback)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        binding.root.post{
            startPostponedEnterTransition()
        }
        handleTopAppbar()
        writeData()
        listenersEventType()
        listenerErrors()
        logicalSaveData()
        binding.userAvatar.setOnClickListener {
            showRequirePermission()
        }

        viewModel.isLoadingUpdateAvatar.observe(viewLifecycleOwner) {
            if (it) Toast.makeText(
                requireContext(),
                resources.getString(R.string.edit_profile_downloadingANewAvatar),
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.isSuccessfulOperation.observe(viewLifecycleOwner) {
            if (it.result) Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            else Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
        }

        callDatePickerListener()

        viewModel.authVk.observe(viewLifecycleOwner) {
            it?.let { response ->
                if (response.details != null && response.status == 0) {
                    Toast.makeText(requireContext(), response.details, Toast.LENGTH_SHORT).show()
                    binding.authVkButton.visibility = View.GONE
                } else {
                    Toast.makeText(requireContext(), response.errors, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDialogAboutPermissions() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(resources.getString(R.string.explainingAboutPermissions))

            .setNegativeButton(resources.getString(R.string.close)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(resources.getString(R.string.settings)) { dialog, which ->
                dialog.cancel()
                val reqIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .apply {
                        val uri = Uri.fromParts("package", "com.teamforce.thanksapp", null)
                        data = uri
                    }
                startActivity(reqIntent)
                // Почему то повторно не запрашивается разрешение
                // requestPermissions()
            }
            .show()
    }

    private fun showRequirePermission() {

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                addPhotoFromIntent(useGallery = true, useCamera = true)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showRequestPermissionRational()
            }
            else -> {
                requestMultiplePermissionsLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }


    private fun addPhotoFromIntent(useGallery: Boolean, useCamera: Boolean) {
        val pickIntent = Intent(Intent.ACTION_GET_CONTENT)
        pickIntent.type = "image/*"
        resultLauncher.launch(
            CropImageContractOptions(
                pickIntent.data, CropImageOptions(
                    imageSourceIncludeGallery = useGallery,
                    imageSourceIncludeCamera = useCamera,
                    guidelines = CropImageView.Guidelines.ON,
                    backgroundColor = requireContext().getColor(R.color.general_contrast),
                    activityBackgroundColor = requireContext().getColor(R.color.general_contrast)
                    // TODO: Затестить овал, поправить вылет при обрезке с камеры
                )
            )
        )
    }


    private fun uriToMultipart(imageURI: Uri, filePath: String, filePathCropped: String) {
        Glide.with(this)
            .load(imageURI)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.userAvatar)
        profile?.profile?.idForEdit?.let {
            viewModel.loadUpdateAvatarUserProfile(profile?.profile?.idForEdit!!,filePath, filePathCropped)
        }
    }

    private fun showRequestPermissionRational() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(resources.getString(R.string.explainingAboutPermissionsRational))

            .setNegativeButton(resources.getString(R.string.close)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(resources.getString(R.string.good)) { dialog, _ ->
                requestMultiplePermissionsLauncher.launch(Manifest.permission.CAMERA)
                dialog.cancel()
            }
            .show()
    }



    private fun writeData() {
        profile?.let {
            setBaseData(it)
            setBirthday(it)
            setContacts(it)
            if (!it.profile.photo.isNullOrEmpty()) {
                Glide.with(this)
                    .load("${Consts.BASE_URL}${it.profile.photo}".toUri())
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.userAvatar)
            }
        }
        viewModel.gender.observe(viewLifecycleOwner){
            setGender(it)
        }
        setOnClickListenerForGenderEt()

    }

    private fun setOnClickListenerForGenderEt(){
        binding.genderEt.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_global_chooseGenderDialogFragment
            )
        }
    }

    private fun setGender(gender: String?){
        when(gender){
            "M" -> binding.genderEt.setText(requireContext().getString(R.string.edit_profile_male))
            "W" -> binding.genderEt.setText(requireContext().getString(R.string.edit_profile_female))
            else -> {}
        }
    }

    private fun setBaseData(it: ProfileModel){
        binding.surnameEt.setText(it.profile.surname)
        binding.firstEt.setText(it.profile.firstname)
        binding.middleEt.setText(it.profile.middleName)
        binding.birthdayEt.setText(it.profile.birthday)
        viewModel.setGender(it.profile.gender)
    }

    private fun setBirthday(it: ProfileModel){
        it.profile.birthday?.let { birthday -> viewModel.setBirthday(birthday) }
        binding.hideYearCheckbox.isChecked = !it.profile.showYearOfBirth
    }

    private fun setContacts(it: ProfileModel){
        if (it.profile.contacts.size == 1) {
            if (it.profile.contacts[0].contactType == "@") {
                binding.emailEt.setText(it.profile.contacts[0].contactId)
                emailContact = it.profile.contacts[0]
                phoneContact = ContactModel(null, "P", "")
            } else {
                binding.phoneEt.setText(it.profile.contacts[0].contactId)
                phoneContact = it.profile.contacts[0]
                emailContact = ContactModel(null, "@", "")

            }
        } else if (it.profile.contacts.size == 2) {
            if (it.profile.contacts[0].contactType == "@") {
                binding.emailEt.setText(it.profile.contacts[0].contactId)
                binding.phoneEt.setText(it.profile.contacts[1].contactId)
                emailContact = it.profile.contacts[0]
                phoneContact = it.profile.contacts[1]
            } else {
                binding.emailEt.setText(it.profile.contacts[1].contactId)
                binding.phoneEt.setText(it.profile.contacts[0].contactId)
                emailContact = it.profile.contacts[1]
                phoneContact = it.profile.contacts[0]
            }
        } else {
            phoneContact = ContactModel(null, "P", "")
            emailContact = ContactModel(null, "@", "")
        }
    }

    private fun listenersEventType() {
        binding.phoneEt.addTextChangedListener(object : TextWatcher {
            private var mSelfChange = false

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s == null || mSelfChange) {
                    return
                }
                // TODO Ввод номера телефона.
                //  Первый клик не вводит нажатую клавишу, а вводит только +7, исправить быстро не вышло
                //  Нужно разобраться
                val preparedStr = s.replace(Regex("(\\D*)"), "")
                var resultStr = ""
                for (i in preparedStr.indices) {
                    resultStr = when (i) {
                        0 -> resultStr.plus("+7")
                        1 -> resultStr.plus(" (".plus(preparedStr[i]))
                        2 -> resultStr.plus(preparedStr[i])
                        3 -> resultStr.plus(preparedStr[i])
                        4 -> resultStr.plus(") ".plus(preparedStr[i]))
                        5 -> resultStr.plus(preparedStr[i])
                        6 -> resultStr.plus(preparedStr[i])
                        7 -> resultStr.plus("-".plus(preparedStr[i]))
                        8 -> resultStr.plus(preparedStr[i])
                        9 -> resultStr.plus("-".plus(preparedStr[i]))
                        10 -> resultStr.plus(preparedStr[i])
                        else -> ""
                    }
                }

                mSelfChange = true
                val oldSelectionPos = binding.phoneEt.selectionStart
                val isEdit = binding.phoneEt.selectionStart != binding.phoneEt.length()
                binding.phoneEt.setText(resultStr)
                if (isEdit) {
                    binding.phoneEt.setSelection(if (oldSelectionPos > resultStr.length) resultStr.length else oldSelectionPos)
                } else {
                    binding.phoneEt.setSelection(resultStr.length)
                }
                mSelfChange = false
                val str = s.toString().filter { it.isDigit() }
                if (str.length != 11 && str.length != 0) {
                    binding.textInputPhone.isErrorEnabled = true
                    binding.textInputPhone.error = resources.getString(R.string.edit_profile_invalidNumber)
                    logicalSaveData()
                } else {
                    binding.textInputPhone.isErrorEnabled = false
                    logicalSaveData()

                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        // binding.phoneEt.addTextChangedListener(PhoneNumberFormattingTextWatcher())


        binding.emailEt.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length?.compareTo(0) != 0 && !s.toString().isValidEmail()) {
                    binding.textInputEmail.isErrorEnabled = true
                    binding.textInputEmail.error = resources.getString(R.string.edit_profile_invalidEmail)
                    logicalSaveData()
                } else {
                    binding.textInputEmail.isErrorEnabled = false
                    logicalSaveData()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }


    private fun listenerErrors() {
        viewModel.profileError.observe(viewLifecycleOwner) {
            val snack = Snackbar.make(
                requireView(),
                it,
                Snackbar.LENGTH_LONG
            )
            snack.setTextMaxLines(3)
                .setTextColor(context?.getColor(R.color.white)!!)
                .setAction(context?.getString(R.string.OK)!!) {
                    snack.dismiss()
                }
            snack.show()
        }

    }

    private fun callDatePickerListener() {
        val datePickerPopup = DatePickerIosPopup.Builder()
            .from(requireContext())
            .offset(3)
            .darkModeEnabled(true)
            .pickerMode(DatePickerIos.DAY_ON_FIRST)
            .textSize(40)
            .endDate(DateUtils.currentTime)
            .currentDate(DateUtils.currentTime)
            .startDate(DateUtils.getTimeMiles(1900, 0, 1))
            .listener(object : DatePickerIosPopup.OnDateSelectListener {

                override fun onDateSelected(
                    dp: DatePickerIos,
                    date: Long,
                    day: Int,
                    month: Int,
                    year: Int
                ) {
                    viewModel.setBirthday("$year-$month-$day")
                }
            })
            .build()

        binding.birthdayEt.setOnClickListener {
            datePickerPopup.show()
        }

        viewModel.birthday.observe(viewLifecycleOwner){
            if (binding.hideYearCheckbox.isChecked){
                binding.birthdayEt.setText(parseDateOutputOnlyDateWithoutYear(it, requireContext()))
            }else{
                binding.birthdayEt.setText(parseDateOutputOnlyDateWithYear(it, requireContext()))
            }
        }

        binding.hideYearCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                binding.birthdayEt.setText(parseDateOutputOnlyDateWithoutYear(viewModel.birthday.value, requireContext()))
            }else{
                binding.birthdayEt.setText(parseDateOutputOnlyDateWithYear(viewModel.birthday.value, requireContext()))
            }
        }
    }

    private fun logicalSaveData() {

        var surname: String? = ""
        var firstName: String? = ""
        var middleName: String? = ""

        if (binding.textInputEmail.isErrorEnabled || binding.textInputPhone.isErrorEnabled) {
            if (appTheme.secondaryBrandColor != null)
                binding.btnSaveChanges.setBackgroundColor(Color.parseColor(appTheme.secondaryBrandColor))
            else
                binding.btnSaveChanges.setBackgroundColor(requireContext().getColor(R.color.general_brand_secondary))

            if (binding.textInputEmail.isErrorEnabled || binding.textInputPhone.isErrorEnabled) {
                binding.btnSaveChanges.setBackgroundColor(requireContext().getColor(R.color.general_brand_secondary))
                binding.btnSaveChanges.setOnClickListener {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.edit_profile_fieldsMustBeFilledInCorrectly),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                binding.btnSaveChanges.setBackgroundColor(requireContext().getColor(R.color.general_brand))
            }


        }else{
            if(appTheme.mainBrandColor != null)
                binding.btnSaveChanges.setBackgroundColor(Color.parseColor(appTheme.mainBrandColor))
            else
                binding.btnSaveChanges.setBackgroundColor(requireContext().getColor(R.color.general_brand))
            binding.btnSaveChanges.setOnClickListener {

                firstName = binding.firstEt.text?.trim().toString()
                surname = binding.surnameEt.text?.trim().toString()
                middleName = binding.middleEt.text?.trim().toString()
                if(profile?.profile?.idForEdit != null){
                    viewModel.loadUpdateProfile(
                        userId = profile?.profile?.idForEdit!!,
                        firstName = firstName,
                        surname = surname,
                        middleName = middleName,
                        tgName = null,
                        nickname = null,
                        status = null,
                        formatOfWork = null,
                        birthDay = viewModel.birthday.value,
                        showYearOfBirth = !binding.hideYearCheckbox.isChecked,
                        gender = viewModel.gender.value
                    )
                }
                val listContact: MutableList<ContactModel> = mutableListOf()
                emailContact?.contactId = binding.emailEt.text.toString()
                phoneContact?.contactId = binding.phoneEt.text.toString().filter { it.isDigit() }
                Log.d("Errori", "${binding.emailEt.text.toString()}")
                Log.d("Errori", "${emailContact?.contactId}")
                emailContact?.let {
                    if (it.id == null && it.contactId == "") {
                    } else listContact.add(it)

                }
                phoneContact?.let {
                    if (it.id == null && it.contactId == "") {
                    } else listContact.add(it)
                }

                viewModel.loadUpdateFewContact(listContact)


            }

            viewModel.updateProfile.observe(viewLifecycleOwner){
                if (it != null){
                    activity?.onBackPressedDispatcher?.onBackPressed()
                    viewModel.resetState()
                }
            }
        }

    }

    private fun handleTopAppbar(){
        binding.header.toolbar.title = requireContext().getString(R.string.edit_profile_title)
        binding.header.filterIv.invisible()
        binding.header.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.scroll.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val currentElevation = binding.header.toolbar.translationZ
            val targetElevation = if (scrollY > 20.dp) {
                2.dp.toFloat()
            } else {
                0.toFloat()
            }

            if (currentElevation != targetElevation) {
                binding.header.toolbar.translationZ = targetElevation
            }
        }
    }

    companion object {
        const val TAG = "EditProfileFragment"
        const val TRANSITION_NAME = "user_avatar"
    }

    override fun applyTheme() {

    }
}