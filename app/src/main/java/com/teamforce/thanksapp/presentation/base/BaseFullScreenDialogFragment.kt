package com.teamforce.thanksapp.presentation.base

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.utils.branding.Branding
import java.util.*


abstract class BaseFullScreenDialogFragment<VB : ViewBinding>(
    private val inflate: Inflate<VB>
) : DialogFragment() {

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

    override fun getTheme(): Int = R.style.DialogTheme_ShowAsACommonFragment

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        dialog?.window?.statusBarColor = Color.parseColor(Branding.brand.colorsJson.mainBrandColor)
        applyTheme()
        binding.root.apply {
            traverseViews(this)
        }
    }

    private fun traverseViews(rootView: View) {
        val viewsStack = Stack<View>()
        viewsStack.push(rootView)

        while (!viewsStack.isEmpty()) {
            val view = viewsStack.pop()
            if (view is Themable) view.setThemeColor(Branding.appTheme)

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