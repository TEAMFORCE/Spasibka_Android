package com.teamforce.thanksapp.presentation.customViews.counterAmount

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater

import android.widget.LinearLayout
import com.teamforce.thanksapp.databinding.LayoutViewCounterBinding
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.utils.branding.Branding

class CounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
): LinearLayout(context, attrs, defStyleAttr, defStyleRes), Themable {

    private var _binding: LayoutViewCounterBinding? = null
    private val binding get() = checkNotNull(_binding) { "Binding is null" }

    //Will handle listener events and takes in the current value
    private var valueListener: ((Int) -> Unit)? = null


    //Property accessor on increments or decrements the edit text value will be updated
    var currentValue: Int = 1
        set(value) {
            if (value >= 1) {
                field = value
                binding.countOfferTv.setText(field.toString())
            }
        }

    init {
        orientation = HORIZONTAL
//        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        inflater.inflate(R.layout.layout_view_counter, this)
        _binding = LayoutViewCounterBinding.inflate(LayoutInflater.from(context), this, true)
        binding.countOfferTv.setText(currentValue.toString())
        binding.plus.setOnClickListener {
            incrementValue()
        }
        binding.minus.setOnClickListener {
            decrementValue()
        }
    }


    private fun decrementValue() {
        val counterCurrentValue = binding.countOfferTv.text
        if (counterCurrentValue.toString() == "0") {
            binding.countOfferTv.setText(currentValue.toString())
        } else {
            currentValue = getEditTextValue()
            --currentValue
        }
        retrieveValue()
    }

    private fun incrementValue() {
        currentValue = getEditTextValue()
        ++currentValue
        retrieveValue()
    }

    private fun getEditTextValue(): Int {
        val counterCurrentValue = binding.countOfferTv.text
        return counterCurrentValue.toString().toInt()
    }

    //Checks if value listener has been set and passes the value to the function for any further operations
    private fun retrieveValue() {
        this.valueListener?.let { function ->
            function(currentValue)
        }
    }

    //Defines higher order function for setting value listener
    fun setListener(value: (Int) -> Unit) {
        this.valueListener = value
    }

    override fun setThemeColor(theme: ColorsModel) {
        binding.apply {
            plus.setCardBackgroundColor(Color.parseColor(Branding.appTheme.secondaryBrandColor))
            minus.setCardBackgroundColor(Color.parseColor(Branding.appTheme.secondaryBrandColor))
            countOfferTv.setTextColor(Color.parseColor(Branding.appTheme.generalContrastColor))
            countOfferCard.setCardBackgroundColor(Color.parseColor(Branding.appTheme.minorInfoSecondaryColor))
            plusIv.setTextColor(Color.parseColor(Branding.appTheme.mainBrandColor))
            minusIv.setTextColor(Color.parseColor(Branding.appTheme.mainBrandColor))
        }
    }
}