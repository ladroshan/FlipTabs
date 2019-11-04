package com.jem.fliptabs

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.fliptab.view.*


class FlipTab : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    private var isLeftSelected: Boolean = true
    private var animationMiddleViewFlippedFlag: Boolean = false

    private val leftTabText get() = tab_left.text.toString()
    private val rightTabText get() = tab_right.text.toString()

    private var tabSelectedListener: TabSelectedListener? = null

    private val leftSelectedDrawable by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            resources.getDrawable(R.drawable.tab_left_selected, null)
        } else {
            resources.getDrawable(R.drawable.tab_left_selected)
        }
    }
    private val rightSelectedDrawable by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            resources.getDrawable(R.drawable.tab_right_selected, null)
        } else {
            resources.getDrawable(R.drawable.tab_right_selected)
        }
    }

    init {
        inflate(context, R.layout.fliptab, this)
        tab_left.setOnClickListener {
            if (!isLeftSelected) {
                flipTabs()
            } else {
                tabSelectedListener?.onTabReselected(isLeftSelected, leftTabText)
            }
        }
        tab_right.setOnClickListener {
            if (isLeftSelected) {
                flipTabs()
            } else {
                tabSelectedListener?.onTabReselected(isLeftSelected, rightTabText)
            }
        }
        clipChildren = false
        clipToPadding = false
    }

    private fun flipTabs() {
        tab_selected_container.animate()
            .rotationYBy(if (isLeftSelected) 180f else -180f)
            .setDuration(500)
            .withStartAction {
                (parent as ViewGroup?)?.clipChildren = false
                (parent as ViewGroup?)?.clipToPadding = false
                tabSelectedListener?.onTabSelected(
                    !isLeftSelected,
                    if (isLeftSelected) rightTabText else leftTabText
                )
            }
            .setUpdateListener {
                if (!animationMiddleViewFlippedFlag && it.animatedFraction > 0.5) {
                    animationMiddleViewFlippedFlag = true
                    //TODO: Find out a better alternative to changing Background in the middle of animation (might result in dropped frame/stutter)
                    if (isLeftSelected) {
                        tab_selected.text = rightTabText
                        tab_selected.background = rightSelectedDrawable
                        tab_selected.scaleX = -1f
                    } else {
                        tab_selected.text = leftTabText
                        tab_selected.background = leftSelectedDrawable
                        tab_selected.scaleX = 1f
                    }
                }
            }
            .withEndAction {
                base_fliptab_container.animate()
                    .rotationYBy(if (isLeftSelected) 3f else -3f)
                    .setDuration(75)
                    .withEndAction {
                        base_fliptab_container.animate()
                            .rotationYBy(if (isLeftSelected) -3f else 3f)
                            .setDuration(75)
                            .withEndAction {
                                isLeftSelected = !isLeftSelected
                                animationInProgress = false
                                (parent as ViewGroup).clipChildren = true
                                (parent as ViewGroup).clipToPadding = true
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }

    public interface TabSelectedListener {
        public fun onTabSelected(isLeftTab: Boolean, tabTextValue: String): Unit
        public fun onTabReselected(isLeftTab: Boolean, tabTextValue: String): Unit
    }

    public fun setTabSelectedListener(tabSelectedListener: TabSelectedListener) {
        this.tabSelectedListener = tabSelectedListener
    }
}