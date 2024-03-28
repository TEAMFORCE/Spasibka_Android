package com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.benefitDetail

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.entities.benefit.BenefitItemByIdEntity
import com.teamforce.thanksapp.data.entities.benefit.BenefitItemEntity
import com.teamforce.thanksapp.databinding.FragmentBenefitDetailBinding
import com.teamforce.thanksapp.domain.models.benefit.BenefitItemByIdModel
import com.teamforce.thanksapp.domain.models.general.ObjectsComment
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.BenefitConsts.MARKET_ID
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.BenefitConsts.OFFER_ID
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.benefitDetail.ImageSlider.DepthPageTransformer
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.benefitDetail.ImageSlider.ImageSliderAdapter
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.reviews.ReviewsFragment
import com.teamforce.thanksapp.presentation.fragment.reactions.CommentsBottomSheetFragment
import com.teamforce.thanksapp.presentation.viewmodel.benefit.BenefitDetailViewModel
import com.teamforce.thanksapp.presentation.viewmodel.benefit.BenefitListViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Extensions.enableOnClickableLinks
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.branding.Cases
import com.teamforce.thanksapp.utils.glide.setPlaceholderImage
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BenefitDetailMainFragment :
    BaseFragment<FragmentBenefitDetailBinding>(FragmentBenefitDetailBinding::inflate) {


    private val sharedViewModel: BenefitDetailViewModel by activityViewModels()


    private var statusOffer: BenefitItemByIdEntity.OrderStatus? = null


    private var offerId: Int? = null
    private var marketId: Int? = null
    private var page: Int? = null
    private var offerName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            offerId = it.getInt(OFFER_ID)
            marketId = it.getInt(MARKET_ID)
            page = it.getInt(Consts.NEEDED_PAGE_POSITION)
        }

        setFragmentResultListener(CommentsBottomSheetFragment.COMMENTS_REQUEST_KEY) { requestKey, bundle ->
            val result = bundle.getInt(
                CommentsBottomSheetFragment.COMMENTS_AMOUNT_BUNDLE_KEY
            )
            if(result != 0) binding.reactionGroup.setCommentsAmount(result)
        }
    }

    private fun transparentStatusBar() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onResume() {
        super.onResume()
        transparentStatusBar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transparentStatusBar()
        startPostponedEnterTransition()
        if (offerId != null && marketId != null) {
            sharedViewModel.setMarketId(marketId!!)
            sharedViewModel.setOfferId(offerId!!)
            sharedViewModel.loadOffer(offerId!!)
        }
        sharedViewModel.offer.observe(viewLifecycleOwner) { offer ->
            offer?.let {
                offerName = offer.name
                setImage(offer)
                if (offer.categories.isNotEmpty())
                    setTags(binding.chipGroup, offer.categories)

                statusOffer = offer.orderStatus
                initBtnBaseOnStatusOffer(statusOffer!!)
                with(binding) {
                    reactionGroup.setLikesAmount(offer.likes_amount)
                    reactionGroup.setIsLiked(offer.user_liked)
                    reactionGroup.setCommentsAmount(offer.commentsAmount)
                    nameOfferValue.text = offer.name
                    restTv.text = offer.rest
                    boughtAmountTv.text = offer.sold

                    descriptionValue.enableOnClickableLinks()
                    descriptionValue.text = offer.description

                    if (offer.price != null) {
                        priceValue.text = "${offer.price.priceInThanks}"
                    }

                    markedUsersValue.text = offer.sold
                }
            }

        }
        binding.closeBtn.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        listenerError()

        binding.chooseBenefitBtn.setOnClickListener {
            if (statusOffer?.nameOrderStatus == BenefitItemByIdEntity.OrderStatus.C.nameOrderStatus) {
                transactionToShoppingCart()
            } else {
                if (offerId != null && statusOffer != null) {
                    sharedViewModel.addOfferToCart(offerId = offerId!!, 1)
                    changeSkinBtnByClick(statusOffer!!)
                }
            }
        }

        binding.basketBtn.setOnClickListener {
            transactionToShoppingCart()
        }

        binding.reviewsBtn.setOnClickListener {
            if(marketId != null && offerId != null){
                val bundle = Bundle().apply {
                    putInt(ReviewsFragment.MARKET_ID, marketId!!)
                    putInt(ReviewsFragment.OFFER_ID, offerId!!)
                }
                findNavController().navigateSafely(R.id.action_global_reviewsFragment, bundle)
            }else{
                Toast.makeText(requireContext(), "MarketId or OfferId is null", Toast.LENGTH_SHORT).show()
            }

        }

        sharedViewModel.resultAddBenefitToCart.observe(viewLifecycleOwner) {
            if (it != null) statusOffer = BenefitItemByIdEntity.OrderStatus.C
        }

        handleReactions()

    }
    private fun transactionToShoppingCart(){
        val bundle = Bundle()
        bundle.putInt(MARKET_ID, sharedViewModel.getMarketId())
        findNavController().navigateSafely(
            R.id.action_benefitDetailMainFragment_to_shoppingCartFragment,
            bundle
        )
    }

    override fun applyTheme() {
        binding.dotsIndicator.setStrokeDotsIndicatorColor(Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor))
        binding.dotsIndicator.setDotIndicatorColor(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
    }

    private fun listenerError() {
        sharedViewModel.offerError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
    }

    private fun setTags(
        tagsChipGroup: ChipGroup,
        tagList: List<BenefitItemEntity.CategoryInBenefitItem>
    ) {
        tagsChipGroup.removeAllViews()
        for (i in tagList.indices) {
            val tagName = tagList[i].categoryName
            val chip: Chip = LayoutInflater.from(tagsChipGroup.context)
                .inflate(
                    R.layout.chip_tag_example_in_benefit_detail,
                    tagsChipGroup,
                    false
                ) as Chip
            with(chip) {
                text = tagName
                setEnsureMinTouchTargetSize(true)
                minimumWidth = 0
            }
            tagsChipGroup.addView(chip)
        }
    }

    private fun setImage(offer: BenefitItemByIdModel?) {
        if (offer != null && offerId == offer.id) {
            if (!offer.images.isNullOrEmpty()) {
                setSliderOnImage(offer.images)
            } else {
                binding.imageBackground.setBackgroundColor(Color.parseColor(Branding.brand?.colorsJson?.mainBrandColor))
                binding.imageBackground.setPlaceholderImage()
            }
        }
    }

    private fun setSliderOnImage(images: List<BenefitItemEntity.Image>) {
        val imagesLink = mutableListOf<String>()
        images.forEach { image ->
            if (image.link != null && image.link != "null") imagesLink.add(image.link)
        }
        if (imagesLink.size > 1) {
            binding.dotsIndicator.visible()
            binding.bottomInnerShadowView.visible()
        }
        if (imagesLink.isNotEmpty()) {
            val imagePager = ImageSliderAdapter(
                childFragmentManager,
                viewLifecycleOwner.lifecycle,
                imagesLink
            )
            binding.imageSliderVp.apply {
                setPageTransformer(DepthPageTransformer())
                adapter = imagePager
                binding.dotsIndicator.attachTo(this)
            }
        }
    }


    private fun initBtnBaseOnStatusOffer(statusOffer: BenefitItemByIdEntity.OrderStatus) {
        if (statusOffer.nameOrderStatus == BenefitItemByIdEntity.OrderStatus.C.nameOrderStatus) {
            binding.chooseBenefitBtn.setBackgroundColor(Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor))
            binding.chooseBenefitBtn.setTextColor(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
            binding.chooseBenefitBtn.text = requireContext().getString(R.string.inCart)
        } else {
            binding.chooseBenefitBtn.setBackgroundColor(Color.parseColor(Branding.brand.colorsJson.mainBrandColor))
            binding.chooseBenefitBtn.setTextColor(Color.parseColor(Branding.brand.colorsJson.generalBackgroundColor))
            binding.chooseBenefitBtn.text = requireContext().getString(R.string.choose)
        }
    }

    private fun changeSkinBtnByClick(statusOffer: BenefitItemByIdEntity.OrderStatus) {
        if (statusOffer.nameOrderStatus != BenefitItemByIdEntity.OrderStatus.C.nameOrderStatus) {
            animChangeBackgroundBtn(
                colorFrom = Color.parseColor(Branding.brand.colorsJson.mainBrandColor),
                colorTo = Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor)
            )
            animChangeTextColorBtn(
                colorFrom = Color.parseColor(Branding.brand.colorsJson.generalBackgroundColor),
                colorTo = Color.parseColor(Branding.brand.colorsJson.mainBrandColor)
            )
            binding.chooseBenefitBtn.text = requireContext().getString(R.string.inCart)
        }
    }

    private fun animChangeBackgroundBtn(colorFrom: Int, colorTo: Int) {
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 250 // milliseconds

        colorAnimation.addUpdateListener { animator ->
            binding.chooseBenefitBtn.setBackgroundColor(
                animator.animatedValue as Int
            )
        }
        colorAnimation.start()
    }

    private fun animChangeTextColorBtn(colorFrom: Int, colorTo: Int) {
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 250 // milliseconds

        colorAnimation.addUpdateListener { animator ->
            binding.chooseBenefitBtn.setTextColor(
                animator.animatedValue as Int
            )
        }
        colorAnimation.start()
    }


    private fun handleReactions() {
        binding.reactionGroup.onLikeClicked = {
            offerId?.let { offerId -> sharedViewModel.pressLike(offerId) }
        }

        binding.reactionGroup.onLongLikeClicked = {
            offerId?.let {
                val bundle = Bundle().apply {
                    putInt(Consts.LIKE_TO_OBJECT_ID, it)
                    putParcelable(Consts.LIKE_TO_OBJECT_TYPE, ObjectsToLike.OFFER)
                }
                findNavController().navigate(R.id.action_global_reactionsFragment, bundle)
            }
        }

        sharedViewModel.likeResult.observe(viewLifecycleOwner) { likeResult ->
            likeResult?.let {
                binding.reactionGroup.updateLikeData(likeResult.likesAmount)
            }
        }

        binding.reactionGroup.onCommentClicked = {

            offerId?.let {
                val bundle = Bundle().apply {
                    putInt(Consts.OBJECTS_COMMENT_ID, it)
                    putString(CommentsBottomSheetFragment.OBJECTS_COMMENT_NAME, offerName)
                    putParcelable(Consts.OBJECTS_COMMENT_TYPE, ObjectsComment.OFFER)
                }
                findNavController().navigate(R.id.action_global_commentsBottomSheetFragment, bundle)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }


    companion object {


        @JvmStatic
        fun newInstance(offerId: Int, marketId: Int) =
            BenefitDetailMainFragment().apply {
                arguments = Bundle().apply {
                    putInt(OFFER_ID, offerId)
                    putInt(MARKET_ID, marketId)
                }
            }
    }
}