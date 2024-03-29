package com.teamforce.thanksapp.presentation.fragment.dialogFragments

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import com.teamforce.thanksapp.databinding.FragmentCustomDialogBinding
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.utils.getParcelableExt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize


@AndroidEntryPoint
class CustomDialogFragment :
    BaseBottomSheetDialogFragment<FragmentCustomDialogBinding>(
        FragmentCustomDialogBinding::inflate
    ) {

    private var settings: SettingsCustomDialogFragment? = null
    private var positiveButtonClickListener: PositiveButtonClickListener? = null
    private var negativeButtonClickListener: NegativeButtonClickListener? = null
    private var dismissListener: DismissListener? = null



    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            settings = it.getParcelableExt(SETTINGS, SettingsCustomDialogFragment::class.java)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSettings()
        binding.continueBtn.setOnClickListener {
            positiveButtonClickListener?.onPositiveButtonClick()
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            negativeButtonClickListener?.onNegativeButtonClick()
            dismiss()
        }
    }

    private fun setSettings(){
        settings?.let {
            binding.title.text = it.title
            binding.subtitle.text = it.subtitle
            binding.continueBtn.text = it.positiveTextBtn
            binding.cancelButton.text = it.negativeTextBtn
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDismiss()
    }


    override fun applyTheme() {

    }

    fun setPositiveButtonClickListener(listener: PositiveButtonClickListener) {
        positiveButtonClickListener = listener
    }

    fun setNegativeButtonClickListener(listener: NegativeButtonClickListener) {
        negativeButtonClickListener = listener
    }

    fun setDismissListener(listener: DismissListener) {
        dismissListener = listener
    }

    companion object {

        const val SETTINGS = "settings"

        @JvmStatic
        fun newInstance(data: SettingsCustomDialogFragment) =
            CustomDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(SETTINGS, data)
                }
            }
    }
}
@Parcelize
data class SettingsCustomDialogFragment(
    val positiveTextBtn: String?,
    val negativeTextBtn: String?,
    val title: String,
    val subtitle: String = "",
) : Parcelable

interface PositiveButtonClickListener {
    fun onPositiveButtonClick()
}

interface NegativeButtonClickListener {
    fun onNegativeButtonClick()
}

interface DismissListener {
    fun onDismiss()
}