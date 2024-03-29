package com.teamforce.thanksapp.presentation.fragment.eventScreen

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentEventsFilterBinding
import com.teamforce.thanksapp.domain.models.events.EventFilterModel
import com.teamforce.thanksapp.domain.models.events.EventTypeModel
import com.teamforce.thanksapp.presentation.base.BaseBottomSheetDialogFragment
import com.teamforce.thanksapp.presentation.fragment.challenges.createChallenge.SettingsChallengeFragment
import com.teamforce.thanksapp.utils.Extensions.observeOnce
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.getParcelableExt
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EventsFilterFragment :
    BaseBottomSheetDialogFragment<FragmentEventsFilterBinding>(FragmentEventsFilterBinding::inflate) {

    private val viewModel: EventsViewModel by activityViewModels()


    private val filtersIds = mutableSetOf<Int>()
    private val checkBoxes = mutableListOf<CheckBox>()
    private var eventFilterModel: EventFilterModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventFilterModel = it.getParcelableExt(EVENT_FILTER, EventFilterModel::class.java)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventFilterModel?.let {
       //     Log.e("EventsFilterModel", it.eventtypes.filter { it.on == true }.toString())
            setCategories(it.eventtypes)
            updateCheckedState(it.eventtypes)

        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) showProgress()
            else hideProgress()
        }
        binding.continueBtn.setOnClickListener {
            viewModel.updateFilters(filtersIds)
            viewModel.saveFilters()
            this.dismiss()
        }
        binding.resetBtn.setOnClickListener {
            filtersIds.clear()
            viewModel.updateFilters(filtersIds)
            viewModel.saveFilters()
            this.dismiss()
        }

        binding.closeTv.setOnClickListener {
            this.dismiss()
        }
    }

    private fun hideProgress() {
        binding.shimmerLayoutTitle.hideShimmer()
        binding.checkBoxGroup.isClickable = true
    }

    private fun showProgress() {
        binding.shimmerLayoutTitle.showShimmer(true)
        binding.checkBoxGroup.isClickable = false
    }

    private fun setListeners() {
        Log.e("EventFilter", "Изначальный ${filtersIds}")
        checkBoxes.forEach {
            it.setOnCheckedChangeListener { buttonView, isChecked ->
                val btnData = buttonView.tag as EventTypeModel
                if (isChecked) {
                    Log.e("EventFilter", "После ${filtersIds}")
                    filtersIds.addAll(btnData.ids)
                } else {
                    Log.e("EventFilter", "До удаления ${filtersIds}")
                    filtersIds.removeAll(btnData.ids.toSet())
                    Log.e("EventFilter", "После ${filtersIds}")
                }
                Log.e("EventFilter", "FilterIds ${filtersIds}")
            }
        }
    }

    private fun setCategories(tagList: List<EventTypeModel>) {
        binding.checkBoxGroup.removeAllViews()
        filtersIds.clear()
        for (i in tagList.indices) {
            val tagName = tagList[i].name
            val checkBox: CheckBox = LayoutInflater.from(binding.checkBoxGroup.context)
                .inflate(
                    R.layout.item_category_events_filter,
                    binding.checkBoxGroup,
                    false
                ) as CheckBox
            with(checkBox) {
                text = tagName
                minimumWidth = 0
                isChecked = tagList[i].on
                if (tagList[i].on) filtersIds.addAll(tagList[i].ids)
                tag = tagList[i]
            }
            checkBoxes.add(checkBox)
            binding.checkBoxGroup.addView(checkBox)
        }
        themingCheckBox()
        setListeners()
        binding.checkBoxGroup.visible()

    }

    private fun updateCheckedState(tagListFromServer: List<EventTypeModel>) {
        if (tagListFromServer.count() == binding.checkBoxGroup.children.count()) {
            tagListFromServer.forEachIndexed { index: Int, item: EventTypeModel ->
                val child = binding.checkBoxGroup.children.elementAt(index) as CheckBox
                child.isChecked = item.on
            }
        }

    }

    private fun themingCheckBox() {
        val chipThemingBackground = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked),
            ), intArrayOf(
                Color.parseColor(Branding.appTheme.mainBrandColor),
                Color.parseColor(Branding.appTheme.generalContrastColor)
            )
        )

        for (child in binding.checkBoxGroup.children) {
            (child as CheckBox).buttonTintList = chipThemingBackground
        }
    }

    override fun onStop() {
        super.onStop()
        setFragmentResult(
            EVENTS_FILTER_REQUEST_KEY, bundleOf(
                EVENTS_FILTER_BUNDLE_KEY to getEventModelByCheckboxes())
        )

    }

    private fun getEventModelByCheckboxes(): EventFilterModel? {
        val eventTypes = mutableListOf<EventTypeModel>()
        for (checkBox in checkBoxes) {
            val eventTypeModel = checkBox.tag as? EventTypeModel
            eventTypeModel?.let {
                eventTypeModel.on = checkBox.isChecked
                eventTypes.add(eventTypeModel)
            }
        }
      //  Log.e("EventsFilterModelПри отправке", eventTypes.toString())

        return if (eventTypes.isNotEmpty()) {
            EventFilterModel(eventTypes)
        } else {
            null
        }
    }


    override fun applyTheme() {

    }

    companion object {

        const val EVENTS_FILTER_REQUEST_KEY = "events_filter_request_key"
        const val EVENTS_FILTER_BUNDLE_KEY = "events_filter_bundle_key"

        const val EVENT_FILTER = "event_types"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventsFilterFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}