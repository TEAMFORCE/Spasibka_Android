package com.teamforce.thanksapp.presentation.fragment.historyScreen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentAdditionalInfoTransactionBottomSheetBinding
import com.teamforce.thanksapp.domain.models.history.HistoryItemModel
import com.teamforce.thanksapp.presentation.adapter.GeneralViewImageAdapter
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme
import com.teamforce.thanksapp.presentation.viewmodel.history.HistoryViewModel
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Consts.DATE_TRANSACTION
import com.teamforce.thanksapp.utils.Consts.STATUS_TRANSACTION
import com.teamforce.thanksapp.utils.Consts.USERNAME
import com.teamforce.thanksapp.utils.Consts.WE_REFUSED_YOUR_OPERATION
import com.teamforce.thanksapp.utils.Extensions.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdditionalInfoTransactionBottomSheetFragment :
    BaseBottomSheetDialogFragment<FragmentAdditionalInfoTransactionBottomSheetBinding>(
        FragmentAdditionalInfoTransactionBottomSheetBinding::inflate
    ) {


    private val viewModel: HistoryViewModel by viewModels()


    private var dateTransaction: String? = null
    private var status_transaction: String? = null
    private var we_refused_your: String? = null
    private var allDataFromBundle: HistoryItemModel.UserTransactionsModel? = null
    private var message: CharSequence? = null
    private var username: String? = null
    private var transactionId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dateTransaction = it.getString(DATE_TRANSACTION)
            status_transaction = it.getString(STATUS_TRANSACTION)
            we_refused_your = it.getString(WE_REFUSED_YOUR_OPERATION)
            username = it.getString(USERNAME)
            allDataFromBundle = it.getParcelableExt(
                Consts.ALL_DATA,
                HistoryItemModel.UserTransactionsModel::class.java
            ) as HistoryItemModel.UserTransactionsModel?
            message = it.getCharSequence(Consts.MESSSAGE)
            transactionId = it.getInt(Consts.TRANSACTION_ID)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (transactionId != null && allDataFromBundle == null) {
            loadDataFromDb(transactionId!!)
        } else {
            setData(allDataFromBundle!!)
            binding.statusTransaction.text = status_transaction
            binding.dateTransactionTv.text = dateTransaction
        }

        viewModel.allData.observe(viewLifecycleOwner) { allData ->
            if (allData != null) {
                setData(allData)
//                binding.statusTransaction.text = allData.transaction_status.transactionStatus
//                binding.dateTransactionTv.text =
//                    parseDateTimeWithBindToTimeZone(allData.updatedAt, requireContext())
            }
        }

        // Получем уже готовую строку обработанную в адаптере и просто вставляем
        binding.descriptionTransactionYouDo.text = message


    }

    private fun loadDataFromDb(transactionId: Int) {
        viewModel.getTransaction(transactionId)
    }

    private fun setData(allData: HistoryItemModel.UserTransactionsModel) {
        binding.valueTransfer.text = allData.amount
        if (allData.reason.isNullOrEmpty()) {
            binding.reasonTransaction.visibility = View.GONE
            binding.reasonTransactionLabel.visibility = View.GONE
        } else {
            binding.reasonTransaction.enableOnClickableLinks()
            binding.reasonTransaction.visibility = View.VISIBLE
            binding.reasonTransactionLabel.visibility = View.VISIBLE
            binding.reasonTransaction.text = allData.reason
        }
        if (we_refused_your != null) {
            binding.currencyTransaction.visibility = View.GONE
            binding.valueTransfer.visibility = View.GONE
            // Пока что вставлю из стринги для обоих случаев Операция была отменена
            // binding.weOrYouRefused.setText(we_refused_your)
            binding.weOrYouRefused.setText(context?.getString(R.string.operationWasRefused))
            binding.weOrYouRefused.visibility = View.VISIBLE
        }

        defineWhoseAvatarToSet(allData)

        initListOfPhotos(allData)

        if (allData.sticker != null) {
            binding.stickerTv.visible()
            binding.cardViewSticker.visible()
            Glide.with(this)
                .load("${Consts.BASE_URL}${allData.sticker}")
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.senderSticker)

            binding.senderSticker.setOnClickListener { view ->
                (view as ImageView).viewSinglePhoto(allData.sticker, requireContext())
            }

        } else {
            binding.stickerTv.invisible()
            binding.cardViewSticker.invisible()
        }


        binding.descriptionTransactionYouDo.setOnLongClickListener {
            if (allData.sender?.sender_tg_name == username)
                transactionToSomeonesProfile(allData.recipient_id)
            else
                transactionToSomeonesProfile(allData.sender_id)
            return@setOnLongClickListener true
        }
    }

    private fun defineWhoseAvatarToSet(allData: HistoryItemModel.UserTransactionsModel){
        if(allData.iAmSender){
            setAvatar(
                name = allData.recipient?.recipient_first_name,
                surname = allData.recipient?.recipient_surname,
                photo = allData.recipient?.recipient_photo
            )
        }else{
            setAvatar(
                name = allData.sender?.sender_first_name,
                surname = allData.sender?.sender_surname,
                photo = allData.sender?.sender_photo
            )
        }
    }

    private fun setAvatar(name: String?, surname: String?, photo: String?){
        if (!photo.isNullOrEmpty()) {
            Glide.with(this)
                .load(photo.addBaseUrl())
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.userAvatar)
            binding.userAvatar.setOnLongClickListener {
                (it as AvatarView).viewSinglePhoto(
                    photo,
                    requireContext()
                )
                return@setOnLongClickListener true
            }
        } else {
            binding.userAvatar.avatarInitials =
                "$name $surname"
        }
    }

    private fun initListOfPhotos(allData: HistoryItemModel.UserTransactionsModel) {
        var listAdapter: GeneralViewImageAdapter? = null
        if (!allData.photos.isNullOrEmpty()) {
            listAdapter = GeneralViewImageAdapter(photos = allData.photos, ::onImageClicked)
        } else if (!allData.photo.isNullOrEmpty() && allData.photos.isNullOrEmpty()) {
            listAdapter = GeneralViewImageAdapter(photos = listOf(allData.photo), ::onImageClicked)
        } else {
            binding.list.invisible()
            binding.photoTv.invisible()
        }

        binding.list.apply {
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
                // Почему то повторно не запрашивается разрешение
                // requestPermissions()
            }
            .show()
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

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                showDialogAboutPermissions()
            }
        }


    private fun transactionToSomeonesProfile(userId: Int?) {
        val bundle = Bundle()
        if (userId != 0) {
            userId?.let {
                bundle.putInt("userId", it)
                findNavController()
                    .navigate(
                        R.id.action_global_someonesProfileFragment,
                        bundle, OptionsTransaction().optionForProfileFragment
                    )
            }
        }
    }

    companion object {
        const val TAG = "AdditionalInfoTransactionBottomSheetFragment"
    }

    override fun applyTheme() {
        with(binding) {
//            descriptionTransactionWho.setTextColor(Color.parseColor(appTheme.mainBrandColor))
            dateTransactionTv.setTextColor(Color.parseColor(appTheme.generalContrastSecondaryColor))
            dateTransactionTv.setTextColor(Color.parseColor(appTheme.generalContrastSecondaryColor))
            descriptionTransactionYouDo.setTextColor(Color.parseColor(appTheme.generalContrastColor))
//            descriptionTransactionAmountText.setTextColor(Color.parseColor(appTheme.generalContrastColor))
            reasonTransactionLabel.setTextColor(Color.parseColor(appTheme.generalContrastColor))
            reasonTransaction.setTextColor(Color.parseColor(appTheme.generalContrastColor))
            labelStatusTransaction.setTextColor(Color.parseColor(appTheme.generalContrastColor))
            statusTransaction.setTextColor(Color.parseColor(appTheme.generalContrastColor))
            photoTv.setTextColor(Color.parseColor(appTheme.generalContrastColor))
            binding.valueTransfer.setTextColor(Color.parseColor(appTheme.minorSuccessColor))
            binding.currencyTransaction.imageTintList =
                ColorStateList.valueOf(Color.parseColor(appTheme.minorSuccessColor))
        }


    }
}