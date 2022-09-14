package love.nuoyan.android.widgets.tab

import android.database.DataSetObservable
import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup

abstract class TabAdapter {
    abstract val itemCount: Int
    /** 创建 View */
    abstract fun onCreateView(parent: ViewGroup, position: Int): View
    /** 正在选中 */
    abstract fun onSelected(item: TabItem)
    /** 取消选中 */
    abstract fun onDeselected(item: TabItem)
    /**
     * 离开
     * @param leavePercent 离开的百分比, 0.0f - 1.0f
     * @param leftToRight  从左至右离开
     */
    abstract fun onLeave(item: TabItem, leavePercent: Float, leftToRight: Boolean)
    /**
     * 进入
     * @param enterPercent 进入的百分比, 0.0f - 1.0f
     * @param leftToRight  从左至右离开
     */
    abstract fun onEnter(item: TabItem, enterPercent: Float, leftToRight: Boolean)

    internal fun createView(parent: ViewGroup, position: Int): TabItem {
        val item = TabItem(onCreateView(parent, position), position)
        if (item.itemView.parent != null) {
            throw IllegalStateException("View must not be attached when created. Ensure that you are not passing 'true' to the attachToRoot parameter of LayoutInflater.inflate(..., boolean attachToRoot)")
        }
        return item
    }

    private val mObservable = DataSetObservable()
    fun registerObserver(observer: DataSetObserver) { mObservable.registerObserver(observer) }
    fun unregisterObserver(observer: DataSetObserver) { mObservable.unregisterObserver(observer) }
    fun notifyChanged() { mObservable.notifyChanged() }
    fun notifyInvalidated() { mObservable.notifyInvalidated() }
}