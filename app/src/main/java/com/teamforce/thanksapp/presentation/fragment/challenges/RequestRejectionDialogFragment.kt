package com.teamforce.thanksapp.presentation.fragment.challenges

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentRequestRejectionDialogBinding
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.presentation.viewmodel.challenge.ContendersChallengeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestRejectionDialogFragment: BaseBottomSheetDialogFragment<FragmentRequestRejectionDialogBinding>(FragmentRequestRejectionDialogBinding::inflate) {


    private val viewModel: ContendersChallengeViewModel by activityViewModels()

    private var idChallenge: Int? = null
    private var reportId: Int? = null
    private var state: Char? = null
    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idChallenge = it.getInt(REJECTION_DIALOG_CHALLENGE_ID)
            reportId = it.getInt(REJECTION_DIALOG_REPORT_ID)
            state = it.getChar(REJECTION_DIALOG_STATE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (this@RequestRejectionDialogFragment.dialog as? BottomSheetDialog)?.behavior?.state =
            BottomSheetBehavior.STATE_EXPANDED

        binding.rejectBtn.setOnClickListener {
            if (binding.descriptionEt.text?.trim()?.isNotEmpty() == true
            ) {
                if (idChallenge != null && reportId != null && state != null) {
                    viewModel.checkReport(
                        reportId!!, state!!,
                        binding.descriptionEt.text.toString(),
                        idChallenge!!
                    )
                }
                this.dialog?.cancel()
            } else {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.reasonOfRejectionIsNecessary),
                    Toast.LENGTH_LONG
                ).show()
            }

        }
        binding.closeDialogBtn.setOnClickListener {
            this.dismiss()
        }
    }

    override fun applyTheme() {

    }



    companion object {
        const val REJECTION_DIALOG_REPORT_ID = "report_id"
        const val REJECTION_DIALOG_STATE = "state"
        const val REJECTION_DIALOG_CHALLENGE_ID = "challenge_id"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param reportId Int.
         * @param state Char.
         * @param idChallenge Int.
         * @return A new instance of fragment RequestRejectionDialogFragment.
         */
        @JvmStatic
        fun newInstance(reportId: Int, state: Char, idChallenge: Int) =
            RequestRejectionDialogFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}