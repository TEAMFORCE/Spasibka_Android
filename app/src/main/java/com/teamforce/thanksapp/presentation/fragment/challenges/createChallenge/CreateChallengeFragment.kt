package com.teamforce.thanksapp.presentation.fragment.challenges.createChallenge

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.teamforce.photopicker.PhotoPickerFragment
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentCreateChallengeBinding
import com.teamforce.thanksapp.domain.models.challenge.createChallenge.CreateChallengeSettingsModel
import com.teamforce.thanksapp.domain.models.challenge.createChallenge.DebitAccountModel
import com.teamforce.thanksapp.domain.models.templates.TemplateForBundleModel
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.customViews.photoSelectionView.PhotoSelectionListener
import com.teamforce.thanksapp.presentation.fragment.challenges.ChallengesConsts
import com.teamforce.thanksapp.presentation.fragment.challenges.createChallenge.SelectAccountsDialogFragment.Companion.SELECT_ACCOUNT_BUNDLE_KEY
import com.teamforce.thanksapp.presentation.fragment.challenges.createChallenge.SelectAccountsDialogFragment.Companion.SELECT_ACCOUNT_REQUEST_KEY
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.CustomDialogFragment
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.NegativeButtonClickListener
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.PositiveButtonClickListener
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.SettingsCustomDialogFragment
import com.teamforce.thanksapp.presentation.viewmodel.challenge.ChallengesViewModel
import com.teamforce.thanksapp.presentation.viewmodel.challenge.CreateChallengeViewModel
import com.teamforce.thanksapp.utils.EditTextValidator
import com.teamforce.thanksapp.utils.Extensions.observeOnce
import com.teamforce.thanksapp.utils.Extensions.showSnackBar
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.addLeadZero
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.getParcelableExt
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.showSnackBarAboutNetworkProblem
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@AndroidEntryPoint
class CreateChallengeFragment :
    BaseFragment<FragmentCreateChallengeBinding>(FragmentCreateChallengeBinding::inflate),
    PhotoPickerFragment.Callback {

    // reflection API and ViewBinding.bind are used under the hood
    private val viewModel: CreateChallengeViewModel by activityViewModels()
    private val challengeListViewModel: ChallengesViewModel by activityViewModels()
//    private val imageAdapter: ImageAdapter =
//        ImageAdapter(onCrossClicked = ::onCrossClicked, photos = mutableListOf())

    private var challengeWasCreated: Boolean = false
    var additionalSettingsHasBeenOpened: Boolean = false

    var dateStartForSendingFormatted: ZonedDateTime? = null
    var dateEndForSendingFormatted: ZonedDateTime? = null
    var dataAboutTemplate: TemplateForBundleModel? = null


    // TODO Получая настройки чалика с бека, мы не записываем их в настройки +
    //  не нужно каждый раз получать новые настройки чалика с бека, их нужно получать лишь 1 раз
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dataAboutTemplate = it.getParcelableExt(
                ChallengesConsts.CHALLENGE_TEMPLATE,
                TemplateForBundleModel::class.java
            )
        }

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.navContainer
            duration = 400.toLong()
            scrimColor = Color.WHITE
            setAllContainerColors(requireContext().getColor(R.color.general_background))
        }
        // TODO Deprecated API need to replace it with something else
        getActivity()?.getWindow()
            ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)


        setFragmentResultListener(SELECT_ACCOUNT_REQUEST_KEY) { requestKey, bundle ->
            val result =
                bundle.getParcelableExt(SELECT_ACCOUNT_BUNDLE_KEY, DebitAccountModel::class.java)
            result?.let {
                viewModel.updateChallengeSettings { currentSettings ->
                    // Обновить список, устанавливая current = true для выбранной модели и current = false для остальных
                    val updatedAccounts = currentSettings.accounts.map { account ->
                        account.copy(current = account == it)
                    }
                    currentSettings.copy(
                        accounts = updatedAccounts
                    )
                }
                setDataAboutAccountInMaterialCard(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        getActivity()?.getWindow()
            ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getCreateChallengeSettingsFromBack()
        binding.shimmerLayout.startShimmer()
        checkingForExternalDataToFill()
        handlingVisibilityOfStartDateField()
        binding.createBtn.setOnClickListener {
            createChallenge()
        }
        binding.createBtnSticky.setOnClickListener {
            createChallenge()
        }
        binding.tvAdditionalSettings.setOnClickListener {
            val bundle = Bundle()
            dataAboutTemplate?.id?.let { templateId ->
                bundle.putInt(
                    ChallengesConsts.CHALLENGE_TEMPLATE_ID,
                    templateId
                )
            }
            additionalSettingsHasBeenOpened = true
            findNavController().navigateSafely(
                R.id.action_global_settingsChallengeFragment,
                bundle,
                OptionsTransaction().optionForEditProfile
            )
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) binding.progressBar.visible()
            else binding.progressBar.invisible()
        }

        binding.closeBtn.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        viewModel.internetError.observe(viewLifecycleOwner) {
            if (it) showSnackBarAboutNetworkProblem(view, requireContext())
        }
        setEventListener(
            requireActivity(),
            viewLifecycleOwner,
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    if (isOpen) {
                        binding.createBtnSticky.visible()
                        binding.createBtn.invisible()
                    } else {
                        binding.createBtnSticky.invisible()
                        binding.createBtn.visible()
                    }
                }
            }
        )

        binding.startDateEt.setOnClickListener {
            callDatePicker(true)
        }

        binding.endDateEt.setOnClickListener {
            callDatePicker(false)
        }

        listenerTextViewDates()


        observeDataForAccountCard()

        initDragRecycler()
    }

    private fun checkingForExternalDataToFill() {
        if (dataAboutTemplate != null) {
            setFieldsBaseOnTemplateData()
        } else if (cacheIsNotEmpty() && !additionalSettingsHasBeenOpened) {
            suggestRestoreData()
        }
    }

    private fun suggestRestoreData() {
        val settingsDialog = SettingsCustomDialogFragment(
            positiveTextBtn = requireContext().getString(R.string.create_challenge_dialog_positive_btn),
            negativeTextBtn = requireContext().getString(R.string.create_challenge_dialog_negative_btn),
            title = requireContext().getString(R.string.create_challenge_dialog_title),
            subtitle = requireContext().getString(R.string.create_challenge_dialog_subtitle)
        )
        val dialogFragment = CustomDialogFragment.newInstance(settingsDialog)
        dialogFragment.setPositiveButtonClickListener(object : PositiveButtonClickListener {
            override fun onPositiveButtonClick() {
                restoreDataFromCache()
            }
        })

        dialogFragment.setNegativeButtonClickListener(object : NegativeButtonClickListener {
            override fun onNegativeButtonClick() {
                clearCache()
            }
        })
        dialogFragment.show(requireActivity().supportFragmentManager, "MyDialogFragment")
    }

    private fun restoreDataFromCache() {
        val sharedPref = requireContext().getSharedPreferences(SP_NAME, 0)
        sharedPref?.apply {
            binding.titleEt.setText(this.getString(NAME, ""))
            binding.descriptionEt.setText(this.getString(DESCRIPTION, ""))
            binding.prizePoolEt.setText(this.getString(PRIZE_POOL, ""))
            binding.prizeFundEt.setText(this.getString(AMOUNT_FUND, ""))
            // Restore Settings
            viewModel.updateChallengeSettings { currentSettings ->
                currentSettings.copy(
                    multipleReports = getBoolean(SEVERAL_REPORTS, true),
                    showContenders = getBoolean(SHOW_CONTENDERS, false),
                    challengeWithVoting = getBoolean(CHALLENGE_WITH_VOTING, false)
                )
            }
            dateEndForSendingFormatted = viewModel.parseZonedDateTime(getString(END_AT, null))
            dateStartForSendingFormatted = viewModel.parseZonedDateTime(getString(START_AT, null))
            if (dateEndForSendingFormatted != null) {
                binding.endDateEt.setText(formatDateFromZonedDateTime(dateEndForSendingFormatted!!))
            }
            if (dateStartForSendingFormatted != null) {
                binding.startDateEt.setText(formatDateFromZonedDateTime(dateStartForSendingFormatted!!))
                binding.checkboxDelayedLaunch.isChecked = true
            }
            val photo = getString(IMAGES_URI, null)
        }
    }

    private fun formatDateFromZonedDateTime(dateTime: ZonedDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        return formatter.format(dateTime)
    }

    private fun cacheIsNotEmpty(): Boolean {
        return requireContext().getSharedPreferences(SP_NAME, 0).run {
            contains(NAME) ||
                    contains(DESCRIPTION) ||
                    contains(AMOUNT_FUND) ||
                    contains(PRIZE_POOL) ||
                    contains(START_AT) ||
                    contains(END_AT) ||
                    contains(IMAGES_URI) ||
                    contains(TEMPLATE_ID) ||
                    contains(CHALLENGE_WITH_VOTING) ||
                    contains(SEVERAL_REPORTS) ||
                    contains(SHOW_CONTENDERS)
        }
    }

    private fun setDebitAccount(settingsChallenge: CreateChallengeSettingsModel) {
        showDebitCardView(settingsChallenge)
        val firstPersonalAccount =
            settingsChallenge.accounts.find { debitAcc -> debitAcc.current }
        firstPersonalAccount?.let { account -> setDataAboutAccountInMaterialCard(account) }
    }

    private fun setDataAboutAccountInMaterialCard(debitAccountModel: DebitAccountModel) {
        if (debitAccountModel.myAccount) {
            binding.tvAccountName.text = requireContext().getString(R.string.personalAccount)
        } else {
            binding.tvAccountName.text = debitAccountModel.organizationName
        }
        binding.tvAccountBalance.text = debitAccountModel.amount.toString()
    }

    private fun showDebitCardView(settingsChallenge: CreateChallengeSettingsModel) {
        if (settingsChallenge.accounts.isNotEmpty() && settingsChallenge.accounts.size > 1) {
            binding.accountCard.visible()
            binding.accountCard.setOnClickListener { view ->
                val bundle =
                    Bundle().apply { putParcelable(SELECT_ACCOUNT_BUNDLE_KEY, settingsChallenge) }
                findNavController().navigateSafely(
                    R.id.action_global_selectAccountsDialogFragment,
                    bundle
                )
            }
        }
    }

    private fun observeDataForAccountCard() {
        viewModel.challengeSettingsState.observeOnce(viewLifecycleOwner) {
            it?.let { settingsModelNonNullable ->
                setDebitAccount(settingsModelNonNullable)
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.invisible()
            }
        }
    }

    private fun setFieldsBaseOnTemplateData() {
        binding.titleEt.setText(dataAboutTemplate?.title)
        binding.descriptionEt.setText(dataAboutTemplate?.description)
        dataAboutTemplate?.let {
            viewModel.updateChallengeSettings { currentSettings ->
                currentSettings.copy(
                    challengeWithVoting = it.challengeWithVoting,
                    multipleReports = it.multipleReports,
                    showContenders = it.showContenders
                )
            }
        }
    }

    private fun handlingVisibilityOfStartDateField() {
        binding.checkboxDelayedLaunch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) binding.startDateTextField.visible()
            else binding.startDateTextField.invisible()
        }
    }


    private fun createChallenge() {
        val areFieldsValid: Boolean = EditTextValidator.areFieldsNotEmpty(
            binding.titleEt,
            binding.descriptionEt,
            binding.prizeFundEt,
            binding.prizePoolEt
        )

        if (areFieldsValid) {
            val sortedPhotos = binding.photoSelection.getPhotos()
            val nameChallenge = binding.titleEt.text?.trim().toString()
            val description = binding.descriptionEt.text?.trim().toString()
            val prizeFund = binding.prizeFundEt.text?.trim().toString().trim().toInt()
            val prizePool = binding.prizePoolEt.text?.trim().toString().trim().toInt()
            val parameterId = 2

            viewModel.createChallenge(
                name = nameChallenge,
                description = description,
                amountFund = prizeFund,
                startAt = if (enableDeferredStart()) dateStartForSendingFormatted else null,
                endAt = dateEndForSendingFormatted,
                parameter_id = parameterId,
                parameter_value = prizePool,
                templateId = dataAboutTemplate?.id,
                photos = sortedPhotos,
            )
            createChallengeObserver()
        } else {
            EditTextValidator.setErrorToEmptyFields(
                Pair(binding.titleTextField, binding.titleEt),
                Pair(binding.descriptionTextField, binding.descriptionEt),
                Pair(binding.prizeFundTextField, binding.prizeFundEt),
                Pair(binding.prizePoolTextField, binding.prizePoolEt),
                error = requireContext().getString(R.string.requiredField)
            )
        }
    }

    private fun createChallengeObserver() {
        viewModel.challengeSuccessfulCreated.observe(viewLifecycleOwner) {
            if (it) {
                binding.createBtn.isEnabled = true
                binding.createBtn.setBackgroundColor(
                    Color.parseColor(Branding.brand.colorsJson.mainBrandColor)
                )
                challengeWasCreated = true
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.challengeWasCreated),
                    Toast.LENGTH_LONG
                ).show()
                challengeListViewModel.setNeedUpdateListValue(true)
                clearCacheAndReturnBack()
            }
        }

        viewModel.createChallengeError.observe(viewLifecycleOwner) {
            try {
                showSnackBar(binding.scrollView, it, Snackbar.LENGTH_LONG)
                Log.e(TAG, it.toString())
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "CreateChallenge Error")
            }
        }
    }

    private fun callDatePicker(forStartDate: Boolean = false) {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendarConstraintBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(today))
        val title =
            if (forStartDate) requireContext().getString(R.string.create_challenge_start_date_picker_title)
            else requireContext().getString(R.string.create_challenge_end_date_picker_title)
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .setCalendarConstraints(calendarConstraintBuilder.build())
                .build()

        datePicker.show(childFragmentManager, "tag")

        datePicker.addOnPositiveButtonClickListener {
            callTimePicker(clickedDateLong = it, isStartPicker = forStartDate)
        }
    }

    private fun callTimePicker(
        clickedDateLong: Long,
        isStartPicker: Boolean,
    ) {
        val clickedDate = Calendar.getInstance().apply {
            timeInMillis = clickedDateLong
        }
        val dayOfMonth = clickedDate.get(Calendar.DAY_OF_MONTH)
        val month = clickedDate.get(Calendar.MONTH)
        val year = clickedDate.get(Calendar.YEAR)
        if (Calendar.getInstance().compareTo(clickedDate) == -1) {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(if (isStartPicker) 0 else 23)
                .setMinute(if (isStartPicker) 0 else 59)
                .build()
            timePicker.show(childFragmentManager, "tag")
            timePicker.addOnPositiveButtonClickListener {
                val hourOfDay: Int = timePicker.hour
                val minute: Int = timePicker.minute
                if (isStartPicker) {
                    saveStartDate(
                        day = dayOfMonth,
                        month = month,
                        year = year,
                        hourOfDay,
                        minute,
                        clickedDate
                    )
                } else {
                    saveEndDate(
                        day = dayOfMonth,
                        month = month,
                        year = year,
                        hourOfDay,
                        minute,
                        clickedDate
                    )
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.forbiddenSetAPastDate),
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    private fun saveStartDate(
        day: Int,
        month: Int,
        year: Int,
        hourOfDay: Int,
        minute: Int,
        clickedDate: Calendar,
    ) {
        val dateStartForTextView = day.addLeadZero() +
                ".${(month + 1).addLeadZero()}" +
                ".${year}"
        val instant: Instant = clickedDate.time.toInstant()
        dateStartForSendingFormatted = instant.atZone(ZoneId.systemDefault()).withYear(year)
            .withMonth(month + 1)
            .withDayOfMonth(day)
            .withHour(hourOfDay)
            .withMinute(minute)
            .withSecond(0)
        Toast.makeText(requireContext(), "$dateStartForSendingFormatted", Toast.LENGTH_LONG).show()
        val dateTimeEndForTextView =
            "$dateStartForTextView ${hourOfDay.addLeadZero()}:${minute.addLeadZero()}"
        binding.startDateEt.setText(dateTimeEndForTextView)
    }

    private fun saveEndDate(
        day: Int,
        month: Int,
        year: Int,
        hourOfDay: Int,
        minute: Int,
        clickedDate: Calendar,
    ) {
        val dateEndForTextView = day.addLeadZero() +
                ".${(month + 1).addLeadZero()}" +
                ".${year}"
        val instant: Instant = clickedDate.time.toInstant()
        dateEndForSendingFormatted = instant.atZone(ZoneId.systemDefault()).withYear(year)
            .withMonth(month + 1)
            .withDayOfMonth(day)
            .withHour(hourOfDay)
            .withMinute(minute)
            .withSecond(0)
        Toast.makeText(requireContext(), "$dateEndForSendingFormatted", Toast.LENGTH_LONG).show()
        val dateTimeEndForTextView =
            "$dateEndForTextView ${hourOfDay.addLeadZero()}:${minute.addLeadZero()}"
        binding.endDateEt.setText(dateTimeEndForTextView)
    }

    private fun listenerTextViewDates() {
        binding.endDateEt.doAfterTextChanged {
            if (!checkCorrectnessOfDates(
                    dateStartForSendingFormatted,
                    dateEndForSendingFormatted
                )
            ) {
                dateEndForSendingFormatted = null
                it?.clear()
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.missEndDate),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
        binding.startDateEt.doAfterTextChanged {
            if (!checkCorrectnessOfDates(
                    dateStartForSendingFormatted,
                    dateEndForSendingFormatted
                )
            ) {
                dateStartForSendingFormatted = null
                it?.clear()
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.missStartDate),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
    }

    private fun checkCorrectnessOfDates(
        startDate: ZonedDateTime?,
        endDate: ZonedDateTime?,
    ): Boolean {
        if (startDate == null || endDate == null) return true
        return startDate.isBefore(endDate.minusHours(1))
    }


    private fun enableDeferredStart() = binding.checkboxDelayedLaunch.isChecked

    override fun onStop() {
        super.onStop()
        if (!challengeWasCreated) {
            saveChallengeDataToCache()
        }
    }

    private fun saveChallengeDataToCache() {
        if (thereIsWhatMustBeSaved()) {
            val sharedPref =
                requireContext().getSharedPreferences(SP_NAME, 0)
            val editor = sharedPref.edit()
            editor.putString(NAME, binding.titleEt.text?.trim().toString())
            editor.putString(DESCRIPTION, binding.descriptionEt.text?.trim().toString())
            binding.prizeFundEt.text.toString().let { editor.putString(AMOUNT_FUND, it) }
            binding.prizePoolEt.text.toString().let { editor.putString(PRIZE_POOL, it) }
            dataAboutTemplate?.id?.let { editor.putInt(TEMPLATE_ID, it) }
            if (enableDeferredStart()) {
                dateStartForSendingFormatted?.let { editor.putString(START_AT, it.toString()) }
            } else {
                editor.putString(START_AT, null)
            }
            dateEndForSendingFormatted?.let { editor.putString(END_AT, it.toString()) }
            // TODO Сохранение картинок в кеш для восстановления
            //editor.putString(IMAGE_URI, imageUri.toString())
            viewModel.getChallengeSettings().apply {
                editor.putBoolean(CHALLENGE_WITH_VOTING, challengeWithVoting)
                editor.putBoolean(SEVERAL_REPORTS, multipleReports)
                editor.putBoolean(SHOW_CONTENDERS, showContenders)
            }
            editor.apply()
        } else {
            clearCache()
        }
    }

    private fun clearCache() {
        requireContext().getSharedPreferences(SP_NAME, 0).edit().clear().apply()
    }

    private fun clearCacheAndReturnBack() {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                clearCache()
            }
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    // TODO Добавить проверку на выбранные картинки, существуют ли они
    private fun thereIsWhatMustBeSaved() = binding.titleEt.text?.trim()?.isNotEmpty() == true ||
            binding.descriptionEt.text?.trim()?.isNotEmpty() == true ||
            dateEndForSendingFormatted != null ||
            dateStartForSendingFormatted != null


    private fun initDragRecycler() {
        dataAboutTemplate?.photos?.let { binding.photoSelection.updateRecyclerViewStringList(it) }

        binding.photoSelection.photoSelectionListener = object : PhotoSelectionListener {
            override fun onAttachImageClicked(maxSelection: Int, alreadySelected: Int) {
                openPicker(maxSelection, alreadySelected)
            }
        }
    }

    override fun onImagesPicked(photos: ArrayList<Uri>) {
        binding.photoSelection.updateRecyclerView(photos)
    }


    private fun openPicker(maxSelection: Int, alreadySelected: Int) {
        PhotoPickerFragment.newInstance(
            multiple = true,
            allowCamera = true,
            maxSelection = maxSelection,
            alreadySelected = alreadySelected,
            mainColor = Branding.brand.colorsJson.mainBrandColor
        ).show(childFragmentManager, "photoPicker")
    }


    companion object {
        const val TAG = "Create Challenge Fragment"
        const val SP_NAME = "challengeCache"
        private const val NAME = "name"
        private const val DESCRIPTION = "description"
        private const val AMOUNT_FUND = "amountFund"
        private const val PRIZE_POOL = "prizePool"
        private const val START_AT = "startAt"
        private const val END_AT = "endAt"
        private const val IMAGES_URI = "imagesUri"
        private const val TEMPLATE_ID = "templateId"
        private const val CHALLENGE_WITH_VOTING = "challengeWithVoting"
        private const val SEVERAL_REPORTS = "severalReports"
        private const val SHOW_CONTENDERS = "showContenders"
    }

    override fun applyTheme() {
    }
}
