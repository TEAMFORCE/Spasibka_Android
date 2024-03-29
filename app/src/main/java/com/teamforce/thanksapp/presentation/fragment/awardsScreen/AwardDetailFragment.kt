package com.teamforce.thanksapp.presentation.fragment.awardsScreen

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentAwardDetailBinding
import com.teamforce.thanksapp.domain.models.awards.AwardState
import com.teamforce.thanksapp.domain.models.awards.AwardsModel
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.viewmodel.awards.AwardDetailViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.glide.animateToColor
import com.teamforce.thanksapp.utils.glide.setImage
import com.teamforce.thanksapp.utils.glide.setImageWithGraySale
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AwardDetailFragment : BaseFragment<FragmentAwardDetailBinding>(
    FragmentAwardDetailBinding::inflate
) {

    private val viewModel: AwardDetailViewModel by viewModels()
    private var award: AwardsModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            award = it.parceleable(AWARD_DETAIL_OBJECT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (award != null){
            setData(award!!)
            viewModel.awardState.observe(viewLifecycleOwner){ state ->
                when(state){
                    AwardState.RECEIVED -> setReceivedAward(award!!)
                    AwardState.CAN_BE_RECEIVED -> setAvailableAward(award!!)
                    AwardState.NOT_AVAILABLE -> setNotAvailableAward(award!!)
                    AwardState.SET_IN_STATUS ->inStatusAward(award!!)
                    null -> setNotAvailableAward(award!!)
                }
            }
        }
        else {
            // Когда нет данных
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) {
                binding.actionBtn.setEnabled(false)
                binding.progressBar.visible()
                binding.actionBtn.text = ""
            } else {
                binding.progressBar.invisible()
                binding.actionBtn.setEnabled(true)
            }
        }

        viewModel.error.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        binding.closeTv.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        viewModel.gainAward.observe(viewLifecycleOwner){
            saveAwardWithNewIdAfterGain(it.id)
            viewModel.setAwardState(AwardState.RECEIVED)
        }
    }

    private fun saveAwardWithNewIdAfterGain(newId: Long){
        award?.let {
            award = it.copy(id = newId)
        }
    }

    private fun setData(item: AwardsModel){
        if(item.state == AwardState.NOT_AVAILABLE) binding.awardImageCard.setImageWithGraySale(item.cover, R.drawable.default_award)
        else binding.awardImageCard.setImage(item.cover, R.drawable.default_award)
        binding.awardTv.text = item.reward.toString()
        binding.awardDescriptionTv.text = item.description
        binding.titleAwardTv.text = item.name
        item.obtainedAt?.let {
            binding.dateTv.text = parseDateTimeOutputOnlyDate(it, requireContext())
            binding.dateLinear.visible()
        }
        viewModel.setAwardState(item.state)
    }

    private fun setAvailableAward(item: AwardsModel){
        // Показать оранжевый текст Получить
        binding.actionBtn.text = binding.root.context.getString(R.string.awards_receive)
        binding.actionBtn.setOnClickListener{
            gainAward(item)
        }
        // Сделать иконку цветной
        binding.awardImageCard.animateToColor()
        // Скрыть label с галочкой
        binding.awardReceivedCard.invisible()
        // Скрыть дату получения
      //  binding.dateLinear.invisible()

       // binding.shareIv.invisibileWithCustomAnim(R.anim.slide_out_right)


    }
    private fun setReceivedAward(item: AwardsModel){
        // Кнопка установить статус
        binding.actionBtn.text = binding.root.context.getString(R.string.awards_set_as_status)
        binding.actionBtn.setOnClickListener{
            setAsStatus(item)
        }
        // Показ даты получения
     //   binding.dateLinear.visible()
        // Сделать иконку цветной
        binding.awardImageCard.animateToColor()
        // Показать label с галочкой
        binding.awardReceivedCard.visible()

    //    binding.shareIv.visibileWithCustomAnim(R.anim.slide_in_right)

    }

    private fun inStatusAward(item: AwardsModel){
        // Кнопка убрать из статуса статус
        binding.actionBtn.text = binding.root.context.getString(R.string.awards_in_status)
        binding.actionBtn.setOnClickListener(null)
        // Показ даты получения
        //   binding.dateLinear.visible()
        // Сделать иконку цветной
        binding.awardImageCard.animateToColor()
        // Показать label с галочкой
        binding.awardReceivedCard.visible()

        //    binding.shareIv.visibileWithCustomAnim(R.anim.slide_in_right)

    }
    private fun setNotAvailableAward(item: AwardsModel){
        // Кнопка с показом текущего состония выполнения
        binding.actionBtn.text = "${item.currentScores}/${item.targetScores}"
        binding.actionBtn.setOnClickListener{
            bumpAnimation(binding.awardDescriptionTv)
        }
        // Сделать иконку серой
        binding.awardImageCard.setImageWithGraySale(item.cover, R.drawable.default_award)
        // Скрыть label с галочкой
        binding.awardReceivedCard.invisible()

        // Скрыть дату получения
       // binding.dateLinear.invisible()

        binding.shareIv.invisibileWithCustomAnim(R.anim.slide_out_right)

    }

    private fun gainAward(item: AwardsModel){
        viewModel.gainAward(item.id)
    }
    private fun setAsStatus(item: AwardsModel){
        viewModel.setInStatusAward(item.id)
    }

    private fun bumpAnimation(textView: View) {
        val scaleUpX = ObjectAnimator.ofFloat(textView, "scaleX", 1.2f)
        val scaleUpY = ObjectAnimator.ofFloat(textView, "scaleY", 1.2f)
        val scaleDownX = ObjectAnimator.ofFloat(textView, "scaleX", 1.0f)
        val scaleDownY = ObjectAnimator.ofFloat(textView, "scaleY", 1.0f)

        val animatorSet = AnimatorSet()
        animatorSet.play(scaleUpX).with(scaleUpY)
        animatorSet.play(scaleDownX).with(scaleDownY).after(scaleUpX)
        animatorSet.duration = 250
        animatorSet.interpolator = AccelerateDecelerateInterpolator()

        animatorSet.start()
    }



    override fun applyTheme() {

    }


    companion object {
        const val AWARD_DETAIL_OBJECT = "award_detail_object"
        @JvmStatic
        fun newInstance(award: AwardsModel) =
            AwardDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(AWARD_DETAIL_OBJECT, award)
                }
            }
    }
}