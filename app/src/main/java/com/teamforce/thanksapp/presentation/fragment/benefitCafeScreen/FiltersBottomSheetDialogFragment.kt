package com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.entities.benefit.Category
import com.teamforce.thanksapp.databinding.FragmentFiltersBottomSheetDialogBinding
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.presentation.viewmodel.benefit.BenefitListViewModel
import com.teamforce.thanksapp.utils.branding.Branding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FiltersBottomSheetDialogFragment : BaseBottomSheetDialogFragment<FragmentFiltersBottomSheetDialogBinding>(FragmentFiltersBottomSheetDialogBinding::inflate){

    private val viewModel: BenefitListViewModel by activityViewModels()

    private val btnColorTint = ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked),
        ), intArrayOf(
            Color.parseColor(Branding.appTheme.mainBrandColor),
            Color.parseColor(Branding.appTheme.generalContrastColor)
        )
    )

    override fun getTheme(): Int  = R.style.AppBottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNecessaryCategory()
        restorePriceField()
        viewModel.categories.observe(viewLifecycleOwner){
            if (it != null) {
                setCategories(it)
                binding.categoryRadioGroup.check(viewModel.checkedIdCategory.value ?: 0)
            }
        }

        binding.continueBtn.setOnClickListener {
            // Обновить список согласно фильтрам и закрыть фрагмент
            Log.d(TAG, " Выбранная категория ${binding.categoryRadioGroup.checkedRadioButtonId}")
            viewModel.setCheckedIdCategory(binding.categoryRadioGroup.checkedRadioButtonId)
            viewModel.setFilterPrice(
                fromTo = binding.priceFromToEt.text?.trim().toString(),
                upTo = binding.priceUpToEt.text?.trim().toString()
            )
            this.dismiss()
        }
        binding.closeBtn.setOnClickListener {
            // Закрыть фрагмент, ничего не обновлять, поля сбросить
            viewModel.resetPrice()
            this.dismiss()
        }

        binding.closeTv.setOnClickListener {
            this.dismiss()
        }
    }

    override fun applyTheme() {

    }

    private fun restorePriceField(){
        binding.priceFromToEt.setText(viewModel.price.value?.first)
        binding.priceUpToEt.setText(viewModel.price.value?.second)
    }

    private fun setCategories(categories: List<Category>){
        for (i in categories.indices) {
            val categoryName = categories[i].name
            val categoryId = categories[i].id
            val radioBtn: RadioButton = LayoutInflater.from(binding.categoryRadioGroup.context)
                .inflate(
                    R.layout.item_category_radiobutton_benefit_filter,
                    binding.categoryRadioGroup,
                    false
                ) as RadioButton
            with(radioBtn) {
                id = categoryId
                text = categoryName
              //  setEnsureMinTouchTargetSize(true)
                minimumWidth = 0
                buttonTintList = btnColorTint
            }
            binding.categoryRadioGroup.addView(radioBtn)
        }
    }

    private fun setNecessaryCategory(){
        val categoryView: RadioButton = LayoutInflater.from(binding.categoryRadioGroup.context)
            .inflate(
                R.layout.item_category_radiobutton_benefit_filter,
                binding.categoryRadioGroup,
                false
            ) as RadioButton
        with(categoryView){
            id = 0
            text = requireContext().getString(R.string.allEvent)
            minimumWidth = 0
            buttonTintList = btnColorTint
        }
        binding.categoryRadioGroup.addView(categoryView)
    }





    companion object {

        const val TAG = "FiltersBottomSheetDialogFragment"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FiltersBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}