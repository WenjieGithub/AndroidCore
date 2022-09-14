package love.nuoyan.android.widgets.banner

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import love.nuoyan.android.widgets.dp2px
import love.nuoyan.android.widgets.recycler.MultiItemTypeAdapter
import love.nuoyan.android.widgets.recycler.item.ViewHolder
import kotlinx.coroutines.Runnable
import java.lang.reflect.Field
import kotlin.math.abs
import kotlin.math.sign

/**
 * 支持循环和自动滚动的 Banner
 */
class Banner: FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val viewPager = ViewPager2(context)
    private val compositePageTransformer = CompositePageTransformer()
    private var marginPageTransformer: MarginPageTransformer? = null

    private var downX = 0f
    private var downY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop / 3
    private fun isOrientationHorizontal() = viewPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL
    private fun canChildScroll(delta: Float): Boolean {
        val direction = -delta.sign.toInt()
        return when {
            isOrientationHorizontal() -> viewPager.canScrollHorizontally(direction)
            else -> viewPager.canScrollVertically(direction)
        }
    }

    private var isCycle = false                 // 是否循环滚动
    private var isAutoScroll = false            // 是否自动滚动
    private var time = 4000L                    // 自动滚动时间间隔

    private val task = Runnable {
        if (isCycle && isAutoScroll) {
            val count = getAdapter()?.itemCount ?: 0
            if (count > 1) {
                if (viewPager.currentItem + 1 < count) {
                    getAdapter()?.let { viewPager.setCurrentItem(viewPager.currentItem + 1, true) }
                } else {
                    getAdapter()?.let { viewPager.setCurrentItem(it.getUIPosition(0), false) }
                }
            }
        }
        start()
    }

    var pageShowListener: ((viewHolder: ViewHolder, position: Int)->Unit)? = null
    internal var pageDataChanged: (()->Unit)? = null
    internal var pageScrolledListener: ((position: Int, positionOffset: Float)->Unit)? = null

    init {
        viewPager.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply { gravity = Gravity.CENTER }
        viewPager.setPageTransformer(compositePageTransformer)
        initViewPagerScrollProxy()
        addView(viewPager)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            var mPosition = -1
            override fun onPageSelected(position: Int) {
                mPosition = position
                ((viewPager.getChildAt(0) as? RecyclerView)?.findViewHolderForAdapterPosition(position) as? ViewHolder)?.let { vh ->
                    getAdapter()?.let { adapter -> pageShowListener?.invoke(vh, adapter.getUIPosition(position)) }
                }
            }
            override fun onPageScrollStateChanged(state: Int) {
                if (isCycle && state == ViewPager2.SCROLL_STATE_IDLE && !viewPager.isFakeDragging) {
                    getAdapter()?.let { viewPager.setCurrentItem(it.getEqualPosition(mPosition), false) }
                }
            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                getAdapter()?.let { pageScrolledListener?.invoke(it.getUIPosition(position), positionOffset) }
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE){
            start()
        } else if(visibility == INVISIBLE || visibility == GONE){
            stop()
        }
    }
    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == View.VISIBLE){
            start()
        } else if(visibility == INVISIBLE || visibility == GONE){
            stop()
        }
    }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (viewPager.isUserInputEnabled) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    stop()
                    downX = ev.rawX
                    downY = ev.rawY
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_MOVE -> {
                    stop()
                    val dx = ev.rawX - downX
                    val dy = ev.rawY - downY
                    if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                        if (isOrientationHorizontal() && abs(dy) > abs(dx)) { // 手势是垂直的，允许所有拦截
                            parent.requestDisallowInterceptTouchEvent(false)
                        } else if (canChildScroll(if (isOrientationHorizontal()) dx else dy)) {
                            parent.requestDisallowInterceptTouchEvent(true)
                        } else {
                            parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> start()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * @param cycle         是否可以循环滚动
     * @param autoScroll    是否可以自动滚动
     * @param margin        item 之间的间距，单位 dp
     * @param tlWidth       item 左边页面显露出来的宽度，单位 dp
     * @param brWidth       item 右边页面显露出来的宽度，单位 dp
     * @param intervalTime  如果可以自动滚动，则滚动的间隔时间为 time
     * @param direction     Banner 滚动的方向，为 0 则横向滚动，否则为纵向滚动
     */
    fun setBanner(cycle: Boolean = false, autoScroll: Boolean = false, margin: Int = 0, tlWidth: Int = 0, brWidth: Int = 0, intervalTime: Long = 4000, direction: Int = 0) {
        isCycle = cycle
        isAutoScroll = autoScroll
        time = intervalTime

        if (isAutoScroll) start() else stop()
        getAdapter()?.isCycle = isCycle

        setMargin(margin)
        setOrientation(direction)
        setMarginPadding(margin, tlWidth, brWidth, direction)
    }
    fun <T> setAdapter(): BannerAdapter<T> {
        return BannerAdapter<T>(isCycle) {
            pageDataChanged?.invoke()
            if (!viewPager.isFakeDragging) { setCurrentItem(0, false) }
        }.apply {
            viewPager.adapter = this
        }
    }
    fun getAdapter(): BannerAdapter<*>? {
        return viewPager.adapter as? BannerAdapter<*>
    }
    fun addTransformer(transformer: ViewPager2.PageTransformer) {
        compositePageTransformer.addTransformer(transformer)
    }
    fun setCurrentItem(uiPosition: Int, smoothScroll: Boolean) {
        getAdapter()?.let { adapter ->
            if (uiPosition >= 0 && adapter.getRealItemCount() > uiPosition && !viewPager.isFakeDragging) {
                viewPager.setCurrentItem(if (isCycle && adapter.itemCount > 1) uiPosition + 2 else uiPosition, smoothScroll)
            }
        }
    }
    fun getCurrentItem(): Int? {
        return (viewPager.adapter as? BannerAdapter<*>)?.getUIPosition(viewPager.currentItem)
    }
    fun getLayoutManager(): LinearLayoutManager? {
        return (viewPager.getChildAt(0) as? RecyclerView)?.let {
            it.layoutManager as? LinearLayoutManager
        }
    }

    private fun start() {
        stop()
        if (isAutoScroll) {
            postDelayed(task, time)
        }
    }
    private fun stop() {
        removeCallbacks(task)
    }

    private fun setMargin(margin: Int) {
        if (margin >= 0) {
            marginPageTransformer?.let { compositePageTransformer.removeTransformer(it) }
            marginPageTransformer = MarginPageTransformer(margin.dp2px.toInt()).apply { addTransformer(this) }
        }
    }
    private fun setOrientation(direction: Int) {
        viewPager.orientation = if (direction == 0) ViewPager2.ORIENTATION_HORIZONTAL else ViewPager2.ORIENTATION_VERTICAL
    }
    private fun setMarginPadding(margin: Int = 0, tlWidth: Int = 0, brWidth: Int = 0, direction: Int = 0) {
        if (tlWidth >= 0 || brWidth >= 0) {
            (viewPager.getChildAt(0) as? RecyclerView)?.let { recyclerView ->
                val m = margin.coerceAtLeast(0)
                val tl = (tlWidth + m).dp2px.toInt()
                val br = (brWidth + m).dp2px.toInt()
                if (direction == 0) {
                    recyclerView.setPadding(tl, 0, br, 0)
                } else {
                    recyclerView.setPadding(0, tl, 0, br)
                }
                recyclerView.clipToPadding = false
            }
        }
    }

    class BannerAdapter<T>(var isCycle: Boolean, var dataChanged: (()->Unit)? = null): MultiItemTypeAdapter<T>() {
        fun getEqualPosition(position: Int): Int {
            val count = itemCount
            return if (isCycle && count > 1) {
                when (position) {
                    0 -> count - 4
                    1 -> count - 3
                    count - 1 -> 3
                    count - 2 -> 2
                    else -> position
                }
            } else {
                position
            }
        }
        fun getUIPosition(position: Int): Int {
            val count = itemCount
            return if (isCycle && count > 1) {
                when (position) {
                    0 -> count - 4
                    1 -> count - 3
                    count - 2 -> 2
                    count - 1 -> 3
                    else -> position
                } - 2
            } else {
                position
            }
        }

        fun getRealItemCount() = super.getItemCount()
        override fun getItemCount(): Int {
            val count = super.getItemCount()
            return count + if (isCycle && count > 1) 4 else 0
        }
        override fun getItemViewType(position: Int): Int { return super.getItemViewType(getUIPosition(position)) }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) { return super.onBindViewHolder(holder, getUIPosition(position)) }
        override fun onDataChange() { dataChanged?.invoke() }
    }

    private fun initViewPagerScrollProxy() {
        try {
            //控制切换速度，采用反射方。法方法只会调用一次，替换掉内部的RecyclerView的LinearLayoutManager
            val recyclerView = viewPager.getChildAt(0) as RecyclerView
            recyclerView.overScrollMode = OVER_SCROLL_NEVER
            val o = recyclerView.layoutManager as? LinearLayoutManager
            val proxyLayoutManger = ProxyLayoutManger(context, o!!)
            recyclerView.layoutManager = proxyLayoutManger

            val mRecyclerView = RecyclerView.LayoutManager::class.java.getDeclaredField("mRecyclerView")
            mRecyclerView.isAccessible = true
            mRecyclerView.set(o, recyclerView)

            val layoutMangerField = ViewPager2::class.java.getDeclaredField("mLayoutManager")
            layoutMangerField.isAccessible = true
            layoutMangerField.set(viewPager, proxyLayoutManger)

            val pageTransformerAdapterField = ViewPager2::class.java.getDeclaredField("mPageTransformerAdapter")
            pageTransformerAdapterField.isAccessible = true
            val mPageTransformerAdapter = pageTransformerAdapterField.get(viewPager)
            if (mPageTransformerAdapter != null) {
                val aClass: Class<*> = mPageTransformerAdapter.javaClass
                val layoutManager: Field = aClass.getDeclaredField("mLayoutManager")
                layoutManager.isAccessible = true
                layoutManager.set(mPageTransformerAdapter, proxyLayoutManger)
            }
            val scrollEventAdapterField: Field = ViewPager2::class.java.getDeclaredField("mScrollEventAdapter")
            scrollEventAdapterField.isAccessible = true
            val mScrollEventAdapter = scrollEventAdapterField.get(viewPager)
            if (mScrollEventAdapter != null) {
                val aClass: Class<*> = mScrollEventAdapter.javaClass
                val layoutManager: Field = aClass.getDeclaredField("mLayoutManager")
                layoutManager.isAccessible = true
                layoutManager.set(mScrollEventAdapter, proxyLayoutManger)
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
}

// linearLayoutManager 是 ViewPager2 中的 LinearLayoutManagerImpl 对象
private class ProxyLayoutManger(context: Context, val linearLayoutManager: RecyclerView.LayoutManager): LinearLayoutManager(context) {
    override fun performAccessibilityAction(recycler: RecyclerView.Recycler, state: RecyclerView.State, action: Int, args: Bundle?): Boolean {
        return linearLayoutManager.performAccessibilityAction(recycler, state, action, args)
    }

    override fun onInitializeAccessibilityNodeInfo(recycler: RecyclerView.Recycler, state: RecyclerView.State, info: AccessibilityNodeInfoCompat) {
        linearLayoutManager.onInitializeAccessibilityNodeInfo(recycler, state, info)
    }

    override fun requestChildRectangleOnScreen(parent: RecyclerView, child: View, rect: Rect, immediate: Boolean, focusedChildVisible: Boolean): Boolean {
        return linearLayoutManager.requestChildRectangleOnScreen(parent, child, rect, immediate)
    }

    override fun calculateExtraLayoutSpace(state: RecyclerView.State, extraLayoutSpace: IntArray) {
        try {
            val method = linearLayoutManager.javaClass.getDeclaredMethod("calculateExtraLayoutSpace", state.javaClass, extraLayoutSpace.javaClass)
            method.isAccessible = true
            method.invoke(linearLayoutManager, state, extraLayoutSpace)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?, position: Int) {
        val linearSmoothScroller = object : LinearSmoothScroller(recyclerView.context) {
            override fun calculateTimeForDeceleration(dx: Int): Int {
                return (800 * (1 - .3356)).toInt()
            }
        }
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }
}