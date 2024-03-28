package com.teamforce.thanksapp.presentation.fragment.newTransactionScreen

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.teamforce.photopicker.PhotoPickerFragment
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.response.UserListItem.UserBean
import com.teamforce.thanksapp.databinding.FragmentTransactionSecondBinding
import com.teamforce.thanksapp.model.domain.TagModel
import com.teamforce.thanksapp.presentation.adapter.transactions.AdapterItem
import com.teamforce.thanksapp.presentation.adapter.transactions.ImageAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.customViews.photoSelectionView.toItems
import com.teamforce.thanksapp.presentation.viewmodel.TransactionViewModel
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.Extensions.toMultipart
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme
import com.teamforce.thanksapp.utils.branding.Cases
import com.teamforce.thanksapp.utils.capitalize
import com.teamforce.thanksapp.utils.dp
import com.teamforce.thanksapp.utils.getParcelableExt
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.visible

class TransactionFragmentSecond :
    BaseFragment<FragmentTransactionSecondBinding>(FragmentTransactionSecondBinding::inflate),
    PhotoPickerFragment.Callback {

    private val viewModel: TransactionViewModel by activityViewModels()

    private var listInitTags: List<TagModel> = listOf() // Изначальный списко полученных tags
    var listCheckedIdByOrder = mutableListOf<Int>() // Список id chips выбранных

    // Конечный список id tags который отправиться в запрос
    private var listCheckedIdTags: MutableList<Int> = mutableListOf()

    // Число на которое нужно будет вычесть(порядковый id первого чипа)
    private var numberForSubtraction = 0
    private var user: UserBean? = null
    private var userId: Int? = null
    private var amountThanks = 0

    private val listAdapter: ImageAdapter =
        ImageAdapter(onCrossClicked = ::onCrossClicked, photos = mutableListOf())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getParcelableExt(Consts.PROFILE_DATA, UserBean::class.java)
            userId = it.getInt(TransactionFragment.TRANSACTION_USER_ID)
        }
        // TODO Deprecated API need to replace it with something else
        getActivity()?.getWindow()
            ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.main_linear
            duration = 400.toLong()
            scrimColor = Color.TRANSPARENT
//            setAllContainerColors(requireContext().getColor(R.color.general_background))
        }

        sharedElementReturnTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.transaction_linear
            duration = 400.toLong()
            scrimColor = Color.TRANSPARENT
//            setAllContainerColors(requireContext().getColor(R.color.general_background))
        }
    }

    private fun hideKeyboard() {
        val view: View? = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        handleTopAppBar()
        checkUsersData()
        loadValuesFromDB()
        setChipsForInputField()
        checkedChip()
        userBalance()
        listeners()
        startPostponedEnterTransition()
        binding.sendCoinBtnSticky.setOnClickListener {
            hideKeyboard()
            sendCoins()
        }

        binding.attachStickerBtn.setOnClickListener {
            findNavController().navigateSafely(R.id.action_transactionFragmentSecond_to_stickerPickerFragment)
        }

        binding.detachImgBtn2.setOnClickListener {
            binding.showAttachedImgCard2.invisible()
            // обнуление картинки в viewModel
            viewModel.clearCheckedStickerId()
        }

        binding.attachImageBtn.setOnClickListener {
            callOpenPicker()
        }

        viewModel.checkedSticker.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.showAttachedImgCard2.visible()
                Glide.with(this)
                    .load("${Consts.BASE_URL}${it.image}")
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.image2)
            } else {
                binding.showAttachedImgCard2.invisible()
            }

        }

        initPhotoRecyclerView()
    }

    private fun checkUsersData() {
        if (user != null) {
            setUserData(user!!)
        } else if (userId != null) {
            viewModel.loadUserBean(userId!!)
            viewModel.userBean.observe(viewLifecycleOwner) {
                user = it
                setUserData(it)
            }
        }
    }

    private fun setUserData(user: UserBean) {
        binding.receiver.setUserData("${user.firstname} ${user.surname}", user.tgName)
        binding.receiver.setUserImage(user.photo, "${user.firstname} ${user.surname}", user.tgName)
    }

    private fun sendCoins() {
        val userId = user?.userId ?: -1
        val countText = binding.countValueEt.text.toString()
        val reason = binding.messageValueEt.text.toString()
        val isAnon = binding.isAnon.isChecked
        val isPublic = binding.isPublic.isChecked
        val photos = listAdapter.getItems().filterIsInstance<AdapterItem.File>().map {
            it.uri.toMultipart(requireContext())
        }

        tagsToIdTags()
        if (userId != -1 && countText.isNotEmpty() &&
            (reason.isNotEmpty() || listCheckedIdTags.size > 0)
        ) {
            try {
                val count: Int = Integer.valueOf(countText)
                viewModel.sendCoinsWithImage(
                    userId,
                    count,
                    reason,
                    isAnon,
                    isPublic,
                    photos,
                    listCheckedIdTags,
                    viewModel.checkedSticker.value?.id
                )
                amountThanks = count
                binding.sendCoinBtnSticky.isClickable = false
                binding.sendCoinBtnSticky.isEnabled = false
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        } else {
            val snack = Snackbar.make(
                requireView(),
                requireContext().resources.getString(R.string.unsuccessfulSendCoins),
                Snackbar.LENGTH_LONG
            )
            snack.setTextMaxLines(3)
                .setTextColor(context?.getColor(R.color.white)!!)
                .setAction(context?.getString(R.string.OK)!!) {
                    snack.dismiss()
                }
            snack.show()
        }
    }

    private fun userBalance() {
        binding.shimmerLayout.startShimmer()
        viewModel.loadUserBalance()

        viewModel.balance.observe(viewLifecycleOwner) {
            binding.myCountValueTv.text = "${it.distribute.amount}"
            binding.mySecondCountValueTv.text = "${it.income.amount}"
            binding.myCountCurrencyTv.text =
                Branding.declineCurrency(it.distribute.amount, Cases.GENITIVE).capitalize()
            binding.mySecondCountCurrencyTv.text =
                Branding.declineCurrency(it.income.amount, Cases.GENITIVE).capitalize()
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.invisible()
            binding.shimmerContent.visible()

        }
    }

    private fun loadValuesFromDB() {
        viewModel.loadSendCoinsSettings()
    }

    private fun showConnectionError() {
        binding.error.root.visible()
        binding.sendCoinContent.invisible()
    }

    private fun hideConnectionError() {
        binding.error.root.invisible()
        binding.sendCoinContent.visible()
    }

    private fun setTags(tagList: List<TagModel>) {
        val iconList = listOf(
            R.drawable.tag_icon_clients,
            R.drawable.tag_icon_hat,
            R.drawable.tag_icon_robot,
            R.drawable.tag_icon_shield,
            R.drawable.tag_icon_rocket,
            R.drawable.tag_icon_rocket,
            R.drawable.ic_diamond,
            R.drawable.ic_like,
            R.drawable.tag_icon_chess
        )
        binding.tagsChipGroup.removeAllViews()
        for (i in tagList.indices) {
            val tagName = tagList[i].name
            val chip: Chip = LayoutInflater.from(binding.tagsChipGroup.context)
                .inflate(
                    R.layout.chip_tag_example_transaction_tag,
                    binding.tagsChipGroup,
                    false
                ) as Chip
            with(chip) {
                text = tagName
                setEnsureMinTouchTargetSize(true)
                minimumWidth = 0
                chipIcon = if (iconList.size >= tagList.size)
                    AppCompatResources.getDrawable(requireContext(), iconList[i])
                else
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_diamond)
                chipIconSize = 16f.dp

            }
            binding.tagsChipGroup.addView(chip)
        }
        binding.tagsChipGroup.visible()
        themingChipTags()
    }

    private fun listeners() {
        viewModel.isSuccessOperation.observe(viewLifecycleOwner) {
            if (it) {
                showResultTransaction(amountThanks, user)
                clearCheckedItem()
                viewModel.setSuccessOperationFalse()
            }
        }

        viewModel.sendCoinsError.distinctUntilChanged().observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                clearViewState()
                showSnackBar(it)
            }
        }

        binding.tagsChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            // Сохраняем id чипов(их id начинаются с 29 самый первый чип)
            listCheckedIdByOrder = checkedIds
            numberForSubtraction = group.children.first().id
            Log.d(
                "Token",
                "list of checked chips ${checkedIds} и id первого дочернего элемента ${numberForSubtraction}"
            )
        }
        binding.countValueEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                setDrawableEndColor()
            }
        })
    }

    // Функция для установки цвета drawableEnd
    fun setDrawableEndColor() {
        val color = if (binding.countValueEt.text.isNullOrEmpty()) {
             binding.countValueEt.currentHintTextColor
        } else {
            binding.countValueEt.currentTextColor
        }
        val drawables = binding.countValueEt.compoundDrawablesRelative
        val drawableEnd = drawables[2] // 0 - left, 1 - top, 2 - right, 3 - bottom
        drawableEnd?.mutate()?.setTint(color)
    }

    private fun showSnackBar(text: String){
        val snack = Snackbar.make(
            requireView(),
            text,
            Snackbar.LENGTH_LONG
        )
        snack.setTextMaxLines(3)
            .setTextColor(context?.getColor(R.color.white)!!)
            .setAction(context?.getString(R.string.OK)!!) {
                snack.dismiss()
            }
        snack.show()
    }

    override fun onDestroy() {
        viewModel.clearSendCoinsError()
        super.onDestroy()
    }

    private fun tagsToIdTags() {
        for (i in listCheckedIdByOrder.indices) {
            // Берем из списка id chip который начинается с неизвестного числа и всегда его вычитаем
            // чтобы получить id позицию выбранного в массиве тега где все начинается с 0
            listCheckedIdTags.add(listInitTags[listCheckedIdByOrder[i] - numberForSubtraction].id)
        }
    }

    private fun setChipsForInputField() {
        val idList = listOf(R.id.chipOne, R.id.chipFive, R.id.chipTen, R.id.chipTwentyFive)
        binding.chipGroupThanks.removeAllViews()
        for (i in idList.indices) {
            val chip: Chip = LayoutInflater.from(binding.chipGroupThanks.context)
                .inflate(
                    R.layout.chip_for_transfer_input,
                    binding.chipGroupThanks,
                    false
                ) as Chip
            with(chip) {
                setEnsureMinTouchTargetSize(true)
                text = if (i == 0) "1" else if (i == 1) "5" else if (i == 2) "10" else "25"
                id = idList[i]
                minimumWidth = 0
                chipIcon =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_logo_24x24)
                chipIconSize = 16f.dp
            }
            binding.chipGroupThanks.addView(chip)
        }
        themingChipsForThanks()
        binding.chipGroupThanks.visible()
    }

    private fun checkedChip() {
        binding.chipGroupThanks.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipOne -> {
                    binding.countValueEt.setText("1")
                }

                R.id.chipFive -> {
                    binding.chipFive.apply {
                        binding.countValueEt.setText("5")
                    }
                }

                R.id.chipTen -> {
                    binding.chipTen.apply {
                        binding.countValueEt.setText("10")
                    }
                }

                R.id.chipTwentyFive -> {
                    binding.chipTwentyFive.apply {
                        binding.countValueEt.setText("25")
                    }
                }
            }
        }

        viewModel.settingsTransaction.observe(viewLifecycleOwner) {
            binding.isPublic.isChecked = it.isPublic
            Log.d("Token", " Вывод тегов ${it}")
            if (!it.isAnonymousAvailable) binding.isAnonLinear.invisible()
            listInitTags = it.tags
            setTags(it.tags)
        }
    }

    private fun clearViewState() {
        binding.messageValueEt.setText("")
        binding.countValueEt.setText("")
        binding.sendCoinBtnSticky.isClickable = true
        binding.sendCoinBtnSticky.isEnabled = true

    }

    private fun showResultTransaction(
        amountThanks: Int, user: UserBean?
    ) {
        clearViewState()
        val bundle = Bundle()
        bundle.putInt(TransactionResultDialog.AMOUNT_TRANSFER, amountThanks)
        bundle.putParcelable(TransactionResultDialog.USER_DATA, user)
        findNavController().navigateSafely(
            R.id.action_global_transactionResultDialog,
            bundle,
            OptionsTransaction().optionForResultTransaction
        )
    }

    private fun openPicker(maxSelection: Int, alreadySelected: Int) {
        PhotoPickerFragment.newInstance(
            multiple = true,
            allowCamera = true,
            maxSelection = maxSelection,
            alreadySelected = alreadySelected,
            mainColor = Branding.brand.colorsJson.mainBrandColor
        ).show(childFragmentManager, "photoPicker")
    }

    override fun onImagesPicked(photos: ArrayList<Uri>) {
        val checkedPhotos = photos.toList().toItems(requireContext()).toMutableList()
        listAdapter.updateAdapter(checkedPhotos.toMutableList())
        binding.list.requestLayout()
    }

    private fun callOpenPicker() {
        openPicker(maxSelection = 5, alreadySelected = listAdapter.itemCount)
    }

    private fun initPhotoRecyclerView() {
        val callback: ItemTouchHelper.Callback = ItemMoveCallback(listAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.list)
        binding.list.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
            this.adapter = listAdapter
        }
    }

    private fun onCrossClicked(position: Int) {
        (binding.list.adapter as ImageAdapter).remove(position)
        binding.list.requestLayout()
    }


    override fun onDestroyView() {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onDestroyView()
    }

    private fun clearCheckedItem() {
        viewModel.clearCheckedStickerId()
        listCheckedIdTags = mutableListOf()
    }

    private fun handleTopAppBar() {
        binding.header.toolbar.title = getString(R.string.new_transaction_label)
        binding.header.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.sendCoinContent.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val currentElevation = binding.header.toolbar.translationZ
            val targetElevation = if (scrollY > 20.dp) {
                2.dp.toFloat()
            } else {
                0.toFloat()
            }

            if (currentElevation != targetElevation) {
                binding.header.toolbar.translationZ = targetElevation
            }
        }
    }

    companion object {

        const val SHARED_ANIM_ID = "transfer_receiver"

        @JvmStatic
        fun newInstance(user: UserBean) =
            TransactionFragmentSecond().apply {
                arguments = Bundle().apply {
                    putParcelable(Consts.PROFILE_DATA, user)
                }
            }
    }

    override fun applyTheme() {
        setDrawableEndColor()
        binding.myCountValueTv.setTextColor(Color.parseColor(Branding.brand.colorsJson.generalContrastColor))
        binding.shimmerContent.setCardBackgroundColor(Color.parseColor(Branding.brand.colorsJson.secondaryBrandColor))
    }

    private fun themingChipsForThanks() {
        // Theming Chips for Thanks amount
        val chipThemingBackground = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked),
            ), intArrayOf(

                Color.parseColor(appTheme.secondaryBrandColor),
                Color.parseColor(appTheme.minorInfoSecondaryColor)
            )
        )
        val chipThemingIconColor = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked),
            ), intArrayOf(
                Color.parseColor(appTheme.mainBrandColor),
                Color.parseColor(appTheme.generalContrastSecondaryColor)
            )
        )

        val chipThemingTextColor = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked),
            ), intArrayOf(
                Color.parseColor(appTheme.mainBrandColor),
                Color.parseColor(appTheme.generalContrastSecondaryColor)
            )
        )
        for (child in binding.chipGroupThanks.children) {
            (child as Chip).chipBackgroundColor = chipThemingBackground
            child.chipIconTint = chipThemingIconColor
            child.setTextColor(chipThemingTextColor)
            child.rippleColor = ColorStateList.valueOf(Color.parseColor(appTheme.mainBrandColor))
        }
    }

    private fun themingChipTags() {
        val chipThemingBackground = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked),
            ), intArrayOf(
                Color.parseColor(appTheme.secondaryBrandColor),
                Color.parseColor(appTheme.minorInfoSecondaryColor)
            )
        )
        val chipThemingIconColor = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked),
            ), intArrayOf(
                Color.parseColor(appTheme.mainBrandColor),
                Color.parseColor(appTheme.generalContrastSecondaryColor)
            )
        )

        val chipThemingTextColor = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked),
            ), intArrayOf(
                Color.parseColor(appTheme.mainBrandColor),
                Color.parseColor(appTheme.generalContrastSecondaryColor)
            )
        )
        for (child in binding.tagsChipGroup.children) {
            (child as Chip).chipBackgroundColor = chipThemingBackground
            child.chipIconTint = chipThemingIconColor
            child.setTextColor(chipThemingTextColor)
            child.chipStrokeColor =
                ColorStateList.valueOf(Color.parseColor(appTheme.mainBrandColor))
            child.rippleColor = ColorStateList.valueOf(Color.parseColor(appTheme.mainBrandColor))
        }
    }
}