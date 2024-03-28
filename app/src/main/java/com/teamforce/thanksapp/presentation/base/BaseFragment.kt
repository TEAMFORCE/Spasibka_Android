package com.teamforce.thanksapp.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme
import java.util.*

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<VB: ViewBinding>(
    private val inflate: Inflate<VB>
) : Fragment() {

    private var _binding: VB? = null
    val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.apply {
          //  setBackgroundColor(Color.parseColor(appTheme.mainBrandColor))
            traverseViews(this)
        }
        applyTheme()
    }

    private fun traverseViews(rootView: View) {
        val viewsStack = Stack<View>()
        viewsStack.push(rootView)

        while (!viewsStack.isEmpty()) {
            val view = viewsStack.pop()
            if (view is Themable) view.setThemeColor(appTheme)

            if (view is ViewGroup) {
                val childCount = view.childCount
                for (i in childCount - 1 downTo 0) {
                    val childView = view.getChildAt(i)
                    viewsStack.push(childView)
                }
            }
        }
    }

    abstract fun applyTheme()
}