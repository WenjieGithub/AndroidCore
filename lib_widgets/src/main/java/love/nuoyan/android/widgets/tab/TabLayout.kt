package love.nuoyan.android.widgets.tab

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.annotation.Px
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.ScrollState

// 一款自定义下划线的仿 TabLayout
class TabLayout: HorizontalScrollView {
    companion object {
        fun bind(viewPager: ViewPager2, tabLayout: TabLayout) {
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(position: Int, positionOffset: Float, @Px positionOffsetPixels: Int) {
                    tabLayout.onPageScrolled(position, positionOffset)
                }
                override fun onPageSelected(position: Int) {
                    tabLayout.onPageSelected(position)
                }
                override fun onPageScrollStateChanged(@ScrollState state: Int) {
                    tabLayout.onPageScrollStateChanged(state)
                }
            })
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var mTabAdapter: TabAdapter? = null
    private val mTabContainer: LinearLayout
    private val mTabItemList = arrayListOf<TabItem>()

    private var mLastScrollX = 0                                    // 记录一下滑动的相对距离，如果跟最新的一样的话就不做操作
    private var mCurrentTabPosition = 0                             // 当前选中的标签
    private var mCurrentScrollTabPosition = 0                       // 当前滚动的标签
    private var mCurrentScrollTabPositionOffset = 0f                // 当前滚动的标签偏移量

    // 给 tabView 默认的 LayoutParams, 如果子 View 是默认填充满布局的，这时就要设置weight = 1；
    private val tabLayoutParams: LinearLayout.LayoutParams
        get() = if (tabFillContainer) {
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        } else {
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

    private val mObserver = object : DataSetObserver() {
        override fun onChanged() {
            initTab()
        }
    }

    var tabScrollGravity = 1                                        // 选中tab滚动后位于容器的  0: 左  1: 中  2: 右
        set(value) {
            field = value
            scrollToCurrentTab()
        }
    var tabFillContainer = true                                     // 标签是否填充容器
        set(value) {
            field = value
            for (item in mTabItemList) {
                item.itemView.layoutParams = tabLayoutParams
            }
            requestLayout()
        }
    var tabContainerGravity = Gravity.START                         // 标签容器位置
        set(value) {
            field = value
            mTabContainer.gravity = value
            requestLayout()
        }

    var indicatorCorner = 0f                                        // 指示器圆角
        set(value) {
            if (value >= 0) {
                (indicatorDrawable as? GradientDrawable)?.let { it.cornerRadius = value }
            }
            field = value
            invalidate()
        }
    var indicatorColor = Color.BLACK                                // 指示器颜色
        set(value) {
            (indicatorDrawable as? GradientDrawable)?.setColor(value)
            field = value
            invalidate()
        }
    var indicatorHeight = 1                                         // 指示器高度
        set(value) {
            field = value
            invalidate()
        }
    var indicatorWidth = 0                                          // 指示器宽度, 大于 0 时为固定宽度
        set(value) {
            field = value
            invalidate()
        }
    var indicatorBottomMargin = 0                                   // 指示器底部边距
        set(value) {
            field = value
            invalidate()
        }
    var indicatorLeftMargin = 0                                     // 指示器左边边距
        set(value) {
            field = value
            invalidate()
        }
    var indicatorRightMargin = 0                                    // 指示器右边边距
        set(value) {
            field = value
            invalidate()
        }
    var indicatorDrawable: Drawable = GradientDrawable().apply { setColor(indicatorColor) }   // 指示器Drawable
        set(value) {
            field = value
            invalidate()
        }
    val indicatorRect = Rect()                                      // 指示器矩形

    init {
        // 初始化 ScrollView
        isHorizontalScrollBarEnabled = false
        isFillViewport = true       // 设置滚动视图是否可以伸缩其内容以填充视口
        setWillNotDraw(false)       // 重写onDraw方法, 需要调用这个方法来清除 flag, 要不然不会执行重写的onDraw()
        clipChildren = false
        clipToPadding = false
        // 添加 Tab 容器
        mTabContainer = LinearLayout(context)
        addView(mTabContainer)
    }

    fun getAdapter(): TabAdapter? {
        return mTabAdapter
    }
    fun setAdapter(adapter: TabAdapter) {
        mTabAdapter?.unregisterObserver(mObserver)
        mTabAdapter = adapter
        mTabAdapter?.registerObserver(mObserver)
        initTab()
    }
    fun initTab() {
        mTabAdapter?.let { adapter ->
            mTabItemList.clear()
            mTabContainer.removeAllViews()
            mCurrentTabPosition = 0

            for (i in 0 until adapter.itemCount) {
                val tabItem = adapter.createView(this, i)
                mTabItemList.add(tabItem)
                mTabContainer.gravity = tabContainerGravity
                mTabContainer.addView(tabItem.itemView, tabItem.position, tabLayoutParams)
            }
        }
    }

    fun onPageScrolled(position: Int, positionOffset: Float) {
        mCurrentScrollTabPosition = position
        mCurrentScrollTabPositionOffset = positionOffset
        scrollToCurrentTab()
        scroll()
        invalidate()
    }
    fun onPageSelected(position: Int) {
        mTabAdapter?.let { adapter ->
            if (mTabItemList.isNotEmpty()) {
                if (mCurrentTabPosition in 0 until adapter.itemCount) {
                    adapter.onDeselected(mTabItemList[mCurrentTabPosition])
                }
                adapter.onSelected(mTabItemList[position])
            }
        }
        mCurrentTabPosition = position
    }
    fun onPageScrollStateChanged(state: Int) {}

    // 绘制指示器, 指示器的本身是一个 Drawable 对象, 其本身大小用 Rect 来约束
    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        mTabAdapter?.let { adapter ->
            if (!isInEditMode && adapter.itemCount > 0) {
                computeIndicatorRect()
                indicatorDrawable.bounds = indicatorRect
                indicatorDrawable.draw(canvas)
            }
        }
    }

    // position 左向右: 滚动位置与当前位置相等  右向左: 滚动位置 = 当前位置 - 1
    private fun scroll() {
        mTabAdapter?.let { adapter ->
            if (mTabItemList.isNotEmpty()) {
                if (mCurrentScrollTabPosition == mCurrentTabPosition && mCurrentScrollTabPosition < mTabItemList.size - 1) {
                    adapter.onLeave(mTabItemList[mCurrentScrollTabPosition], mCurrentScrollTabPositionOffset, true)
                    adapter.onEnter(mTabItemList[mCurrentScrollTabPosition + 1], mCurrentScrollTabPositionOffset, true)
                } else if (mCurrentScrollTabPosition == mCurrentTabPosition - 1 && mCurrentScrollTabPosition >= 0) {
                    adapter.onLeave(mTabItemList[mCurrentTabPosition], 1.0f - mCurrentScrollTabPositionOffset, false)
                    adapter.onEnter(mTabItemList[mCurrentScrollTabPosition], 1.0f - mCurrentScrollTabPositionOffset, false)
                }
            }
        }
    }

    private fun scrollCenter(itemCount: Int, currentScrollTab: View, offset: Int, newScrollX: Int): Int {
        var nsx = newScrollX
        var herf = 0
        if (mCurrentScrollTabPosition < itemCount - 1) {
            mTabContainer.getChildAt(mCurrentScrollTabPosition + 1)?.let { nextTab ->
                val leftDistance = currentScrollTab.left + (nextTab.left - currentScrollTab.left) * mCurrentScrollTabPositionOffset
                val rightDistance = currentScrollTab.right + (nextTab.right - currentScrollTab.right) * mCurrentScrollTabPositionOffset
                herf = ((rightDistance - leftDistance) / 2).toInt()
            }
        }
        if (mCurrentScrollTabPosition > 0 || offset > 0) {
            val centerDistance = width / 2
            nsx -= centerDistance        // 正在滚动Tab距离中心点的位置
            nsx += herf                  // 下一个Tab距离中心点的位置
        }
        return nsx
    }
    private fun scrollRight(itemCount: Int, currentScrollTab: View, offset: Int, newScrollX: Int): Int {
        var nsx = newScrollX
        if (mCurrentScrollTabPosition < itemCount - 1) {
            mTabContainer.getChildAt(mCurrentScrollTabPosition + 1)?.let { nextTab ->
                val leftDistance = currentScrollTab.left + (nextTab.left - currentScrollTab.left) * mCurrentScrollTabPositionOffset
                val rightDistance = currentScrollTab.right + (nextTab.right - currentScrollTab.right) * mCurrentScrollTabPositionOffset
                val herf = ((rightDistance - leftDistance) / 2).toInt()
                if (mCurrentScrollTabPosition > 0 || offset > 0) {
                    nsx -= width - herf * 2
                }
            }
        }
        return nsx
    }
    // HorizontalScrollView 滚到当前 tab
    private fun scrollToCurrentTab() {
        mTabAdapter?.let { adapter ->
            val itemCount = adapter.itemCount
            if (itemCount > 0 && mCurrentScrollTabPositionOffset >= 0) {
                mTabContainer.getChildAt(mCurrentScrollTabPosition)?.let { currentScrollTab ->
                    val offset = (currentScrollTab.width * mCurrentScrollTabPositionOffset).toInt()
                    var newScrollX = currentScrollTab.left + offset
                    when (tabScrollGravity) {
                        1 -> newScrollX = scrollCenter(itemCount, currentScrollTab, offset, newScrollX)
                        2 -> newScrollX = scrollRight(itemCount, currentScrollTab, offset, newScrollX)
                    }
                    if (newScrollX != mLastScrollX) {
                        mLastScrollX = newScrollX
                        scrollTo(newScrollX, 0)
                    }
                }
            }
        }
    }

    // 计算指示器的大小，用 Rect 来表示. 该 Rect 用来决定 Drawable 的大小.
    private fun computeIndicatorRect() {
        mTabAdapter?.let { adapter ->
            val itemCount = adapter.itemCount
            if (itemCount > 0 && mCurrentScrollTabPositionOffset >= 0) {
                mTabContainer.getChildAt(mCurrentScrollTabPosition)?.let { currentScrollTab ->
                    var left = currentScrollTab.left
                    var right = currentScrollTab.right
                    val bottom = currentScrollTab.bottom
                    if (indicatorWidth > 0) {
                        left += getTabInsert(currentScrollTab)
                        right -= getTabInsert(currentScrollTab)
                    }
                    if (mCurrentScrollTabPosition < itemCount - 1) {
                        val nextChild = mTabContainer.getChildAt(mCurrentScrollTabPosition + 1) ?: return
                        // 这个是要移动的距离
                        var leftDistance = (nextChild.left - left).toFloat()
                        var rightDistance = (nextChild.right - right).toFloat()
                        if (indicatorWidth <= 0) {
                            leftDistance *= mCurrentScrollTabPositionOffset
                            rightDistance *= mCurrentScrollTabPositionOffset
                        } else {
                            val tabInsert = getTabInsert(nextChild)
                            leftDistance = (leftDistance + tabInsert) * mCurrentScrollTabPositionOffset
                            rightDistance = (rightDistance - tabInsert) * mCurrentScrollTabPositionOffset
                        }
                        left += leftDistance.toInt()
                        right += rightDistance.toInt()
                    }
                    indicatorRect.left = left + indicatorLeftMargin
                    indicatorRect.top = bottom - indicatorHeight - indicatorBottomMargin
                    indicatorRect.right = right - indicatorRightMargin
                    indicatorRect.bottom = bottom - indicatorBottomMargin
                }
            }
        }
    }
    private fun getTabInsert(view: View): Int {
        return (view.width - indicatorWidth) / 2
    }
}