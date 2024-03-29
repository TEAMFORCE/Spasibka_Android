package com.teamforce.thanksapp.presentation.fragment.feedScreen

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.entities.feed.Tag
import com.teamforce.thanksapp.databinding.FragmentAdditionalInfoFeedItemBinding
import com.teamforce.thanksapp.domain.models.feed.FeedItemByIdModel
import com.teamforce.thanksapp.domain.models.general.ObjectsComment
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.presentation.adapter.GeneralViewImageAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.presentation.fragment.reactions.CommentsFragment
import com.teamforce.thanksapp.presentation.viewmodel.feed.AdditionalInfoFeedItemViewModel
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.Consts.TRANSACTION_ID
import com.teamforce.thanksapp.utils.Extensions.PosterOverlayView
import com.teamforce.thanksapp.utils.Extensions.downloadImage
import com.teamforce.thanksapp.utils.Extensions.imagesView
import com.teamforce.thanksapp.utils.Extensions.paintLinks
import com.teamforce.thanksapp.utils.Extensions.showImageWithOpportunityToDownload
import com.teamforce.thanksapp.utils.addBaseUrl
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.dp
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

@AndroidEntryPoint
class AdditionalInfoFeedItemFragment : BaseFragment<FragmentAdditionalInfoFeedItemBinding>(
    FragmentAdditionalInfoFeedItemBinding::inflate
) {

    private val viewModel: AdditionalInfoFeedItemViewModel by viewModels()


    private var userIdReceiver: Int? = null
    private var userIdSender: Int? = null
    private var likesCountReal: Int = 0
    private var isLikedInner: Boolean? = null
    private var transactionId: Int? = null
    private var showComment: Boolean = false
    private var showReaction: Boolean = false
    private var message: String? = null


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                showDialogAboutPermissions()

            }
        }

    private fun showDialogAboutPermissions() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(resources.getString(R.string.explainingAboutPermissions))

            .setNegativeButton(resources.getString(R.string.close)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(resources.getString(R.string.settings)) { dialog, which ->
                dialog.cancel()
                val reqIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .apply {
                        val uri = Uri.fromParts("package", "com.teamforce.thanksapp", null)
                        data = uri
                    }
                startActivity(reqIntent)

            }
            .show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            transactionId = it.getInt(TRANSACTION_ID)
            showComment = it.getBoolean(FEED_ITEM_SHOW_COMMENT)
            showReaction = it.getBoolean(FEED_ITEM_SHOW_REACTION)
            message = it.getString(Consts.MESSSAGE)
        }
        if(showReaction && transactionId != null){
            showReaction(transactionId!!)
            showReaction = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTopBar()
        loadDataFromDb()
        setBaseInfo()
        transactionId?.let { setCommentFragment(it) }
        listeners()
        view.postDelayed({
            handleShowCommArg()
        }, 600)

        scaleImageAnim()
        transactionId?.let { transactionIdNotNull ->
            binding.likesAmountTv.setOnClickListener {
                showReaction(transactionIdNotNull)
            }
        }

    }

    private fun showReaction(transactionId: Int){
        val bundle = Bundle().apply {
            putInt(Consts.LIKE_TO_OBJECT_ID, transactionId)
            putParcelable(Consts.LIKE_TO_OBJECT_TYPE, ObjectsToLike.TRANSACTION)
        }
        findNavController().navigateSafely(R.id.action_global_reactionsFragment, bundle)
    }

    private fun setTags(tagsChipGroup: ChipGroup, tagList: List<Tag>?) {
        tagsChipGroup.removeAllViews()
        tagList?.let {
            for (i in tagList.indices) {
                val tagName = tagList[i].name
                val tvTag: TextView = LayoutInflater.from(tagsChipGroup.context)
                    .inflate(
                        R.layout.text_view_tag_event,
                        tagsChipGroup,
                        false
                    ) as TextView
                with(tvTag) {
                    text = String.format(context.getString(R.string.item_feed_tag), tagName)
                    setTextColor(Color.parseColor(Branding.brand.colorsJson.minorInfoColor))
                }
                tagsChipGroup.addView(tvTag)
            }
        }
    }

    private fun scaleImageAnim(){

        val maxScroll = 128.dp

        binding.scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->

            if (scrollY <= maxScroll) {
                val alpha = 1 - scrollY.toFloat() / maxScroll
                val newSizeAvatar = 128.dp * alpha
                val newSizeIndicator = 43.dp * alpha
                val newTextSize = 16 * alpha

                binding.avatarRelativeLayout.alpha = alpha
                binding.avatarRelativeLayout.layoutParams.width = newSizeAvatar.toInt()
                binding.avatarRelativeLayout.layoutParams.height = newSizeAvatar.toInt()

                binding.indicatorCard.layoutParams.width = newSizeIndicator.toInt()
                binding.indicatorCard.layoutParams.height = newSizeIndicator.toInt()

                binding.gratitudeAmount.textSize = newTextSize
            }
        }
    }

    private fun handleShowCommArg(){
        if(showComment){
            binding.scrollView.fullScroll(View.FOCUS_DOWN)
            showComment = false
        }
    }

    private fun setCommentFragment(transactionId: Int){
        val commentFragment = CommentsFragment.newInstance(
            transactionId,
            ObjectsComment.TRANSACTION
        )

        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.comments_container, commentFragment)
        fragmentTransaction.commit()
    }

    override fun applyTheme() {

    }

    private fun initTopBar() {
        binding.header.filterIv.invisible()
        binding.header.toolbar.title = requireContext().getString(R.string.detailOfTransaction)
        binding.header.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }


    private fun loadDataFromDb() {
        transactionId?.let { viewModel.loadTransactionDetail(it) }
    }

    private fun listeners() {


        binding.likeBtn.setOnClickListener {
            transactionId?.let {
                viewModel.pressLike(it)
                updateOutlookLike()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLodaing ->

        }

    }


    private fun updateOutlookLike() {
        if (isLikedInner != null) {
            isLikedInner = !isLikedInner!!
            likesCountReal = if (isLikedInner!!) likesCountReal + 1  else  likesCountReal - 1
            changeLikeView(isLikedInner!!)
            updateLikesAmount(likesCountReal)
        }
    }

    private fun setBaseInfo() {
        with(binding) {
            viewModel.dataOfTransaction.observe(viewLifecycleOwner) {
                if(it != null){
                    //   dateTransactionTv.text = it?.updated_at
                    descriptionTv.text = it.reason
                    setMessageAboutTransaction()
                    likesAmountTv.text = it.amount.toString()
                    setAvatar(it)
                    userIdReceiver = it.recipient_id
                    userIdSender = it.sender_id
                    setTags(binding.chipGroup, it.tags)
                    setLikes(it.like_amount, it.user_liked)
                    if (it.photos.isNotEmpty()) {
                        initListOfPhotos(it.photos)
                    }else{
                        binding.attachmentsList.invisible()
                        binding.attachmentsHeaderTv.invisible()
                    }
                    handleShowCommArg()

                    // Переход к просмотру фото с скачиванием
                    binding.image.setOnClickListener { view ->
                        it.recipient_photo?.let { photo ->
                            (view as AvatarView).showImageWithOpportunityToDownload(
                                photo,
                                requireContext(),
                                PosterOverlayView(requireContext(), linkImages = mutableListOf(photo)) {
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                            val url = "${Consts.BASE_URL}${photo.replace("_thumb", "")}"
                                            downloadImage(url, requireContext())
                                        } else {
                                            when {
                                                ContextCompat.checkSelfPermission(
                                                    requireContext(),
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                ) == PackageManager.PERMISSION_GRANTED -> {
                                                    val url = "${Consts.BASE_URL}${
                                                        photo.replace(
                                                            "_thumb",
                                                            ""
                                                        )
                                                    }"
                                                    downloadImage(url, requireContext())
                                                }
                                                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                                                    showRequestPermissionRational()
                                                }
                                                else -> {
                                                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
                binding.scrollView.visible()
            }
        }
    }

    private fun initListOfPhotos(listOfPhotos: List<String?>) {
        var listAdapter: GeneralViewImageAdapter? = null
        if (listOfPhotos.isNotEmpty()) {
            binding.attachmentsHeaderTv.visible()
            listAdapter = GeneralViewImageAdapter(photos = listOfPhotos, ::onImageClicked)
        } else {
            binding.attachmentsList.invisible()
            binding.attachmentsHeaderTv.invisible()
        }

        binding.attachmentsList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
            this.adapter = listAdapter
        }
    }

    private fun onImageClicked(listOfPhotos: List<String?>, clickedView: View, position: Int) {
        clickedView.imagesView(
            listOfPhotos,
            requireContext(),
            startPosition = position,
            view = PosterOverlayView(
                requireContext(),
                amountImages = listOfPhotos.size,
                linkImages = listOfPhotos,
                positionClickedImage = position
            ) { photo ->
                lifecycleScope.launch(Dispatchers.Main) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val url = "${Consts.BASE_URL}${photo.replace("_thumb", "")}"
                        downloadImage(url, requireContext())
                    } else {
                        when {
                            ContextCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                val url = "${Consts.BASE_URL}${photo.replace("_thumb", "")}"
                                downloadImage(url, requireContext())
                            }
                            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                                showRequestPermissionRational()
                            }
                            else -> {
                                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                        }
                    }
                }
            }
        )

    }

    private fun setMessageAboutTransaction() {
        message?.let { message ->
            binding.messageTv.movementMethod = LinkMovementMethod.getInstance()
            binding.messageTv.text = HtmlCompat.fromHtml(message.addLineBreak(), HtmlCompat.FROM_HTML_MODE_COMPACT)
            binding.messageTv.paintLinks(Color.parseColor(Branding.brand.colorsJson.generalContrastColor))
        }
    }

    private fun String.addLineBreak(): String {
        val regex = Regex("(от|from)")
        return replace(regex) { matchResult ->
            "<br>${matchResult.value}"
        }
    }


    private fun showRequestPermissionRational() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(resources.getString(R.string.explainingAboutPermissionsRational))

            .setNegativeButton(resources.getString(R.string.close)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(resources.getString(R.string.good)) { dialog, _ ->
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                dialog.cancel()
            }
            .show()
    }


    private fun setAvatar(item: FeedItemByIdModel?) {

        if (item?.recipient_photo != null && !item.recipient_photo.contains("null")) {
            Glide.with(requireContext())
                .load(item.recipient_photo.addBaseUrl())
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.image)
        } else {
            binding.image.avatarInitials = "${item?.recipientName}"
        }
        binding.gratitudeAmount.text = item?.amount.toString()

    }

    private fun setLikes(likes: Int, isLiked: Boolean) {
        likesCountReal = likes
        isLikedInner = isLiked
        updateLikesAmount(likesCountReal)
        val drawableRes = if (isLikedInner == true) R.drawable.ic_like_filled else R.drawable.ic_like
        binding.likeBtn.setImageResource(drawableRes)
    }

    private fun changeLikeView(isLiked: Boolean) {
        val scaleX = 0.8f
        val scaleY = 0.8f
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            binding.likeBtn,
            PropertyValuesHolder.ofFloat("scaleX", scaleX),
            PropertyValuesHolder.ofFloat("scaleY", scaleY)
        )
        animator.duration = 200
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.repeatCount = 1

        animator.start()

        val drawableRes = if (isLiked) R.drawable.ic_like_filled else R.drawable.ic_like
        binding.likeBtn.setImageResource(drawableRes)
    }

    private fun updateLikesAmount(likesAmount: Int){
        binding.likesAmountTv.text = String.format(requireContext().getString(R.string.additional_feed_item_amount_like), likesAmount)
    }

    companion object {
        const val FEED_ITEM_SHOW_COMMENT = "show comment"
        const val FEED_ITEM_SHOW_REACTION = "show reaction"
    }

}