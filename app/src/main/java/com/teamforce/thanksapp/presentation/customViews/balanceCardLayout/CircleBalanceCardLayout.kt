package com.teamforce.thanksapp.presentation.customViews.balanceCardLayout

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.CircleBalanceCardLayoutBinding
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.visible


class CircleBalanceCardLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), Themable {

    private var _binding: CircleBalanceCardLayoutBinding? = null
    private val binding get() = checkNotNull(_binding) { "Binding is null" }

    var onMyCountCardClicked: (() -> Unit)? = null
    var onMyRemainsCardClicked: (() -> Unit)? = null


    private var focusedCard: View? = null

    init {
        _binding = CircleBalanceCardLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        binding.root.post{
            setInitialFocusCard()
        }
        setupListeners()
    }

    private fun setupListeners() {
        binding.myCount.setOnClickListener {
            handleMyCountCardClicked(it)
        }

        binding.remains.setOnClickListener {
            handleMyRemainsCardClicked(it)
        }
    }

    private fun handleMyCountCardClicked(card: View){
        if (focusedCard == card) onMyCountCardClicked?.invoke()
        else{
            showMyCountDescr()
            FrameLayoutAnimationUtil.resetElevationAndScale(binding.remains)
            FrameLayoutAnimationUtil.elevateAndScaleUp(binding.myCount)
            focusedCard = binding.myCount
        }
    }

    private fun handleMyRemainsCardClicked(card: View){
        if (focusedCard == card) onMyRemainsCardClicked?.invoke()
        else{
            showRemainsDescr()
            FrameLayoutAnimationUtil.resetElevationAndScale(binding.myCount)
            FrameLayoutAnimationUtil.elevateAndScaleUp(binding.remains)
            focusedCard = binding.remains
        }
    }

    private fun showMyCountDescr(){
        binding.myCountDescr.visible()
        binding.remainsDescr.invisible()
    }

    private fun showRemainsDescr(){
        binding.myCountDescr.invisible()
        binding.remainsDescr.visible()
    }

    fun setMyCountText(text: String) {
        binding.incomeTv.text = text
    }

    fun setRemainsText(text: String) {
        binding.remainsTv.text = text

    }


    fun setDaysBeforeBurn(days: Int){
        binding.willBurnTvText.text = String.format(
            context.getString(R.string.will_burn_after),
            context.resources.getQuantityString(
                R.plurals.plurals_days,
                days,
                days
            )
        )
    }


    private fun setInitialFocusCard() {
        focusedCard = binding.remains
        showRemainsDescr()
        FrameLayoutAnimationUtil.elevateAndScaleUp(binding.remains)
    }

    private object FrameLayoutAnimationUtil {
        fun elevateAndScaleUp(view: View) {
            val animatorSet = AnimatorSet()
            // Анимация изменения масштаба
            val scaleXAnimator = ObjectAnimator.ofFloat(
                view,
                "scaleX",
                view.scaleX,
                1.3f
            )
            val scaleYAnimator = ObjectAnimator.ofFloat(
                view,
                "scaleY",
                view.scaleY,
                1.3f
            )

            animatorSet.playTogether(
                scaleXAnimator,
                scaleYAnimator,
            )
            animatorSet.duration = 300
            animatorSet.start()
        }

        fun resetElevationAndScale(view: View) {
            val animatorSet = AnimatorSet()


            // Анимация сброса масштаба
            val scaleXAnimator = ObjectAnimator.ofFloat(
                view,
                "scaleX",
                view.scaleX,
                1f
            )
            val scaleYAnimator = ObjectAnimator.ofFloat(
                view,
                "scaleY",
                view.scaleY,
                1f
            )

            animatorSet.playTogether(
                scaleXAnimator,
                scaleYAnimator
            )
            animatorSet.duration = 150 // Длительность анимации в миллисекундах
            animatorSet.start()
        }
    }

    override fun setThemeColor(theme: ColorsModel) {
    }
}
