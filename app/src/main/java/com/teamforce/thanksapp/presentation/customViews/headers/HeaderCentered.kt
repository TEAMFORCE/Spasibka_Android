package com.teamforce.thanksapp.presentation.customViews.headers

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.teamforce.thanksapp.databinding.HeaderCenteredBinding
import com.teamforce.thanksapp.utils.dp

class HeaderCentered @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialToolbar(context, attrs, defStyleAttr)  {

    private var _binding: HeaderCenteredBinding? = null
    private val binding get() = checkNotNull(_binding) { "Binding is null" }

    init {
        _binding = HeaderCenteredBinding.inflate(LayoutInflater.from(context), this, true)

    }


    fun setupWithRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val currentElevation = binding.toolbar.translationZ
                val targetElevation = if (recyclerView.computeVerticalScrollOffset() > 20.dp) {
                    2.dp.toFloat()
                } else {
                    0.toFloat()
                }

                if (currentElevation != targetElevation) {
                    binding.toolbar.translationZ = targetElevation
                }
            }
        })
    }
}