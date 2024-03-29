package com.teamforce.thanksapp.presentation.fragment.historyScreen

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentFilterHistoryTransactionsBottomSheetBinding
import com.teamforce.thanksapp.databinding.FragmentFiltersBottomSheetDialogBinding
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.utils.DateUtils
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view.datePicker.DatePickerIos
import com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view.popup.DatePickerIosPopup
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.presentation.viewmodel.history.HistoryListViewModel
import com.teamforce.thanksapp.utils.formatDateFromLongToUserViewWithYear
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilterHistoryTransactionsBottomSheetFragment : BaseBottomSheetDialogFragment<FragmentFilterHistoryTransactionsBottomSheetBinding>(
    FragmentFilterHistoryTransactionsBottomSheetBinding::inflate) {

    private val viewModel: HistoryListViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restorePriceField()
        binding.dateFromToEt.setOnClickListener {
            initialDatePicker(true)
        }

        binding.dateUpToEt.setOnClickListener {
            initialDatePicker(false)
        }

        binding.closeBtn.setOnClickListener {
            viewModel.resetDate()
            viewModel.setFilterUpdated(true)
            this.dismiss()
        }

        binding.continueBtn.setOnClickListener {
            viewModel.setFilterUpdated(true)
            this.dismiss()
        }

        viewModel.upToDate.observe(viewLifecycleOwner){ upDate ->
            upDate?.let {
                binding.dateUpToEt.setText(formatDateFromLongToUserViewWithYear(it, requireContext()))
            }
        }

        viewModel.fromToDate.observe(viewLifecycleOwner){ fromDate ->
            fromDate?.let {
                binding.dateFromToEt.setText(formatDateFromLongToUserViewWithYear(it, requireContext()))
            }
        }
    }

    private fun initialDatePicker(setFromTo: Boolean){
        val datePickerPopup = DatePickerIosPopup.Builder()
            .from(requireContext())
            .offset(3)
            .darkModeEnabled(false)
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
                    if(setFromTo) viewModel.setFromToDateFilter(date)
                    else viewModel.setUpToDateFilter(date)

                    restorePriceField()
                }
            })
            .build()
        datePickerPopup.show()
    }

    private fun restorePriceField(){
        viewModel.fromToDate.value?.let {
            binding.dateFromToEt.setText(formatDateFromLongToUserViewWithYear(it, requireContext()))
        }
        viewModel.upToDate.value?.let {
            binding.dateUpToEt.setText(formatDateFromLongToUserViewWithYear(it, requireContext()))
        }
    }

    override fun applyTheme() {

    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FilterHistoryTransactionsBottomSheetFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}