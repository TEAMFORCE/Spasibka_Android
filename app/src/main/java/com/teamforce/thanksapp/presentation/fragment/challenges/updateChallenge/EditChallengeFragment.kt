package com.teamforce.thanksapp.presentation.fragment.challenges.updateChallenge

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.teamforce.photopicker.PhotoPickerFragment
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.api.ImageFileData
import com.teamforce.thanksapp.data.api.toIndexedList
import com.teamforce.thanksapp.databinding.FragmentEditChallengeBinding
import com.teamforce.thanksapp.domain.models.challenge.ChallengeCondition
import com.teamforce.thanksapp.domain.models.challenge.createChallenge.CreateChallengeSettingsModel
import com.teamforce.thanksapp.domain.models.challenge.createChallenge.DebitAccountModel
import com.teamforce.thanksapp.domain.models.challenge.updateChallenge.UpdateChallengeModel
import com.teamforce.thanksapp.model.domain.ChallengeModelById
import com.teamforce.thanksapp.presentation.adapter.transactions.AdapterItem
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.presentation.customViews.photoSelectionView.PhotoSelectionListener
import com.teamforce.thanksapp.presentation.customViews.photoSelectionView.toItems
import com.teamforce.thanksapp.presentation.fragment.challenges.createChallenge.SelectAccountsDialogFragment.Companion.SELECT_ACCOUNT_BUNDLE_KEY
import com.teamforce.thanksapp.presentation.fragment.challenges.createChallenge.SelectAccountsDialogFragment.Companion.SELECT_ACCOUNT_REQUEST_KEY
import com.teamforce.thanksapp.presentation.fragment.challenges.createChallenge.SettingsChallengeFragment
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.CustomDialogFragment
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.PositiveButtonClickListener
import com.teamforce.thanksapp.presentation.fragment.dialogFragments.SettingsCustomDialogFragment
import com.teamforce.thanksapp.presentation.viewmodel.challenge.EditChallengeViewModel
import com.teamforce.thanksapp.utils.Extensions.toMultipart
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.addLeadZero
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.getFilePathFromUri
import com.teamforce.thanksapp.utils.getParcelableExt
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.isNullOrEmptyMy
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.parceleable
import com.teamforce.thanksapp.utils.parseDateTimeWithBindToTimeZone
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar

@AndroidEntryPoint
class EditChallengeFragment :
    BaseBottomSheetDialogFragment<FragmentEditChallengeBinding>(FragmentEditChallengeBinding::inflate),
    PhotoPickerFragment.Callback {

    private val viewModel: EditChallengeViewModel by viewModels()

    private var challengeForChange: ChallengeModelById? = null

    private var dateStartForServerFormatted: ZonedDateTime? = null
    private var dateEndForServerFormatted: ZonedDateTime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            challengeForChange = it.parceleable(CHALLENGE_FOR_CHANGE_KEY)
        }

        setFragmentResultListener(SettingsChallengeFragment.SETTINGS_CHALLENGE_REQUEST_KEY) { requestKey, bundle ->
            val result = bundle.getParcelableExt(
                SettingsChallengeFragment.SETTINGS_CHALLENGE_BUNDLE_KEY,
                CreateChallengeSettingsModel::class.java
            )
            result?.let {
                viewModel.updateChallengeSettings { currentSettings ->
                    it
                }
            }

        }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (this@EditChallengeFragment.dialog as? BottomSheetDialog)?.behavior?.state =
            BottomSheetBehavior.STATE_EXPANDED
        viewModel.getCreateChallengeSettingsFromBack()
        fieldAvailabilityCheck()
        setFieldByInitialData()

        binding.startDateEt.setOnClickListener {
            callDatePicker(true)
        }

        binding.endDateEt.setOnClickListener {
            callDatePicker(false)
        }

        binding.tvAdditionalSettings.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_global_settingsChallengeFragment,
                null,
                OptionsTransaction().optionForEditProfile
            )
        }

        binding.updateBtn.setOnClickListener {
            updateChallenge()
        }
        binding.closeBtn.setOnClickListener {
            dialog?.dismiss()
        }
        observeDataForAccountCard()
        initDragRecycler()

        binding.checkboxChooseAccount.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) binding.accountCard.visible()
            else binding.accountCard.invisible()
        }
    }

    private fun initDragRecycler() {
        challengeForChange?.photos?.let { binding.photoSelection.updateRecyclerViewStringList(it) }

        binding.photoSelection.photoSelectionListener = object : PhotoSelectionListener {
            override fun onAttachImageClicked(maxSelection: Int, alreadySelected: Int) {
                openPicker(maxSelection, alreadySelected)
            }
        }
    }



    private fun openPicker(maxSelection: Int, alreadySelected: Int) {
        PhotoPickerFragment.newInstance(
            multiple = true,
            allowCamera = true,
            maxSelection = maxSelection,
            alreadySelected = alreadySelected,
            mainColor = Branding.brand.colorsJson.mainBrandColor ?: Branding.appThemeDefault.mainBrandColor!!
        ).show(childFragmentManager, "photoPicker")
    }

    override fun onImagesPicked(photos: ArrayList<Uri>) {
        binding.photoSelection.updateRecyclerView(photos)
    }


    private fun hasParticipants() = (challengeForChange?.participantsTotal
        ?: 0) > 0 || challengeForChange?.challengeCondition == ChallengeCondition.FINISHED

    private fun observeDataForAccountCard() {
        viewModel.challengeSettingsState.observe(viewLifecycleOwner) {
            it?.let { settingsModelNonNullable ->
                setDebitAccount(settingsModelNonNullable)
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.invisible()
            }
        }
    }

    private fun setDebitAccount(settingsChallenge: CreateChallengeSettingsModel) {
        if(settingsChallenge.accounts.isNotEmpty()) {
            binding.chooseAccountLinear.visible()
            showDebitCardView(settingsChallenge)
        }else{
            binding.chooseAccountLinear.invisible()
        }
        val firstPersonalAccount =
            settingsChallenge.accounts.find { debitAcc -> debitAcc.myAccount }
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
            if(binding.checkboxChooseAccount.isChecked)  binding.accountCard.visible()
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

    private fun fieldAvailabilityCheck() {
        if (hasParticipants()) {
            binding.linearAdditionalFields.invisible()
        } else {
            binding.linearAdditionalFields.visible()
        }
    }

    private fun updateChallenge() {

        if (challengeForChange?.id != null) {

            val model = if (hasParticipants()) {
                UpdateChallengeModel(
                    name = binding.titleEt.text?.trim().toString(),
                    description = binding.descriptionEt.text?.trim().toString(),
                    photos = binding.photoSelection.getPhotos(),
                    fileList = binding.photoSelection.getFileList()
                )
            } else {
                UpdateChallengeModel(
                    name = binding.titleEt.text?.trim().toString(),
                    description = binding.descriptionEt.text?.trim().toString(),
                    startAt = dateStartForServerFormatted,
                    endAt = dateEndForServerFormatted,
                    startBalance = binding.prizeFundEt.text.toString().toIntOrNull(),
                    winnersCount = binding.prizePoolEt.text.toString().toIntOrNull(),
                    accountId = if(binding.checkboxChooseAccount.isChecked) viewModel.getChallengeSettings().accounts.find { it.current }?.id else null,
                    multipleReports = viewModel.getChallengeSettings().multipleReports,
                    showContenders = viewModel.getChallengeSettings().showContenders,
                    challengeWithVoting = viewModel.getChallengeSettings().challengeWithVoting,
                    photos = binding.photoSelection.getPhotos(),
                    fileList = binding.photoSelection.getFileList()
                )
            }

            viewModel.updateChallenge(
                challengeForChange?.id!!,
                dataOfChallenge = model
            )
        } else {
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.update_challenge_error_not_found_id),
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.updatedChallengeResponse.observe(viewLifecycleOwner) {
            if (it != null && it.status == 0) {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.update_challenge_success_text),
                    Toast.LENGTH_SHORT
                ).show()
                setFragmentResult(
                    EDIT_CHALLENGE_REQUEST_KEY, bundleOf(
                        EDIT_CHALLENGE_BUNDLE_KEY to EditChallengeResponse(
                            challengeWasChanged = true,
                            challengeWasDeleted = false
                        )
                    )
                )
                dialog?.dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "${it?.status} ${it?.details}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun deleteChallenge(challenge: ChallengeModelById) {
        if (challengeForChange?.id != null) {
            viewModel.deleteChallenge(challenge.id)
        } else {
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.update_challenge_error_not_found_id),
                Toast.LENGTH_SHORT
            ).show()
        }
        viewModel.deleteChallengeStatus.observe(viewLifecycleOwner) {
            setFragmentResult(
                EDIT_CHALLENGE_REQUEST_KEY, bundleOf(
                    EDIT_CHALLENGE_BUNDLE_KEY to EditChallengeResponse(
                        challengeWasChanged = false,
                        challengeWasDeleted = true
                    )
                )
            )
        }
    }

    private fun challengeCanBeDeletedByUser(challenge: ChallengeModelById) {
        if ((viewModel.amIAdmin() || challenge.amICreator) && challenge.participantsTotal == 0) {
            binding.tvDeleteChallenge.visible()
            binding.tvDeleteChallenge.setOnClickListener {
                callDeleteChallengeDialog(challenge)
            }
        } else {
            binding.tvDeleteChallenge.visibility = View.INVISIBLE
        }
    }

    private fun callDeleteChallengeDialog(challenge: ChallengeModelById) {
        val settingsDialog = SettingsCustomDialogFragment(
            positiveTextBtn = requireContext().getString(R.string.detail_challenge_delete_yes),
            negativeTextBtn = requireContext().getString(R.string.detail_challenge_delete_no),
            title = requireContext().getString(R.string.detail_challenge_delete_title),
            subtitle = requireContext().getString(R.string.detail_challenge_delete_subtitle)
        )
        val dialogFragment = CustomDialogFragment.newInstance(settingsDialog)
        dialogFragment.setPositiveButtonClickListener(object : PositiveButtonClickListener {
            override fun onPositiveButtonClick() {
                deleteChallenge(challenge)
            }
        })
        dialogFragment.show(requireActivity().supportFragmentManager, "MyDialogFragment")
    }

    private fun setFieldByInitialData() {
        challengeForChange?.let {
            challengeCanBeDeletedByUser(it)
            binding.titleEt.setText(it.name)
            binding.descriptionEt.setText(it.description)
            viewModel.updateChallengeSettings { currentSettings ->
                currentSettings.copy(
                    multipleReports = it.multipleReports,
                    showContenders = it.showContenders,
                    challengeWithVoting = it.challengeWithVoting
                )
            }
            binding.prizePoolEt.setText(it.awardees.toString())
            binding.prizeFundEt.setText(it.fund.toString())
            it.endAt?.let { endAtNotNull ->
                binding.endDateEt.setText(
                    parseDateTimeWithBindToTimeZone(
                        endAtNotNull,
                        requireContext()
                    )
                )
            }
            setDateStart(it)
        }
    }

    private fun setDateStart(challenge: ChallengeModelById) {
        if (challenge.startAt.isNullOrEmptyMy()) binding.startDateTextField.invisible()
        else {
            binding.startDateTextField.visible()
            binding.startDateEt.setText(
                parseDateTimeWithBindToTimeZone(
                    challenge.startAt,
                    requireContext()
                )
            )
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
            val timePicker =
                MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).build()
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
        dateStartForServerFormatted = instant.atZone(ZoneId.systemDefault())
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
        dateEndForServerFormatted = instant.atZone(ZoneId.systemDefault())
        val dateTimeEndForTextView =
            "$dateEndForTextView ${hourOfDay.addLeadZero()}:${minute.addLeadZero()}"
        binding.endDateEt.setText(dateTimeEndForTextView)
    }

    override fun applyTheme() {
    }

    companion object {

        const val CHALLENGE_FOR_CHANGE_KEY = "Challenge for Change Key"
        const val EDIT_CHALLENGE_REQUEST_KEY = "Edit Challenge Request Key"
        const val EDIT_CHALLENGE_BUNDLE_KEY = "Edit Challenge Bundle Key"

        @Parcelize
        data class EditChallengeResponse(
            val challengeWasChanged: Boolean = false,
            val challengeWasDeleted: Boolean = false,
        ) : Parcelable

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditChallengeFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}