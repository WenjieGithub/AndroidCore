package love.nuoyan.android.widgets.recycler.decor

import android.graphics.Canvas
import android.graphics.Rect
import android.util.ArrayMap
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import java.lang.ref.WeakReference

/**
 * 用于实现 RecyclerView 分组的 Decoration； 根据需求复写 onCreate***、onBind*** 函数。
 * 不需要再次调用 addItemDecoration 函数
 */
abstract class ItemDecorationSection(recyclerView: RecyclerView, isStickyHeader: Boolean) : ItemDecoration(), OnItemTouchListener {
    private val mSectionId = arrayListOf<Int>()
    private val mSectionInfoList = ArrayMap<String, SectionInfo>()  // 保存 SectionInfo
    private val mTapDetector: GestureDetector                       // 手势检测
    private var isStickyHeader = false                              // 是否开启顶部粘性头布局, 设置是否悬浮分组View

    private var mRecyclerViewReference: WeakReference<RecyclerView>? = null
    init {
        this.isStickyHeader = isStickyHeader
        mRecyclerViewReference = WeakReference(recyclerView)
        mTapDetector = GestureDetector(recyclerView.context, SingleTapDetector())
        recyclerView.addItemDecoration(this)
        recyclerView.addOnItemTouchListener(this)
    }


    // 创建 SectionHeaderLayout
    abstract fun onCreateSectionInfo(location: Location, position: Int): SectionInfo?
    abstract fun onBindSectionInfo(location: Location, info: SectionInfo, position: Int)

    // 清除历史数据
    fun clear() {
        mSectionId.clear()
        mSectionInfoList.clear()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        if (RecyclerView.NO_POSITION != position) {
            getSectionInfo(true, parent, view, position)?.let {
                onBindSectionInfo(Location.Header, it, position)
                outRect.top = it.view.height
            }
            getSectionInfo(false, parent, view, position)?.let {
                onBindSectionInfo(Location.Footer, it, position)
                outRect.bottom = it.view.height
            }
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        for (info in mSectionInfoList.values) {
            info.mRect.set(0, 0, 0,0)
        }
        for (layoutPos in 0 until parent.childCount) {                                          // 遍历屏幕上加载的 item
            val view = parent.getChildAt(layoutPos)
            val position = parent.getChildAdapterPosition(view)                                 // 获取该 item 在列表数据中的位置
            if (RecyclerView.NO_POSITION != position) {
                getSectionInfo(false, parent, view, position)?.let {
                    c.save()
                    val left = view.left + it.marginLeft + it.translationX                      // 获取绘制header的起始位置(left,top)
                    val top = (view.y + view.height + it.translationY).toInt()
                    c.translate(left.toFloat(), top.toFloat())                                       // 将画布移动到绘制的位置
                    it.view.draw(c)                                                                  // 绘制
                    c.restore()
                    it.mRect.set(left, top, left + it.view.width, top + it.view.height)// 保存绘制的区域
                }

                var headerInfo = getSectionInfo(true, parent, view, position)
                if (headerInfo == null && isStickyHeader && layoutPos == 0) {                        // 只有在最上面一个 item 或者有 header 的 item 才绘制 header
                    headerInfo = getSectionInfo(true, parent, view, getSectionHeaderPosition(position))
                }
                if (headerInfo != null) {
                    c.save()
                    val left = view.left + headerInfo.marginLeft + headerInfo.translationX
                    val top = if (isStickyHeader) {
                        getSectionHeaderTop(parent, view, headerInfo, position, layoutPos)
                    } else {
                        (view.y - headerInfo.view.height + headerInfo.translationY).toInt()
                    }
                    c.translate(left.toFloat(), top.toFloat())
                    headerInfo.view.draw(c)
                    c.restore()
                    headerInfo.mRect.set(left, top, left + headerInfo.view.width, top + headerInfo.view.height)
                }
            }
        }
    }

    @Synchronized
    private fun getSectionInfo(isHeader: Boolean, parent: RecyclerView, view: View, position: Int): SectionInfo? {
        val key = (if (isHeader) "Header_" else "Footer_") + position
        var info = mSectionInfoList[key]
        if (info == null) {
            info = if (isHeader) onCreateSectionInfo(Location.Header, position) else onCreateSectionInfo(
                Location.Footer, position)
            if (info != null) {
                if (isHeader) {
                    mSectionId.add(position)
                }
                mSectionInfoList[key] = info
            }
        }
        if (info != null) {
            // 测量 View 并且 layout
            val widthSpec = View.MeasureSpec.makeMeasureSpec(view.width - info.marginLeft - info.marginRight, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)
            // 根据父 View 的 MeasureSpec 和子 view 自身的 LayoutParams 以及 padding 来获取子 View 的 MeasureSpec
            val childWidth = ViewGroup.getChildMeasureSpec(widthSpec, 0, view.layoutParams.width)
            val childHeight = ViewGroup.getChildMeasureSpec(heightSpec, parent.paddingTop + parent.paddingBottom, parent.layoutParams.height)
            // 进行测量
            info.view.measure(childWidth, childHeight)
            // 根据测量后的宽高放置位置
            info.view.layout(0, 0, info.view.measuredWidth, info.view.measuredHeight)
        }
        return info
    }

    // 计算距离顶部的高度
    private fun getSectionHeaderTop(
        parent: RecyclerView,
        view: View,
        info: SectionInfo,
        position: Int,
        layoutPos: Int
    ): Int {
        val headerHeight = info.view.height
        var top = view.y.toInt() - headerHeight + info.translationY
        if (layoutPos == 0) {                                                                       // 在绘制最顶部的 SectionHeader 的时候，需要考虑处理两个分组的 SectionHeader 交换时候的情况
            val currentId = "Header_" + getSectionHeaderPosition(position)
            for (i in 1 until parent.childCount) {                                              // 从屏幕上的第二个 item 开始遍历
                val nextView = parent.getChildAt(i)
                val nextPosition = parent.getChildAdapterPosition(nextView)
                if (nextPosition != RecyclerView.NO_POSITION) {
                    val nextId = "Header_" + getSectionHeaderPosition(nextPosition)
                    if (currentId != nextId) {                                                      // 找到下一个不同组的 view
                        val nextInfo = mSectionInfoList[nextId]
                        if (nextInfo != null) {
                            // 当不同组的第一个 view 距离顶部的位置减去两组 SectionHeader 的高度，得到 offset
                            val offset = nextView.y.toInt() + nextInfo.translationY - nextInfo.view.height - headerHeight
                            return if (offset < 0) {                                                // offset小于 0 即为两组开始交换，第一个 SectionHeader 被挤出界面的距离
                                offset
                            } else {
                                break
                            }
                        }
                    }
                }
            }
            top = 0.coerceAtLeast(top)
        }
        return top
    }

    // 获取这个位置的 Item Section 的开头 position
    private fun getSectionHeaderPosition(position: Int): Int {
        var oldP = -1
        for (i in mSectionId) {
            oldP = when {
                i > position -> return oldP
                i == position -> return i
                else -> i
            }
        }
        return oldP
    }
    // 遍历屏幕上 SectionView 的区域，判断点击的位置是否在某个 SectionView 的区域内
    private fun findSectionInfoPositionUnder(x: Int, y: Int): String? {
        for (i in 0 until mSectionInfoList.size) {
            val info = mSectionInfoList[mSectionInfoList.keyAt(i)]
            if (info != null && info.mRect.contains(x, y)) {
                return mSectionInfoList.keyAt(i)
            }
        }
        return null
    }
    private fun findSectionInfoClickView(view: View?, x: Int, y: Int): Boolean {
        if (view == null) return false
        for (i in 0 until mSectionInfoList.size) {
            val info = mSectionInfoList[mSectionInfoList.keyAt(i)]
            if (info != null && info.mRect.contains(x, y)) {
                val vRect = Rect()
                // 需要响应点击事件的区域在屏幕上的坐标
                vRect.set(info.mRect.left + view.left, info.mRect.top + view.top, info.mRect.left + view.left + view.width, info.mRect.top + view.top + view.height)
                return vRect.contains(x, y)
            }
        }
        return false
    }

    // 以下为手势相关
    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        return if (MotionEvent.ACTION_DOWN == e.action) {                                           // 如果是点击在 SectionHeader 区域，则拦截事件
            null != findSectionInfoPositionUnder(e.x.toInt(), e.y.toInt())
        } else false
    }

    override fun onTouchEvent(view: RecyclerView, e: MotionEvent) {
        mTapDetector.onTouchEvent(e)                                                         // 将事件交给 GestureDetector 类进行处理，通过 onSingleTapUp 返回的值，判断是否要拦截事件
    }
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    private inner class SingleTapDetector : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val key = findSectionInfoPositionUnder(e.x.toInt(), e.y.toInt())                // 根据点击的坐标查找是不是点击在 SectionHeader 的区域
            if (key != null) {                                                                      // 如果 position 不等于 -1, 则表示点击在 SectionHeader 区域，然后在判断是否在 SectionHeader 需要响应的区域
                val headerInfo = mSectionInfoList[key]
                if (headerInfo != null) {
                    if (headerInfo.view is ViewGroup) {
                        for (i in 0 until headerInfo.view.childCount) {
                            val child = headerInfo.view.getChildAt(i)
                            if (findSectionInfoClickView(child, e.x.toInt(), e.y.toInt())) {
                                child.performClick()                                                // 如果在header需要响应的区域，该区域的view模拟点击
                            }
                        }
                    }
                    mRecyclerViewReference?.get()?.playSoundEffect(SoundEffectConstants.CLICK)
                    // headerView.onTouchEvent(e);
                    headerInfo.view.performClick()
                    return true
                }
            }
            return false
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            return true
        }
    }


    data class SectionInfo(
        val view: View,
        val translationX: Int = 0,          // X 轴平移量
        val translationY: Int = 0,          // Y 轴平移量
        val marginLeft: Int = 0,            // 距离左边缘距离
        val marginRight: Int = 0,           // 距离右边缘距离
        internal val mRect: Rect = Rect()   // 存放屏幕上显示的 SectionView 的点击区域，每次重新绘制头部的时候重置
    )

    enum class Location {
        Header, Footer
    }
}