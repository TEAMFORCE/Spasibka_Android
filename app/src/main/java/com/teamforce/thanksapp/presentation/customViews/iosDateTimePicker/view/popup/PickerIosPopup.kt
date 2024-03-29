package com.teamforce.thanksapp.presentation.customViews.iosDateTimePicker.view.popup


import android.content.Context
import android.view.View

import android.widget.LinearLayout

import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.teamforce.thanksapp.databinding.IosPickerPopupLayoutBinding


open class PickerIosPopup(
    context: Context
) : BottomSheetDialog(context), IPopupIosInterface {

    var confirm: TextView? = null
    var cancel: TextView? = null
    private var container: LinearLayout? = null

    private var _binding: IosPickerPopupLayoutBinding? = null
    val binding
        get() = _binding!!


    init {
        init()
    }

    private fun init() {
        _binding = IosPickerPopupLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
       // setContentView(R.layout.ios_picker_popup_layout)
        confirm = binding.textConfirm
        cancel = binding.textCancel
        container = binding.popupContainerIos
        cancel!!.setOnClickListener { dismiss() }
    }

    override fun addView(view: View?) {
        container!!.addView(view)
    }
}