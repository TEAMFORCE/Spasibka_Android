package com.teamforce.thanksapp.presentation.fragment.employeesScreen

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.FragmentEmployeesBinding
import com.teamforce.thanksapp.presentation.adapter.employees.EmployeesAdapter
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.viewmodel.employees.EmployeesViewModel
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.ViewLifecycleDelegate
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.showSnackBarAboutNetworkProblem
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmployeesFragment: BaseFragment<FragmentEmployeesBinding>(FragmentEmployeesBinding::inflate) {

    private val viewModel: EmployeesViewModel by activityViewModels()

    private val listAdapter by ViewLifecycleDelegate{
        EmployeesAdapter(::onEmployeeClicked)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        setupAdapter(view = view)
        loadEmployees()
        inputFieldObserver()

        binding.header.filterIv.setOnClickListener {
            findNavController().navigate(R.id.action_employeesFragment_to_employeesFilterFragment)
        }
    }

    override fun applyTheme() {
    }

    private fun setupAdapter(view: View){
        binding.list.apply {
            layoutManager = LinearLayoutManager(requireContext())
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
                showConnectionError()

            }
            if (loadState.refresh is LoadState.NotLoading){
                viewModel.setIsEmptyResult(listAdapter.snapshot().isEmpty())
            }
            if(loadState.refresh is LoadState.Loading){
                viewModel.setIsEmptyResult(false)
            }
        }

        viewModel.isEmptyResult.observe(viewLifecycleOwner){
            if(it){
                binding.list.invisible()
                binding.emptyView.root.visible()
            }else{
                binding.list.visible()
                binding.emptyView.root.invisible()
            }
        }

    }

    private fun loadEmployees(){
        viewModel.loadDepartmentTree()
        viewModel.filterMediatorLiveData.observe(viewLifecycleOwner){
            viewLifecycleOwner.lifecycleScope.launch{
                viewModel.getEmployees().flowWithLifecycle(viewLifecycleOwner.lifecycle).collectLatest {
                    listAdapter.submitData(it)
                }
            }
        }

    }

    private fun inputFieldObserver() {
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            // TODO Возможно стоит будет оптимизировать вызов списка пользователей через поле ввода
            // Слышал что существует debounce, но как его сделать с flow не понял
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

            override fun afterTextChanged(s: Editable) {
            }
        })
        if (binding.searchEt.text?.trim().toString().isEmpty()) {
            viewModel.setSearchString(null)
        }
    }

    private fun onEmployeeClicked(employeeId: Int){
        val bundle = Bundle()
        bundle.putInt(Consts.USER_ID, employeeId)
        findNavController().navigate(
            R.id.action_global_someonesProfileFragment,
            bundle,
            OptionsTransaction().optionForAdditionalInfoFeedFragment
        )
    }

    private fun showConnectionError(){
        binding.error.root.visible()
        binding.list.invisible()
    }

    private fun hideConnectionError(){
        binding.error.root.invisible()
        binding.list.visible()
    }


    override fun onDestroyView() {
        binding.list.adapter = null
        super.onDestroyView()
    }

    private fun handleTopAppBar() {
        binding.header.toolbar.title = requireContext().getString(R.string.members_label)
        binding.header.filterIv.visible()
        binding.header.filterIv.setOnClickListener {
            findNavController().navigateSafely(R.id.employeesFilterFragment)
        }
        binding.header.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }




    companion object {

        @JvmStatic
        fun newInstance() =
            EmployeesFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}