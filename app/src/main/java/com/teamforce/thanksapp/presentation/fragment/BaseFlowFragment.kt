package com.teamforce.thanksapp.presentation.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController

abstract class BaseFlowFragment(
    @LayoutRes layoutId: Int,
    @IdRes private val navHostFragmentId: Int
): Fragment(layoutId) {

    private var navController: NavController? = null

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHostFragment =
            childFragmentManager.findFragmentById(navHostFragmentId) as NavHostFragment
        navController = navHostFragment.findNavController()
        navController?.let {
            handleIntents(it)
            setupNavigation(it)
            onViewCreated(it)
        }
        profilePage()
        navController?.let { showSuggestionToLaunchPeriod(it) }

    }

    override fun onResume() {
        super.onResume()
        navController?.let {
//            if(requireActivity().intent != null){
//                handleIntents(it)
//               // requireActivity().intent = null
//            }
            handleIntents(it)
        }

    }

    protected open fun showSuggestionToLaunchPeriod(navController: NavController){
    }

    protected open fun handleIntents(navController: NavController){
    }

    protected open fun onViewCreated(navController: NavController){

    }

    protected open fun setupNavigation(navController: NavController) {
    }

    protected open fun getBrandTheme(currentOrgId: Int) {
    }

    protected open fun profilePage(){

    }

    companion object {
        private const val TAG = "BaseFlowFragment"
    }

}