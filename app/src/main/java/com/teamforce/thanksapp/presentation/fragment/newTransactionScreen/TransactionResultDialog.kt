package com.teamforce.thanksapp.presentation.fragment.newTransactionScreen

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.teamforce.thanksapp.data.response.UserListItem
import com.teamforce.thanksapp.databinding.FragmentTransactionResultDialogBinding
import com.teamforce.thanksapp.presentation.base.BaseDialogFragment
import com.teamforce.thanksapp.utils.blur
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.branding.Cases
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.isNotNullOrEmptyMy
import com.teamforce.thanksapp.utils.parceleable
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class TransactionResultDialog : BaseDialogFragment<FragmentTransactionResultDialogBinding>(FragmentTransactionResultDialogBinding::inflate) {

    private var user: UserListItem.UserBean? = null

    private var amountTransfer: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.parceleable(USER_DATA)
            amountTransfer = it.getInt(AMOUNT_TRANSFER)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            setStyle(STYLE_NO_FRAME, android.R.style.Theme);
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.blurView.blur(5f, requireContext(), binding.root)
        if(user != null && amountTransfer != 0){
            setData(user!!, amountTransfer)
        }else{
            binding.userLinear.invisible()
        }
        binding.blurView.setOnClickListener {
            dialog?.dismiss()
        }


    }

    private fun setData(user: UserListItem.UserBean, amountTransfer: Int){

        val (currentDate, currentTime) = getCurrentDateTime()
        binding.dateTv.text = currentDate
        binding.timeTv.text = currentTime
        binding.userNameTv.text = getUserName(user)
        binding.amountTransferTv.text = "${amountTransfer} ${Branding.declineCurrency(amountTransfer, Cases.GENITIVE)}"
        binding.userAvatar.setAvatarImageOrInitials(user.photo, "${user.firstname} ${user.surname}")
    }

    private fun getUserName(user: UserListItem.UserBean): String{
        return if(user.firstname.isNotEmpty() || user.surname.isNotNullOrEmptyMy()){
            "${user.firstname} ${user.surname}"
        }else{
            user.tgName
        }
    }



    private fun getCurrentDateTime(): Pair<String, String> {
        val currentDate = getCurrentDate()
        val currentTime = getCurrentTime()

        return Pair(currentDate, currentTime)
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(calendar.time)
    }




    override fun applyTheme() {

    }

    companion object {
        const val USER_DATA = "transaction_result_user_data"
        const val AMOUNT_TRANSFER = "transaction_result_amount_transfer"


        @JvmStatic
        fun newInstance(user: UserListItem.UserBean, amountTransfer: Int) =
            TransactionResultDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(USER_DATA, user)
                    putInt(AMOUNT_TRANSFER, amountTransfer)
                }
            }
    }
}