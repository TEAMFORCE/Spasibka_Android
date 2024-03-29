package com.teamforce.thanksapp.presentation.fragment.profileScreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentChooseGenderDialogBinding
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.presentation.viewmodel.profile.EditProfileViewModel
import com.teamforce.thanksapp.utils.Consts


class ChooseGenderDialogFragment : DialogFragment(R.layout.fragment_choose_gender_dialog) {

    private val binding: FragmentChooseGenderDialogBinding by viewBinding()
    private val viewModel: EditProfileViewModel by activityViewModels()

    private var checkedGender: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (viewModel.gender.value) {
            "M" -> binding.male.isChecked = true
            "W" -> binding.female.isChecked = true
            else -> {}
        }
        binding.statusRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            checkedGender = when (checkedId) {
                R.id.male -> "M"
                R.id.female -> "W"
                else -> null
            }
            viewModel.setGender(checkedGender)
            this.dismiss()
        }

    }


    companion object {

        @JvmStatic
        fun newInstance() =
            ChooseGenderDialogFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}