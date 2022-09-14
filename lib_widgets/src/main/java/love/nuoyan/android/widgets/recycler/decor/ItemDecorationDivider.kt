package love.nuoyan.android.widgets.recycler.decor

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * 用于实现 RecyclerView 分割线的 Decoration；根据需求复写 onCreateDivider、onBindDivider 函数。
 */
abstract class ItemDecorationDivider : RecyclerView.ItemDecoration() {
    private val mDividerList = hashMapOf<String, DividerInfo>()         // 保存绘制的 section 的区域

    abstract fun onCreateDivider(direction: Direction, position: Int): DividerInfo?
    abstract fun onBindDivider(direction: Direction, info: DividerInfo, position: Int)

    fun clear() {
        mDividerList.clear()
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        if (RecyclerView.NO_POSITION != position) {
            getDivider(Direction.Left, position)?.let {
                onBindDivider(Direction.Left, it, position)
                if (it.distance > 0) {
                    outRect.left = it.distance
                }
            }
            getDivider(Direction.Top, position)?.let {
                onBindDivider(Direction.Top, it, position)
                if (it.distance > 0) {
                    outRect.top = it.distance
                }
            }
            getDivider(Direction.Right, position)?.let {
                onBindDivider(Direction.Right, it, position)
                if (it.distance > 0) {
                    outRect.right = it.distance
                }
            }
            getDivider(Direction.Bottom, position)?.let {
                onBindDivider(Direction.Bottom, it, position)
                if (it.distance > 0) {
                    outRect.bottom = it.distance
                }
            }
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        for (layoutPos in 0 until parent.childCount) {                  // 遍历屏幕上加载的 item
            val child = parent.getChildAt(layoutPos)
            val position = parent.getChildAdapterPosition(child)       // 获取该 item 在列表数据中的位置
            if (RecyclerView.NO_POSITION != position) {
                val viewLeft = child.left                               // 获取绘制header的起始位置(left,top)
                val viewWidth = child.width
                val viewHeight = child.height
                c.save()
                c.translate(viewLeft.toFloat(), child.y)
                getDivider(Direction.Top, position)?.let { info ->
                    info.divider?.let {
                        it.setBounds(
                            info.marginLeft,
                            -info.distance + info.marginTop,
                            viewWidth - info.marginRight,
                            -info.marginBottom
                        )
                        it.draw(c)
                    }
                }
                getDivider(Direction.Bottom, position)?.let { info ->
                    info.divider?.let {
                        it.setBounds(
                            info.marginLeft,
                            viewHeight + info.marginTop,
                            viewWidth - info.marginRight,
                            viewHeight + info.distance - info.marginBottom
                        )
                        it.draw(c)
                    }
                }
                getDivider(Direction.Left, position)?.let { info ->
                    info.divider?.let {
                        it.setBounds(
                            -info.distance + info.marginLeft,
                            info.marginTop,
                            -info.marginRight,
                            viewHeight - info.marginBottom
                        )
                        it.draw(c)
                    }
                }
                getDivider(Direction.Right, position)?.let { info ->
                    info.divider?.let {
                        it.setBounds(
                            viewWidth + info.marginLeft,
                            info.marginTop,
                            viewWidth + info.distance - info.marginRight,
                            viewHeight - info.marginBottom
                        )
                        it.draw(c)
                    }
                }
                c.restore()
            }
        }
    }

    private fun getDivider(direction: Direction, position: Int): DividerInfo? {
        val key = when (direction) {
            Direction.Left -> "DividerLeftKey_$position"
            Direction.Top ->  "DividerTopKey_$position"
            Direction.Right -> "DividerRightKey_$position"
            Direction.Bottom -> "DividerBottomKey_$position"
        }
        var info = mDividerList[key]
        if (info == null) {
            info = onCreateDivider(direction, position)
            info?.let {
                mDividerList[key] = it
            }
        }
        return info
    }

    // 分割线信息
    data class DividerInfo(
        var divider: Drawable? = null,  // 绘制的 Drawable

        var distance: Int = 0,          // 上、下的分割线高度; 左、右的分割线宽度
        var marginLeft: Int = 0,        // 距离左边缘距离
        var marginTop: Int = 0,         // 距离上边缘距离
        var marginRight: Int = 0,       // 距离右边缘距离
        var marginBottom: Int = 0       // 距离下边缘距离
    )

    // 方向 direction：left=0; top=1 right=2 bottom=3
    enum class Direction {
        Left, Top, Right, Bottom
    }
}