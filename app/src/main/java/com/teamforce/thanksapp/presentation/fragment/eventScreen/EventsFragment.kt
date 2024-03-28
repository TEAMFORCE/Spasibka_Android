package com.teamforce.thanksapp.presentation.fragment.eventScreen

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentEventsBinding
import com.teamforce.thanksapp.domain.models.events.EventFilterModel
import com.teamforce.thanksapp.domain.models.events.EventTypeModel
import com.teamforce.thanksapp.presentation.adapter.feed.EventsAdapter
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.fragment.feedScreen.AdditionalInfoFeedItemFragment
import com.teamforce.thanksapp.presentation.fragment.newTransactionScreen.TransactionFragment
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.ViewLifecycleDelegate
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.parceleable
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged


@AndroidEntryPoint
class EventsFragment : BaseFragment<FragmentEventsBinding>(FragmentEventsBinding::inflate) {

    private val viewModel: EventsViewModel by activityViewModels()


    private val filtersIds = mutableSetOf<Int>()
    private var eventFilterModel: EventFilterModel? = null


    private val listAdapter by ViewLifecycleDelegate {
        EventsAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }


        setFragmentResultListener(EventsFilterFragment.EVENTS_FILTER_REQUEST_KEY) { requestKey, bundle ->
            val result = bundle.parceleable<EventFilterModel>(
                EventsFilterFragment.EVENTS_FILTER_BUNDLE_KEY
            )
            result?.let {
                // Log.e("EventsFragmentsModel", it.eventtypes.filter { it.on == true }.toString())
                eventFilterModel = it
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        setAdapter()
        loadData()
        listenersCategories()
        binding.header.filterIv.setOnClickListener {
            //  Log.e("EventsFragmentsModelПри открытии фильтра", eventFilterModel.toString())
            val bundle = Bundle().apply {
                putParcelable(EventsFilterFragment.EVENT_FILTER, eventFilterModel)
            }
            findNavController().navigateSafely(
                R.id.action_eventsFragment_to_eventsFilterFragment,
                bundle
            )
        }
        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
    }


    private fun listenersCategories() {
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            scrollingChipGroup(requireActivity().findViewById(checkedIds.last()))
            filtersIds.clear()
            checkedIds.forEach {
                val selectedChip = requireActivity().findViewById<CheckBox>(it)
                val selectedEventType = selectedChip.tag as EventTypeModel
                setVisibilityForFilterButton(selectedEventType.showFilter)
                filtersIds.addAll(selectedEventType.ids)
            }
            viewModel.updateFilters(filtersIds)
        }
    }

    private fun restoreCheckedChip(checkedIds: Set<Int>?){
        if (checkedIds?.count() == 1){
            for (child in binding.chipGroup.children) {
                val childChip = child as Chip
                val eventType = (childChip.tag as? EventTypeModel)
                childChip.isChecked = (eventType?.ids?.count() == 1 && eventType.ids.first() == checkedIds.first())
            }
        }

    }

    private fun setVisibilityForFilterButton(showFilterButton: Boolean) {
        if (showFilterButton) {
            binding.header.filterIv.visible()
        } else {
            binding.header.filterIv.invisible()
        }
    }

    private fun scrollingChipGroup(selectedChip: Chip) {
        val selectedChipPosition = selectedChip.left + selectedChip.width / 2
        val scrollViewWidth = binding.scroll.width

        // Calculate the scroll amount based on the selected chip's position
        val scrollAmount = selectedChipPosition - scrollViewWidth / 2

        // Scroll the HorizontalScrollView to the desired position
        binding.scroll.smoothScrollTo(scrollAmount, 0)

    }

    private fun loadData() {
        lifecycleScope.launchWhenStarted {
            viewModel.filters.distinctUntilChanged().collectLatest {
                eventFilterModel = it
                val filterModel = addSpecialFirstCategoryNameIsAll(it)
                if (filterModel != null) {
                    setCategories(filterModel.eventtypes)
                    viewModel.updateFilters(filtersIds)
                }
            }
        }

        viewModel.filtersIds.observe(viewLifecycleOwner) { filtersIds ->
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                viewModel.getEvents().collectLatest {
                    restoreCheckedChip(filtersIds)
                    listAdapter.submitData(it)
                }
            }
        }
        if (viewModel.filtersIds.value.isNullOrEmpty()) {
            viewModel.saveFilterStatus.observe(viewLifecycleOwner) {
                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                    viewModel.getEvents().collectLatest {
                        listAdapter.submitData(it)
                    }
                }
            }
        }

        listAdapter.addLoadStateListener { state ->
            binding.refreshLayout.isRefreshing = state.refresh == LoadState.Loading
        }

        binding.refreshLayout.setOnRefreshListener {
            listAdapter.refresh()
            binding.refreshLayout.isRefreshing = true
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) showProgress()
            else hideProgress()
        }
    }

    private fun addSpecialFirstCategoryNameIsAll(from: EventFilterModel?): EventFilterModel? {
        val idsForCustomCategory = mutableListOf<Int>()
        from?.eventtypes?.forEach {
            if (it.on) idsForCustomCategory.addAll(it.ids)
        }
        val categories = mutableListOf(
            EventTypeModel(
                ids = idsForCustomCategory,
                name = requireContext().getString(R.string.events_filter_custom),
                on = false,
                showFilter = true
            )
        )
        from?.let { categories.addAll(it.eventtypes) }
        return from?.copy(eventtypes = categories)
    }

    private fun showProgress() {
        binding.chipGroup.invisible()
        binding.shimmerLayout.visible()
        binding.shimmerLayout.startShimmer()
    }

    private fun hideProgress() {
        binding.shimmerLayout.invisible()
        binding.shimmerLayout.stopShimmer()
        binding.chipGroup.visible()
    }

    private fun setAdapter() {
        binding.list.apply {
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            this.adapter = listAdapter.withLoadStateHeaderAndFooter(
                header = HistoryLoadStateAdapter(),
                footer = HistoryLoadStateAdapter()
            )
//            this.addItemDecoration(
//                VerticalDividerDecoratorForListWithBottomBar(
//                    8,
//                    listAdapter.itemCount
//                )
//            )
        }

        listAdapter.onTransactionClicked = { transactionId, message ->
            val bundle = Bundle()
            bundle.putInt(Consts.TRANSACTION_ID, transactionId)
            bundle.putString(Consts.MESSSAGE, message)
            findNavController()
                .navigateSafely(
                    R.id.action_eventsFragment_to_additionalInfoFeedItemFragment,
                    bundle
                )
        }

        listAdapter.onLikeClicked = { objectId, typeOfObject, position ->
            viewModel.pressLike(objectId, typeOfObject, position)
            viewModel.likeResult.observe(viewLifecycleOwner) {
                it?.let { likeResponse ->
                    listAdapter.updateLikesCount(
                        likeResponse.position,
                        likeResponse.likesAmount,
                        likeResponse.isLiked
                    )
                }
            }
        }


        listAdapter.onCommentClicked = { transactionId: Int, message ->
            val bundle = Bundle()
            bundle.putInt(Consts.TRANSACTION_ID, transactionId)
            bundle.putCharSequence(Consts.MESSSAGE, message)
            bundle.putBoolean(AdditionalInfoFeedItemFragment.FEED_ITEM_SHOW_COMMENT, true)
            findNavController()
                .navigateSafely(
                    R.id.action_eventsFragment_to_additionalInfoFeedItemFragment,
                    bundle
                )
        }

        listAdapter.onCelebrateSomeone = { userId: Int ->
            val bundle = Bundle().apply {
                putInt(TransactionFragment.TRANSACTION_USER_ID, userId)
            }
            findNavController().navigateSafely(R.id.action_global_transaction_graph, bundle, OptionsTransaction().optionForTransaction)
        }
        listAdapter.onLikeLongClicked = { itemId, objectType ->
            val bundle = Bundle().apply {
                putInt(Consts.LIKE_TO_OBJECT_ID, itemId)
                putParcelable(Consts.LIKE_TO_OBJECT_TYPE, objectType)
            }
            findNavController().navigateSafely(R.id.action_global_reactionsFragment, bundle)

        }
    }

    private fun setCategories(tagList: List<EventTypeModel>) {
        binding.chipGroup.removeAllViews()
        for (i in tagList.indices) {
            val tagName = tagList[i].name
            val chip: Chip = LayoutInflater.from(binding.chipGroup.context)
                .inflate(
                    R.layout.chip_category_events,
                    binding.chipGroup,
                    false
                ) as Chip
            with(chip) {
                text = tagName
                setEnsureMinTouchTargetSize(true)
                minimumWidth = 0
                tag = tagList[i]
                isChecked = i == 0
            }
            binding.chipGroup.addView(chip)
        }
        themingChips()
        binding.chipGroup.visible()

    }

    private fun themingChips() {
        val chipThemingBackground = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked),
            ), intArrayOf(
                Color.parseColor(Branding.appTheme.mainBrandColor),
                Color.parseColor(Branding.appTheme.generalBackgroundColor)
            )
        )

        val chipThemingTextColor = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked),
            ), intArrayOf(
                Color.parseColor(Branding.appTheme.generalBackgroundColor),
                Color.parseColor(Branding.appTheme.generalContrastColor)
            )
        )
        for (child in binding.chipGroup.children) {
            (child as Chip).chipBackgroundColor = chipThemingBackground
            child.setTextColor(chipThemingTextColor)
            child.chipStrokeColor =
                ColorStateList.valueOf(Color.parseColor(Branding.appTheme.midpoint))
            child.rippleColor =
                ColorStateList.valueOf(Color.parseColor(Branding.appTheme.mainBrandColor))
        }
    }

    private fun handleTopAppBar() {
        binding.header.toolbar.title = requireContext().getString(R.string.feed_label)
        binding.header.filterIv.visible()
        binding.header.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }



    override fun applyTheme() {

    }

    companion object {

        @JvmStatic
        fun newInstance() =
            EventsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}