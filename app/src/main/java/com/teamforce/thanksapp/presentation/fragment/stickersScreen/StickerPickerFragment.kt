package com.teamforce.thanksapp.presentation.fragment.stickersScreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentStickerPickerBinding
import com.teamforce.thanksapp.presentation.adapter.stickers.StickersAdapter
import com.teamforce.thanksapp.presentation.viewmodel.CheckedSticker
import com.teamforce.thanksapp.presentation.viewmodel.TransactionViewModel
import com.teamforce.thanksapp.utils.ViewLifecycleDelegate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StickerPickerFragment : BottomSheetDialogFragment(R.layout.fragment_sticker_picker) {

    private val binding: FragmentStickerPickerBinding by viewBinding()
    private val viewModel: TransactionViewModel by activityViewModels()
    private val stickersAdapter by ViewLifecycleDelegate {
        StickersAdapter(::onStickerClicked)
    }


    override fun getTheme(): Int  = R.style.AppBottomSheetDialogTheme


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadAllStickers()
        initList()
    }

    private fun initList(){
        val linearLayoutManagerHorizontal = LinearLayoutManager(requireContext())
        linearLayoutManagerHorizontal.orientation = LinearLayoutManager.HORIZONTAL
        binding.list.apply {
            layoutManager = linearLayoutManagerHorizontal
            adapter = stickersAdapter
        }
        viewModel.stickers.observe(viewLifecycleOwner){
           // (binding.list.adapter as StickersAdapter).submitList(null)
            (binding.list.adapter as StickersAdapter).submitList(it)
        }
    }

    private fun onStickerClicked(sticker: CheckedSticker){
        // Клик по стикеру, сохранение выбранного id стикера и установка его в предпросмотре`
        viewModel.setCheckedStickerId(sticker)
        this.dismiss()
    }

    override fun onDestroyView() {
        binding.list.adapter = null
        stickersAdapter.submitList(null)
        super.onDestroyView()
    }



    companion object {

        @JvmStatic
        fun newInstance() =
            StickerPickerFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}