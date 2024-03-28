package com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.shoppingCart

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.paging.LoadState
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.entities.benefit.BenefitItemEntity
import com.teamforce.thanksapp.databinding.FragmentShoppingCartBinding
import com.teamforce.thanksapp.presentation.adapter.benefitCafe.ShoppingCartAdapter
import com.teamforce.thanksapp.presentation.adapter.decorators.VericalDividerDecoratorForShoppingList
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.BenefitConsts.MARKET_ID
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.benefitDetail.BenefitDetailMainFragment
import com.teamforce.thanksapp.presentation.viewmodel.benefit.IsCheckedStatus
import com.teamforce.thanksapp.presentation.viewmodel.benefit.ShoppingCartViewModel
import com.teamforce.thanksapp.presentation.viewmodel.benefit.StateShoppingCart
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.Extensions.PosterOverlayView
import com.teamforce.thanksapp.utils.Extensions.downloadImage
import com.teamforce.thanksapp.utils.Extensions.imagesView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShoppingCartFragment :
    BaseFragment<FragmentShoppingCartBinding>(FragmentShoppingCartBinding::inflate) {

    private val viewModel: ShoppingCartViewModel by viewModels()

    private var marketId: Int? = null

    private val listAdapter by ViewLifecycleDelegate {
        ShoppingCartAdapter(
            ::onRefuseOffer,
            ::onCheckBoxOfferClicked,
            ::onCounterOfferClicked,
            ::onImageOfferClicked,
            ::onOfferClicked
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            marketId = it.getInt(MARKET_ID)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        viewModel.setMarketId(marketId!!)
        initRecyclerView()
        loadShoppingCartData()
        loadTotalPrice()

        binding.buyBtn.setOnClickListener {
            if (listAdapter.getCheckedItemCounter() > 0) {
                viewModel.transactionOffersFromCartToOrder()
            } else {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.shopping_cart_no_item_selected),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.moveOffersToOrderError.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Not enough thanks for pay", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        viewModel.refuseOffer.observe(viewLifecycleOwner) {
            loadShoppingCartData()
        }

        viewModel.resultUpdateBenefitToCart.observe(viewLifecycleOwner){
            it?.let {
                listAdapter.updateTotalPrice(it.position, it.quantity, it.price)
            }
            loadTotalPrice()
        }

        viewModel.updateOfferError.observe(viewLifecycleOwner) {
            loadShoppingCartData()
        }

        binding.btnGoToMainPage.setOnClickListener {
            findNavController().navigateSafely(R.id.action_shoppingCartFragment_to_benefitFragment)
        }

        handleState()

        viewModel.totalPrice.observe(viewLifecycleOwner){
            binding.buyBtn.isEnabled = it != 0
            binding.buyBtn.text = "${getString(R.string.getBenefits)} ${it}"
        }
    }

    override fun applyTheme() {

    }

    private fun handleState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                StateShoppingCart.GOOD -> {
                    binding.progressBar.invisible()
                    binding.error.root.invisible()
                    binding.nothing.invisible()
                    binding.benefitsRv.visible()
                    binding.buyBtn.visible()
                    binding.btnGoToMainPage.invisible()
                }

                StateShoppingCart.LOADING -> {
                    binding.progressBar.visible()
                    binding.benefitsRv.visible()
                    binding.buyBtn.visible()
                    binding.error.root.invisible()
                    binding.nothing.invisible()
                    binding.btnGoToMainPage.invisible()
                }

                StateShoppingCart.ERROR -> {
                    binding.error.root.visible()
                    binding.benefitsRv.invisible()
                    binding.buyBtn.invisible()
                    binding.nothing.invisible()
                    binding.btnGoToMainPage.invisible()
                }

                StateShoppingCart.ORDER_SUCCESS -> {
                    binding.progressBar.invisible()
                    binding.error.root.invisible()
                    binding.nothing.invisible()
                    binding.buyBtn.invisible()
                    binding.benefitsRv.invisible()
                    binding.successOrderContent.visible()
                    binding.btnGoToMainPage.visible()
                    // Показывать картинку успешного заверешния
                }

                StateShoppingCart.NOTHING -> {
                    binding.benefitsRv.invisible()
                    binding.buyBtn.invisible()
                    binding.nothing.visible()
                    binding.progressBar.invisible()
                    binding.btnGoToMainPage.invisible()

                }

                null -> {
                    binding.benefitsRv.invisible()
                    binding.buyBtn.invisible()
                    binding.nothing.visible()
                    binding.progressBar.invisible()
                    binding.btnGoToMainPage.invisible()
                }
            }
        }
    }

    private fun initRecyclerView() {
        binding.benefitsRv.apply {
            this.adapter = listAdapter
            setHasFixedSize(true)
            this.adapter = listAdapter.withLoadStateHeaderAndFooter(
                header = HistoryLoadStateAdapter(),
                footer = HistoryLoadStateAdapter()
            )
            this.addItemDecoration(
                VericalDividerDecoratorForShoppingList(
                    8,
                    listAdapter.itemCount
                )
            )
        }

        listAdapter.addLoadStateListener { state ->
            binding.refreshLayout.isRefreshing = state.refresh == LoadState.Loading
        }

        binding.refreshLayout.setOnRefreshListener {
            listAdapter.refresh()
            binding.refreshLayout.isRefreshing = true
        }

        listAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Error) {
                binding.error.root.visible()
                binding.benefitsRv.invisible()
                binding.buyBtn.invisible()
                binding.nothing.invisible()
                binding.btnGoToMainPage.invisible()
            } else {
                binding.progressBar.invisible()
                binding.error.root.invisible()
                binding.nothing.invisible()
                binding.benefitsRv.visible()
                binding.buyBtn.visible()
                binding.btnGoToMainPage.invisible()
            }
        }
    }

    private fun loadShoppingCartData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadOffers().collectLatest {
                listAdapter.submitData(it)
            }
        }
        loadTotalPrice()
    }

    private fun loadTotalPrice() {
        viewModel.loadTotalPrice()
    }

//    private fun setData() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewModel.loadOffers().collectLatest {
//                binding.benefitsRv.visible()
//                listAdapter.submitData(it)
//                binding.benefitsRv.adapter?.notifyDataSetChanged()
//            }
//        }
//    }

    private fun onRefuseOffer(offerId: Int) {
        // Удаление товара из корзины
        viewModel.refuseOffer(offerId)
    }

    private fun onCounterOfferClicked(offerId: Int, amount: Int, position: Int ,isChecked: Boolean) {
        // Сохранение счетчика
        val isCheckedInner = if (isChecked) IsCheckedStatus.CHECKED.definition
        else IsCheckedStatus.UNCHECKED.definition
        viewModel.updateDataOfOffer(offerId = offerId, quantity = amount, position, isCheckedInner)
    }

    private fun onCheckBoxOfferClicked(offerId: Int, amount: Int, position: Int, isChecked: Boolean) {
        // Сохранение выбора конкретного товара
        val isCheckedInner = if (isChecked) IsCheckedStatus.CHECKED.definition
        else IsCheckedStatus.UNCHECKED.definition
        viewModel.updateDataOfOffer(offerId = offerId, quantity = amount, position, isCheckedInner)
    }

    private fun onImageOfferClicked(images: List<BenefitItemEntity.Image>, view: View) {
        val imagesLink = mutableListOf<String>()
        images.forEach {
            it.link?.let { link -> imagesLink.add(link) }
        }
        view.imagesView(
            imagesLink,
            requireContext(),
            PosterOverlayView(
                requireContext(),
                amountImages = images.size,
                linkImages = imagesLink
            ) { photo ->
                lifecycleScope.launch(Dispatchers.Main) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val url = "${Consts.BASE_URL}${photo.replace("_thumb", "")}"
                        downloadImage(url, requireContext())
                    } else {
                        when {
                            ContextCompat.checkSelfPermission(
                                requireContext(),
                                WRITE_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                val url = "${Consts.BASE_URL}${photo.replace("_thumb", "")}"
                                downloadImage(url, requireContext())
                            }

                            shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE) -> {
                                showRequestPermissionRational()
                            }

                            else -> {
                                requestPermissionLauncher.launch(WRITE_EXTERNAL_STORAGE)
                            }
                        }
                    }
                }
            }
        )

    }

    private fun onOfferClicked(offerId: Int) {
        val bundle = Bundle()
        bundle.putInt(OFFER_ID, offerId)
        bundle.putInt(MARKET_ID, viewModel.getMarketId())
        findNavController().navigateSafely(
            R.id.action_shoppingCartFragment_to_benefitDetailMainFragment,
            bundle
        )
    }

    private fun handleTopAppBar() {
        binding.header.toolbar.title = requireContext().getString(R.string.shoppingCart)
        binding.header.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.header.filterIv.invisible()
    }


    companion object {
        private const val OFFER_ID = "offer_id"

        @JvmStatic
        fun newInstance() =
            ShoppingCartFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }


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
                requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                dialog.cancel()
            }
            .show()
    }
}