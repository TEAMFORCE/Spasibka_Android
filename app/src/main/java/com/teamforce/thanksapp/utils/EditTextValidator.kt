package com.teamforce.thanksapp.utils

import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputLayout

class EditTextValidator {

    companion object {
        fun areFieldsNotEmpty(vararg fields : AppCompatEditText): Boolean{
            return fields.none { it.text.isNullOrEmpty() }
        }

        fun setErrorToEmptyFields(vararg fields : Pair<TextInputLayout, AppCompatEditText>, error: String){
            fields.map {
                if(it.second.text.isNullOrEmpty()){
                    it.first.errorIconDrawable = null
                    it.first.error = error
                }else{
                    it.first.error = ""
                }
            }
        }
    }
}