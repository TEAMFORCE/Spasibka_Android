package com.teamforce.thanksapp.presentation.fragment.auth.onBoarding

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentSettingsPeriodBinding
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.viewmodel.onboarding.SettingsPeriodViewModel
import com.teamforce.thanksapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class SettingsPeriodFragment :
    BaseFragment<FragmentSettingsPeriodBinding>(FragmentSettingsPeriodBinding::inflate) {

    private val dateFormatForServer = SimpleDateFormat(DATE_FORMAT_FOR_SERVER, Locale.getDefault())
    private val dateFormatForTextView = SimpleDateFormat(DATE_FORMAT_FOR_USER, Locale.getDefault())


    private val viewModel: SettingsPeriodViewModel by viewModels()

    private var dateRangePeriod: Pair<String?, String?> = Pair(null, null)

    private var isNeedGoToMainFlow: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isNeedGoToMainFlow = it.getBoolean(Consts.RETURN_AFTER_FINISHIED)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.settingsScreenState.observe(viewLifecycleOwner) { screenState ->
            when (screenState) {
                SettingsPeriodViewModel.SettingScreenState.AUTO_SETTINGS -> disableEditField()
                SettingsPeriodViewModel.SettingScreenState.MANUAL_SETTINGS -> enableEditField()
                else -> disableEditField()
            }

        }

        binding.startPeriodBtn.setOnClickListener {
            if (dateRangePeriod.first != null && dateRangePeriod.second != null && !binding.startBalanceEt.text.isNullOrEmpty()) {
                // Старт периода
                viewModel.launchCommunityPeriod(
                    dateRangePeriod.first.toString(),
                    dateRangePeriod.second.toString(),
                    binding.startBalanceEt.text.toString().toInt(),
                    binding.startBalanceAdminEt.text.toString().toInt()
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.onboarding_fill_date_need,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.startPeriodEt.setOnClickListener {
            callRangeDatePicker()
        }
        binding.endPeriodEt.setOnClickListener {
            callRangeDatePicker()
        }


        viewModel.launchPeriod.observe(viewLifecycleOwner) { isLaunchedPeriod ->
            if (isLaunchedPeriod){
                Toast.makeText(
                    requireContext(),
                    R.string.onboarding_launched_period_successfully,
                    Toast.LENGTH_LONG
                ).show()
                if (isNeedGoToMainFlow) {
                    findNavController().navigate(
                        R.id.action_settingsPeriodFragment2_to_feed_graph,
                        null,
                        OptionsTransaction().optionForLastScreenOnBoardingFromMainFlow
                    )
                } else {
                    findNavController().navigateSafely(
                        R.id.action_settingsPeriodFragment_to_lastOnBoardingScreenFragment,
                        null,
                        OptionsTransaction().optionForLastScreenOnBoarding
                    )
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showProgressBar() else hideProgressBar()
        }

        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }


    }

    private fun hideProgressBar() {
        binding.progressBar.invisible()
        binding.startPeriodBtn.isEnabled = true
    }

    private fun showProgressBar() {
        binding.progressBar.visible()
        binding.startPeriodBtn.isEnabled = false
    }

    private fun callRangeDatePicker() {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendarConstraintBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(today))


        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .setCalendarConstraints(calendarConstraintBuilder.build())
                .build()

        dateRangePicker.show(childFragmentManager, "tag")

        dateRangePicker.addOnPositiveButtonClickListener {
            parseRangeDates(it)
        }
    }

    private fun parseRangeDates(range: androidx.core.util.Pair<Long, Long>) {
        val startDate = range.first
        val endDate = range.second

        val startDateStrForServer = dateFormatForServer.format(Date(startDate))
        val endDateStrForServer = dateFormatForServer.format(Date(endDate))
        dateRangePeriod = Pair(startDateStrForServer, endDateStrForServer)

        binding.startPeriodEt.setText(dateFormatForTextView.format(Date(startDate)))
        binding.endPeriodEt.setText(dateFormatForTextView.format(Date(endDate)))

    }

    override fun applyTheme() {

    }

    private fun enableEditField() {
        binding.apply {
            startPeriodEt.setText("")
            startPeriodEt.isEnabled = true
            endPeriodEt.setText("")
            endPeriodEt.isEnabled = true
            startBalanceEt.setText("")
            startBalanceEt.isEnabled = true
            startBalanceAdminEt.setText("")
            startBalanceAdminEt.isEnabled = true
            dateRangePeriod = Pair(null, null)
        }
        binding.settingsPeriodBtn.setText(R.string.onboarding_settings_auto)
        binding.settingsPeriodBtn.setOnClickListener {
            viewModel.updateScreenState(SettingsPeriodViewModel.SettingScreenState.AUTO_SETTINGS)
        }
    }

    private fun disableEditField() {
        binding.apply {
            startPeriodEt.setText(getCurrentDate())
            startPeriodEt.isEnabled = false
            endPeriodEt.setText(getDateAfterThreeMonths())
            endPeriodEt.isEnabled = false
            startBalanceEt.setText("50")
            startBalanceEt.isEnabled = false
            startBalanceAdminEt.setText("50")
            startBalanceAdminEt.isEnabled = false
            setAutoDateForServer()

        }
        binding.settingsPeriodBtn.setText(R.string.onboarding_settings_manually)
        binding.settingsPeriodBtn.setOnClickListener {
            viewModel.updateScreenState(SettingsPeriodViewModel.SettingScreenState.MANUAL_SETTINGS)
        }
    }

    private fun getCurrentDate(): String {
        val currentDate = Date().time
        return dateFormatForTextView.format(currentDate)
    }

    private fun getDateAfterThreeMonths(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 3)
        val dateAfterThreeMonths = calendar.time
        return dateFormatForTextView.format(dateAfterThreeMonths)
    }

    private fun setAutoDateForServer() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 3)
        val dateAfterThreeMonths = calendar.time
        dateRangePeriod = Pair(
            dateFormatForServer.format(Date().time),
            dateFormatForServer.format(dateAfterThreeMonths)
        )
    }


    companion object {

        @JvmStatic
        fun newInstance(isNeedReturnAfterFinished: Boolean) =
            SettingsPeriodFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(Consts.RETURN_AFTER_FINISHIED, isNeedReturnAfterFinished)
                }
            }
    }
}