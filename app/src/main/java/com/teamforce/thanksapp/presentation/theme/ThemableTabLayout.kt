package com.teamforce.thanksapp.presentation.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.utils.branding.Branding

class ThemableTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TabLayout(context, attrs, defStyleAttr), Themable {

    private var selectedTabPosition: Int = 0

    private var tabTitles: List<String> = emptyList()

    fun setTabTitles(titles: List<String>) {
        tabTitles = titles
        setCustomViewForTabItem()
        applyThemeColors(Branding.brand.colorsJson)
        addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) {
                selectedTabPosition = tab?.position ?: 0
                applyThemeColors(Branding.brand.colorsJson)
            }

            override fun onTabUnselected(tab: Tab?) {
                // Ничего не делаем при отмене выбора
            }

            override fun onTabReselected(tab: Tab?) {
                // Ничего не делаем при повторном выборе
            }
        })
    }

    private fun setFieldCustomTabItem(position: Int): View {
        val v: View = LayoutInflater.from(context)
            .inflate(R.layout.tab_item_for_pager, null)
        val tv = v.findViewById<TextView>(R.id.textViewInTabItem)
        tv.text = tabTitles[position]
        return v
    }

    private fun setCustomViewForTabItem() {
        for (i in 0 until tabCount) {
            val tab: TabLayout.Tab? = getTabAt(i)
            tab?.view?.clipToPadding = false
            tab?.customView = setFieldCustomTabItem(i)
        }
    }

    override fun setThemeColor(theme: ColorsModel) {

    }

    private fun applyThemeColors(theme: ColorsModel) {
        for (i in 0 until tabCount) {
            val tab = getTabAt(i)
            val tabView = tab?.customView as? FrameLayout

            val textColor = Color.parseColor(
                if (i == selectedTabPosition) {
                    theme.generalBackgroundColor
                } else {
                    theme.generalContrastColor
                }
            )

            val backgroundColor = Color.parseColor(
                if (i == selectedTabPosition) {
                    theme.mainBrandColor
                } else {
                    theme.generalBackgroundColor
                }
            )

            tab?.customView?.findViewById<TextView>(R.id.textViewInTabItem)?.setTextColor(textColor)
            tabView?.backgroundTintList = ColorStateList.valueOf(backgroundColor)
        }
    }
}
