package com.teamforce.thanksapp.presentation.fragment.employeesScreen

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.distinctUntilChanged
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentEmployeesFilterBinding
import com.teamforce.thanksapp.domain.models.employees.DepartmentFilterModel
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.presentation.customViews.AvatarView.internal.dp
import com.teamforce.thanksapp.presentation.theme.ThemableCheckBox
import com.teamforce.thanksapp.presentation.viewmodel.employees.EmployeesViewModel
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.dp


class EmployeesFilterFragment : BaseBottomSheetDialogFragment<FragmentEmployeesFilterBinding>(FragmentEmployeesFilterBinding::inflate)  {

    private val viewModel: EmployeesViewModel by activityViewModels()

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreCheckedSwitch()
        binding.continueBtn.setOnClickListener {
            // Обновить список согласно фильтрам и закрыть фрагмент
            Log.e("Ids", "SeletecedIds ${getSelectedIds(binding.header)}")
            viewModel.setSelectedDepartments(getSelectedIds(binding.header))
            checkSwitch()
            this.dismiss()
        }
        binding.closeBtn.setOnClickListener {
            reset()
            this.dismiss()
        }
        viewModel.departments.distinctUntilChanged().observe(viewLifecycleOwner){
            generateUI(binding.header, it)
            if(!viewModel.selectedDepartments.value.isNullOrEmpty()){
                setRestoreCheckedIds(binding.header, viewModel.selectedDepartments.value!!)
            }

        }
    }

    override fun applyTheme() {

    }

    private fun restoreCheckedSwitch() {
        if (viewModel.onHoliday.value == true) binding.onHoliday.isChecked = true
        if (viewModel.inOffice.value == true) binding.inOffice.isChecked = true
        if (viewModel.isFired.value == true) binding.isFired.isChecked = true
    }

    private fun checkSwitch() {
        viewModel.setInOffice(binding.inOffice.isChecked)
        viewModel.setIsFired(binding.isFired.isChecked)
        viewModel.setOnHoliday(binding.onHoliday.isChecked)
    }

    private fun reset() {
        viewModel.setInOffice(false)
        viewModel.setIsFired(false)
        viewModel.setOnHoliday(false)
        viewModel.setSelectedDepartments(setOf())
    }

    private fun setRestoreCheckedIds(root: ViewGroup, checkedIds: Set<Long>){
        for (child in root.children){
            if(child is CheckBox){
                child.isChecked = checkedIds.contains(child.tag as? Long)
            }else if(child is LinearLayout){
                setRestoreCheckedIds(child, checkedIds)
            }
        }
    }

    fun generateUI(root: ViewGroup, organizations: List<DepartmentFilterModel>, marginStart: Float = 0F) {
        for (organization in organizations) {
            val linearLayout = LinearLayout(root.context)
            linearLayout.orientation = LinearLayout.VERTICAL
            val params = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.marginStart = marginStart.toInt()
            linearLayout.layoutParams = params

            val checkBox = ThemableCheckBox(root.context)
            val checkBoxParams =  ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            checkBoxParams.bottomMargin = 24
            checkBox.layoutParams = checkBoxParams
            checkBox.text = "  " + organization.name
            checkBox.tag = organization.id
            checkBox.setThemeColor(Branding.appTheme)

            linearLayout.addView(checkBox)

            if (organization.children.isNotEmpty()) {
                generateUI(linearLayout, organization.children, marginStart + 24F.dp)
            }

            root.addView(linearLayout)

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                handleCheckBoxCheckedChange(linearLayout, isChecked)
            }
        }
    }


    fun handleCheckBoxCheckedChange(linearLayout: LinearLayout, isChecked: Boolean) {
        for (i in 0 until linearLayout.childCount) {
            val child = linearLayout.getChildAt(i)
            if (child is CheckBox) {
                child.isChecked = isChecked
            }
            if(child is LinearLayout){
                handleCheckBoxCheckedChange(child, isChecked)
            }
        }
    }

    fun getSelectedIds(root: ViewGroup): Set<Long> {
        val selectedIds = mutableSetOf<Long>()
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            if (child is LinearLayout) {
                for (j in 0 until child.childCount) {
                    val checkBox = child.getChildAt(j) as? CheckBox
                    if (checkBox != null && checkBox.isChecked) {
                        (checkBox.tag as? Long)?.let{
                            selectedIds.add(it)
                        }
                    }
                }
                selectedIds.addAll(getSelectedIds(child))
            }
        }
        return selectedIds
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            EmployeesFilterFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}