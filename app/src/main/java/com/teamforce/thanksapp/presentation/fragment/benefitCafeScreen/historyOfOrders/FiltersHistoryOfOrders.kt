package com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.historyOfOrders

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentFiltersHistoryOfOrdersBinding
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.presentation.viewmodel.benefit.HistoryOfOrdersViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FiltersHistoryOfOrders : BaseBottomSheetDialogFragment<FragmentFiltersHistoryOfOrdersBinding>(FragmentFiltersHistoryOfOrdersBinding::inflate) {

    private val viewModel: HistoryOfOrdersViewModel by activityViewModels()

    private var listOfStatus = mutableListOf<String>()


    override fun getTheme(): Int  = R.style.AppBottomSheetDialogTheme


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInitialCheckedCheckBox()
        binding.statusRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.all_status -> {
                    Log.d(TAG, "all")
                    // nothing
                    listOfStatus = mutableListOf()
                }
                R.id.ordered_status -> {
                    Log.d(TAG, "ordered")
                    // 1 - moved from cart to orders, considering
                    // 5 - accepted, waiting
                    // 6 - accepted by customer
                    // 7 - purchased
                    // 8 - ready for delivery
                    // 9 - sent or delivered
                    listOfStatus = mutableListOf("1", "5", "6", "7", "8", "9")
                }
                R.id.received_status -> {
                    Log.d(TAG, "received")
                    // 10 - ready
                    listOfStatus = mutableListOf("10")
                }
                R.id.declined_status -> {
                    Log.d(TAG, "declined")
                    // 20 - declined
                    listOfStatus = mutableListOf("20")
                }
                R.id.cancelled_status -> {
                    Log.d(TAG, "cancelled")
                    //21 - cancelled
                    listOfStatus = mutableListOf("21")
                }
            }
        }
        binding.applyBtn.setOnClickListener {
            viewModel.setStatus(listOfStatus)
            this.dismiss()
        }
        binding.closeBtn.setOnClickListener {
            viewModel.resetStatus()
            this.dismiss()
        }
    }

    override fun applyTheme() {

    }

    private fun setInitialCheckedCheckBox(){
        viewModel.statusList.observe(viewLifecycleOwner){
            if(it.isNullOrEmpty()) binding.allStatus.isChecked = true
            else if(it.find { status -> status == "1" } != null){
                binding.orderedStatus.isChecked = true
            }else if(it.find { status -> status == "10" } != null){
                binding.receivedStatus.isChecked = true
            }else if(it.find { status -> status == "20" } != null){
                binding.declinedStatus.isChecked = true
            }else if(it.find { status -> status == "21" } != null){
                binding.cancelledStatus.isChecked = true
            }

        }
    }



    companion object {
        const val TAG = "FiltersHistoryOfOrders"

        @JvmStatic
        fun newInstance() =
            FiltersHistoryOfOrders().apply {
                arguments = Bundle().apply {

                }
            }
    }
}