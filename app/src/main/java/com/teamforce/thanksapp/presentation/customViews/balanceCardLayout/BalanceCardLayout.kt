package com.teamforce.thanksapp.presentation.customViews.balanceCardLayout

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.CustomBalanceCardLayoutBinding
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.utils.branding.Branding

class BalanceCardLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), Themable {

    private var _binding: CustomBalanceCardLayoutBinding? = null
    private val binding get() = checkNotNull(_binding) { "Binding is null" }

    var onMyCountCardClicked: (() -> Unit)? = null
    var onMyRemainsCardClicked: (() -> Unit)? = null


    private var focusedCard: FrameLayout? = null

    init {
        _binding = CustomBalanceCardLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        binding.root.post{
            setInitialFocusCard()
        }
        setupListeners()
    }

    private fun setupListeners() {
        binding.myCount.setOnClickListener {
            handleMyCountCardClicked(it as FrameLayout)
        }

        binding.remains.setOnClickListener {
            handleMyRemainsCardClicked(it as FrameLayout)
        }
    }

    private fun handleMyCountCardClicked(card: FrameLayout){
        if (focusedCard == card) onMyCountCardClicked?.invoke()
        else{
            FrameLayoutAnimationUtil.resetElevationAndScale(binding.remains)
            FrameLayoutAnimationUtil.elevateAndScaleUp(binding.myCount, translationToRight = true)
            focusedCard = binding.myCount
        }
    }

    private fun handleMyRemainsCardClicked(card: FrameLayout){
        if (focusedCard == card) onMyRemainsCardClicked?.invoke()
        else{
            FrameLayoutAnimationUtil.resetElevationAndScale(binding.myCount)
            FrameLayoutAnimationUtil.elevateAndScaleUp(binding.remains, translationToRight = false)
            focusedCard = binding.remains
        }
    }

    fun setMyCountText(text: String) {
        binding.countValueTv.text = text
    }

    fun setRemainsText(text: String) {
        binding.leastCount.text = text

    }

    fun setCancelledAmount(cancelled: String){
        binding.cancelledLabelTv.text =
            String.format(context.getString(R.string.cancelled), cancelled)
    }

    fun setApprovalAmount(approval: String){
        binding.approvalLabelTv.text =
            String.format(context.getString(R.string.frozen_label), approval)
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


//    private fun handleCardClick(card: FrameLayout, position: Int) {
//
//        if (focusedCard == card) {
//            // навигация
//        } else {
//            FrameLayoutAnimationUtil.resetElevationAndScale(binding.myCount)
//            FrameLayoutAnimationUtil.resetElevationAndScale(binding.remains)
//            FrameLayoutAnimationUtil.elevateAndScaleUp(card, translationToRight = card == binding.myCount)
//            focusedCard = card
//        }
//    }

    private fun setInitialFocusCard() {
        focusedCard = binding.myCount
        FrameLayoutAnimationUtil.elevateAndScaleUp(binding.myCount, true)
    }

    private object FrameLayoutAnimationUtil {
        fun elevateAndScaleUp(frameLayout: FrameLayout, translationToRight: Boolean) {
            val animatorSet = AnimatorSet()
            // Анимация изменения elevation
            val elevateAnimator = ObjectAnimator.ofFloat(
                frameLayout,
                "elevation",
                frameLayout.elevation,
                20f
            )
            // Анимация изменения масштаба
            val scaleXAnimator = ObjectAnimator.ofFloat(
                frameLayout,
                "scaleX",
                frameLayout.scaleX,
                1.2f
            )
            val scaleYAnimator = ObjectAnimator.ofFloat(
                frameLayout,
                "scaleY",
                frameLayout.scaleY,
                1.2f
            )

            // Анимация смещения влево
            val translationXAnimator = if (translationToRight) {
                ObjectAnimator.ofFloat(
                    frameLayout,
                    "translationX",
                    frameLayout.translationX,
                    frameLayout.width * 0.2f // Смещение на 10% ширины вправо
                )
            } else {
                ObjectAnimator.ofFloat(
                    frameLayout,
                    "translationX",
                    frameLayout.translationX,
                    -frameLayout.width * 0.2f // Смещение на 10% ширины влево
                )
            }
            animatorSet.playTogether(
                scaleXAnimator,
                scaleYAnimator,
                translationXAnimator,
                elevateAnimator
            )
            animatorSet.duration = 300
            animatorSet.start()
        }

        fun resetElevationAndScale(frameLayout: FrameLayout) {
            val animatorSet = AnimatorSet()

            // Анимация сброса elevation
            val elevateAnimator = ObjectAnimator.ofFloat(
                frameLayout,
                "elevation",
                frameLayout.elevation,
                0f
            )

            // Анимация сброса масштаба
            val scaleXAnimator = ObjectAnimator.ofFloat(
                frameLayout,
                "scaleX",
                frameLayout.scaleX,
                1f
            )
            val scaleYAnimator = ObjectAnimator.ofFloat(
                frameLayout,
                "scaleY",
                frameLayout.scaleY,
                1f
            )

            // Анимация сброса смещения
            val translationXAnimator = ObjectAnimator.ofFloat(
                frameLayout,
                "translationX",
                frameLayout.translationX,
                0f
            )
            animatorSet.playTogether(
                scaleXAnimator,
                scaleYAnimator,
                translationXAnimator,
                elevateAnimator
            )
            animatorSet.duration = 150 // Длительность анимации в миллисекундах
            animatorSet.start()
        }
    }

    override fun setThemeColor(theme: ColorsModel) {
        binding.myCount.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor))

        val gradientDrawable = ResourcesCompat.getDrawable(
            resources,
            R.drawable.background_for_my_remains_card,
            null
        ) as GradientDrawable
        gradientDrawable.apply {
            colors = intArrayOf(
                Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor),
                Color.parseColor(Branding.brand.colorsJson.mainBrandColor)
            )
        }
        binding.remains.background = gradientDrawable
    }
}
