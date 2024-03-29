package com.teamforce.thanksapp.presentation.fragment.profileScreen

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentChooseStatusDialogBinding
import com.teamforce.thanksapp.domain.models.profile.FormatOfWork
import com.teamforce.thanksapp.domain.models.profile.ProfileModel
import com.teamforce.thanksapp.domain.models.profile.Status
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.presentation.viewmodel.profile.EditProfileViewModel
import com.teamforce.thanksapp.utils.Consts.PROFILE_DATA
import com.teamforce.thanksapp.utils.getParcelableExt
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseStatusDialogFragment : BaseBottomSheetDialogFragment<FragmentChooseStatusDialogBinding>(FragmentChooseStatusDialogBinding::inflate) {

    private val viewModel: EditProfileViewModel by viewModels()

    private var checkedStatus: String? = null
    private var checkedFormatOfWork: String? = null
    private var profile: ProfileModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            profile = it.getParcelableExt(PROFILE_DATA, ProfileModel::class.java)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Установка текущего статуса
        if (profile != null) {
            setCurrentStatus(profile!!)
        }
        binding.applyBtn.setOnClickListener {
            if (profile != null && profile?.profile?.status != checkedStatus) {
                viewModel.updateStatus(
                    userId = profile!!.profile.idForEdit,
                    status = checkedStatus,
                    formatOfWork = checkedFormatOfWork
                )
            }
        }
        viewModel.updateProfile.observe(viewLifecycleOwner){
            findNavController().navigate(R.id.action_chooseStatusDialogFragment2_to_profileFragment)
        }

        viewModel.isLoading.observe(viewLifecycleOwner){
            if(it) showProgressBar()
            else hideProgressBar()
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            checkedStatus = when (checkedId) {
                R.id.radio_vacation -> Status.VACATION.value
                R.id.radio_sick -> Status.SICK_LEAVE.value
                else -> Status.WORK.value
            }
        }

        binding.radioGroupFormatWork.setOnCheckedChangeListener { group, checkedId ->
            checkedFormatOfWork = when (checkedId) {
                R.id.radio_remote -> FormatOfWork.REMOTE.value
                R.id.radio_office -> FormatOfWork.OFFICE.value
                else -> FormatOfWork.REMOTE.value
            }
        }
    }


    private fun showProgressBar() {
        binding.progressBar.visible()
    }

    private fun hideProgressBar() {
        binding.progressBar.invisible()
    }

    override fun applyTheme() {

    }

    private fun setCurrentStatus(profileModel: ProfileModel) {
        when (profileModel.profile.status) {
            Status.VACATION.value -> binding.radioVacation.isChecked = true
            Status.SICK_LEAVE.value -> binding.radioSick.isChecked = true
            Status.WORK.value -> binding.radioWork.isChecked = true
        }

        when(profileModel.profile.formatOfWork){
            FormatOfWork.OFFICE.value -> binding.radioOffice.isChecked = true
            FormatOfWork.REMOTE.value -> binding.radioRemote.isChecked = true
        }
    }

    override fun onDestroyView() {
        checkedStatus = null
        profile = null
        super.onDestroyView()
    }

    companion object {

        @JvmStatic
        fun newInstance(profile: ProfileModel) =
            ChooseStatusDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PROFILE_DATA, profile)
                }
            }
    }

}