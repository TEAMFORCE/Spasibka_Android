package com.teamforce.thanksapp.presentation.fragment.newTransactionScreen


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.transition.MaterialSharedAxis
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.response.UserListItem.UserBean
import com.teamforce.thanksapp.databinding.FragmentTransactionBinding
import com.teamforce.thanksapp.presentation.adapter.history.HistoryLoadStateAdapter
import com.teamforce.thanksapp.presentation.adapter.transactions.UsersPagingAdapter
import com.teamforce.thanksapp.presentation.base.BaseFragment
import com.teamforce.thanksapp.presentation.viewmodel.TransactionViewModel
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.OptionsTransaction
import com.teamforce.thanksapp.utils.ViewLifecycleDelegate
import com.teamforce.thanksapp.utils.getParcelableExt
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.navigateSafely
import com.teamforce.thanksapp.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class TransactionFragment :
    BaseFragment<FragmentTransactionBinding>(FragmentTransactionBinding::inflate) {


    private val viewModel: TransactionViewModel by activityViewModels()

    private var user: UserBean? = null
    private var userId: Int? = null

    private val listAdapter by ViewLifecycleDelegate {
        UsersPagingAdapter()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getInt(TRANSACTION_USER_ID)
            user = it.getParcelableExt(Consts.PROFILE_DATA, UserBean::class.java) as UserBean?
        }


        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = 400L
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = 400L
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopAppBar()
        if (user != null || userId != null) {
            goToNextFragmentWithoutClickOnUser(user, userId)
            user = null
            userId = null
        }
        initViews()
        dropDownMenuUserInput(binding.usersEt)
    }

    private fun goToNextFragmentWithoutClickOnUser(user: UserBean?, userId: Int?) {
        val bundle = Bundle()
        bundle.putParcelable(Consts.PROFILE_DATA, user)
        userId?.let { bundle.putInt(TRANSACTION_USER_ID, userId) }
        findNavController().navigateSafely(
            R.id.action_transactionFragment_to_transactionFragmentSecond,
            bundle,
            OptionsTransaction().optionForTransaction
        )
    }

    private fun goToNextFragmentWithoutClickOnUser(userId: Int) {
        val bundle = Bundle()
        bundle.putInt(TRANSACTION_USER_ID, userId)
        findNavController().navigateSafely(
            R.id.action_transactionFragment_to_transactionFragmentSecond,
            bundle,
            OptionsTransaction().optionForTransaction
        )
    }


    private fun showConnectionError() {
        binding.error.root.visible()
        binding.chooseMemberContent.invisible()
    }

    private fun hideConnectionError() {
        binding.error.root.invisible()
        binding.chooseMemberContent.visible()
    }


    private fun initViews() {
        binding.usersListRv.adapter = listAdapter.withLoadStateHeaderAndFooter(
            header = HistoryLoadStateAdapter(),
            footer = HistoryLoadStateAdapter()
        )
        viewModel.isLoading.observe(
            viewLifecycleOwner,
            Observer { isLoading ->
                if (isLoading) {
                    //    binding.usersListRv.invisible()
                    binding.progressBar.visible()
                } else {
                    //    binding.usersListRv.visible()
                    binding.progressBar.invisible()
                }

            }
        )
        listAdapter.onSomeonesClicked = { user, clickedView ->
            onSomeonesClicked(user, clickedView)
        }
    }


    @OptIn(FlowPreview::class)
    fun dropDownMenuUserInput(userInput: TextInputEditText) {
        val textChangesFlow = MutableStateFlow("")

        userInput.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                textChangesFlow.value = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {}
        })

        textChangesFlow
            .debounce(200)
            .distinctUntilChanged()
            .onEach { text ->
                loadUserList(text)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun loadUserList(input: String) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.setQuery(input)
            viewModel.users.distinctUntilChanged().collectLatest {
                binding.error.root.invisible()
                listAdapter.submitData(it)
                //  binding.usersListRv.scheduleLayoutAnimation()
            }
        }
    }

    private fun hideKeyboard() {
        val view: View? = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    private fun onSomeonesClicked(user: UserBean, clickedView: View) {
        hideKeyboard()
        val bundle = Bundle().apply {
            putParcelable(Consts.PROFILE_DATA, user)
        }
        val extras =
            FragmentNavigatorExtras(clickedView to TransactionFragmentSecond.SHARED_ANIM_ID)
        findNavController().navigateSafely(
            R.id.action_transactionFragment_to_transactionFragmentSecond,
            bundle,
            null,
            extras
        )
    }

    private fun handleTopAppBar() {
        binding.header.toolbar.title = getString(R.string.new_transaction_label)
        binding.header.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun applyTheme() {

    }

    companion object {
        const val TRANSACTION_USER_ID = "transaction_user_id"
    }

}
