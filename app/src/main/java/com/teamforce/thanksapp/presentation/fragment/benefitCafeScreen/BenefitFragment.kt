package com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.entities.benefit.Category
import com.teamforce.thanksapp.databinding.FragmentBenefitBinding
import com.teamforce.thanksapp.presentation.adapter.benefitCafe.BenefitItemAdapter
import com.teamforce.thanksapp.presentation.adapter.decorators.VerticalDividerDecoratorForListWithBottomBar
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.benefitCafeScreen.BenefitConsts.MARKET_ID
import com.teamforce.thanksapp.presentation.viewmodel.benefit.BenefitListViewModel
import com.teamforce.thanksapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BenefitFragment : BaseFragment<FragmentBenefitBinding>(FragmentBenefitBinding::inflate){

    private var mediator: TabLayoutMediator? = null
    private val viewModel: BenefitListViewModel by activityViewModels()


    private val listAdapter by ViewLifecycleDelegate{
        BenefitItemAdapter(::onOfferClicked)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
             viewModel.setCurrentMarketId(it.getInt(MARKET_ID))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        loadAvailableMarkets()

        setupAdapter(view)
        loadOffers()



    }

    private fun setupAdapter(view: View){
        binding.list.apply {
            setHasFixedSize(true)
            this.adapter = listAdapter.withLoadStateHeaderAndFooter(
                header = HistoryLoadStateAdapter(),
                footer = HistoryLoadStateAdapter()
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
                showSnackBarAboutNetworkProblem(
                    view,
                    requireContext()
                )

            }
        }

    }

    private fun loadOffers(){
        viewModel.filterMediatorLiveData.distinctUntilChanged().observe(viewLifecycleOwner) {
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.getOffers()?.collectLatest {
                    listAdapter.submitData(it)
                }
            }
        }

    }



    private fun onOfferClicked(offerId: Int){
        val bundle = Bundle()
        bundle.apply {
            putInt(BenefitConsts.OFFER_ID, offerId)
            // We can't click on it when marketId is null
            putInt(MARKET_ID, viewModel.currentMarketId.value!!)
        }
        findNavController().navigateSafely(R.id.action_benefitFragment_to_benefitDetailMainFragment,
            bundle, OptionsTransaction().optionForTransactionWithSaveBackStack)
    }

    override fun applyTheme() {

    }

    private fun loadAvailableMarkets() {
        if(viewModel.listOfAvailableMarkets.value.isNullOrEmpty()){
            viewModel.loadAvailableMarkets()
            viewModel.listOfAvailableMarkets.distinctUntilChanged().observe(viewLifecycleOwner) {
                if (it.isNullOrEmpty()) {
                    showWarning()
                } else {
                    viewModel.setCurrentMarketId(it[0].id)
                    hideWarning()
                    loadCategories()
                    inputFieldObserver()
                }
            }
        }

    }

    private fun showWarning() {
        with(binding) {
          //  binding.list.invisible()
            noData.visible()
        }
    }

    private fun hideWarning() {
        with(binding) {
         //   binding.list.visible()
            noData.invisible()
        }
    }

    private fun loadCategories() {

        viewModel.loadCategories()

        viewModel.categories.observe(viewLifecycleOwner) {
            if (it != null) {
                val listOfCategoryInner: MutableList<Category> = it.toMutableList()
                listOfCategoryInner.add(
                    0,
                    Category(0, requireContext().getString(R.string.allEvent))
                )
            }
        }
    }

    private fun tabsObserver() {
        viewModel.checkedIdCategory.observe(viewLifecycleOwner) { page ->
            // TODO Обновление списка по нужной категории
        }
    }

    private fun inputFieldObserver() {
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            // TODO Возможно стоит будет оптимизировать вызов списка товаров
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.trim().isNotEmpty())
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.setSearchString(s.trim().toString())
                    }
                else
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.setSearchString(null)
                    }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {}
        })
        if (binding.searchEt.text?.trim().toString().isEmpty()) {
            viewModel.setSearchString(null)
        }
    }


    override fun onDestroyView() {
        mediator?.detach()
        mediator = null
        super.onDestroyView()
    }

    private fun handleTopAppBar() {
        binding.header.toolbar.title = requireContext().getString(R.string.benefit_cafe_label)
        binding.header.benefitLinear.visible()
        binding.header.filterIv.setOnClickListener {
            findNavController().navigateSafely(R.id.action_benefitFragment_to_filtersBottomSheetDialogFragment)
        }

        binding.header.historyBtn.setOnClickListener {
            viewModel.currentMarketId.value?.let { marketId ->
                val bundle = Bundle()
                bundle.putInt(MARKET_ID, marketId)
                findNavController().navigateSafely(
                    R.id.action_benefitFragment_to_historyOfOrdersFragment,
                    bundle
                )
            }
        }

        binding.header.basketBtn.setOnClickListener {
            viewModel.currentMarketId.value?.let { marketId ->
                val bundle = Bundle()
                bundle.putInt(MARKET_ID, marketId)
                findNavController().navigateSafely(
                    R.id.action_benefitFragment_to_shoppingCartFragment,
                    bundle
                )
            }
        }
    }


    companion object {

        @JvmStatic
        fun newInstance() =
            BenefitFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

}