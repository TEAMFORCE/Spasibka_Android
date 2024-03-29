package com.teamforce.thanksapp.presentation.fragment.profileScreen.settings

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.request.settings.CreateCommunityWithPeriodRequest
import com.teamforce.thanksapp.databinding.FragmentCreateOrganizationBinding
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.viewmodel.profile.SettingsViewModel
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.DATE_FORMAT_FOR_SERVER
import com.teamforce.thanksapp.utils.DATE_FORMAT_FOR_USER
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.navigateSafely
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class CreateOrganizationFragment : BaseFragment<FragmentCreateOrganizationBinding>(FragmentCreateOrganizationBinding::inflate) {

    private val viewModel: SettingsViewModel by activityViewModels()


    private var dateRangePeriod: Pair<String?, String?> = Pair(null, null)
    private val dateFormatForServer = SimpleDateFormat(DATE_FORMAT_FOR_SERVER, Locale.getDefault())
    private val dateFormatForTextView = SimpleDateFormat(DATE_FORMAT_FOR_USER, Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.navContainer
            duration = 400.toLong()
            scrimColor = Color.WHITE
            setAllContainerColors(Color.WHITE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition(100L, TimeUnit.MILLISECONDS)
        binding.startPeriodEt.setOnClickListener {
            callRangeDatePicker()
        }
        binding.endPeriodEt.setOnClickListener {
            callRangeDatePicker()
        }
        binding.createOrgBtn.setOnClickListener {
            if(fieldsNotEmpty()){
                viewModel.createCommunity(CreateCommunityWithPeriodRequest(
                    organization_name = binding.nameEt.text.toString(),
                    period_start_date = dateRangePeriod.first!!,
                    period_end_date = dateRangePeriod.second!!,
                    users_start_balance = binding.startBalanceEt.text.toString().toInt(),
                    owner_start_balance = binding.startBalanceAdminEt.text.toString().toInt()
                ))
            }else{
                showNotFilledFieldsError()
            }
        }
        viewModel.createCommunity.observe(viewLifecycleOwner){ response ->
            response?.let {
                activity?.onBackPressedDispatcher?.onBackPressed()
                val bundle = Bundle()
                bundle.putString(Consts.COMMUNITY_ID, it.organization_id.toString())
                findNavController().navigateSafely(
                    R.id.action_global_inviteMembersBottomSheetDialogFragment2, bundle
                )
            }

        }
        viewModel.error.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        binding.closeTv.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun callRangeDatePicker() {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendarConstraintBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(today))


        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(requireContext().getString(R.string.create_organization_select_dates))
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

    private fun fieldsNotEmpty(): Boolean = listOf(
        binding.nameEt,
        binding.startPeriodEt,
        binding.endPeriodEt,
        binding.startBalanceEt,
        binding.startBalanceAdminEt
    ).all { !it.text.isNullOrEmpty() }

    private fun showNotFilledFieldsError() {
        val textError = requireContext().getString(R.string.required_field)
        val fieldsMap = mapOf(
            binding.nameEt to binding.textInputName,
            binding.startPeriodEt to binding.textInputStartPeriod,
            binding.endPeriodEt to binding.textInputEndPeriod,
            binding.startBalanceEt to binding.textInputStartBalance,
            binding.startBalanceAdminEt to binding.textInputStartBalanceAdmin
        )

        fieldsMap.forEach { (editText, textInputLayout) ->
            if (editText.text.isNullOrEmpty()) {
                textInputLayout.error = textError
            } else {
                textInputLayout.error = null
            }
        }
    }

    override fun applyTheme() {
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateOrganizationFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}