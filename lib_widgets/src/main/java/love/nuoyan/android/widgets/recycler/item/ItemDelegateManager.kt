package love.nuoyan.android.widgets.recycler.item

import androidx.collection.SparseArrayCompat
import love.nuoyan.android.widgets.recycler.MultiItemTypeAdapter

/**
 * 描述: ItemDelegate 管理类
 * 作者: WJ
 * 时间: 2017/10/23
 * 版本: 1.0
 */
class ItemDelegateManager<T> {
    private val delegates = SparseArrayCompat<ItemDelegate<T>>()    // Item 类型数组

    /**
     * 添加 Item 类型
     */
    fun addItemDelegate(delegate: ItemDelegate<T>) {
        delegates.put(delegate.itemLayoutId, delegate)
    }

    /**
     * 根据数据获取 Item 类型
     */
    fun getItemType(item: T, position: Int): Int {
        for (i in 0 until delegates.size()) {
            val delegate = delegates.valueAt(i)
            if (delegate != null && delegate.isForItemType(item, position)) {
                return delegates.keyAt(i)
            }
        }
        throw IllegalArgumentException("没有添加匹配的 ItemDelegate: position=$position")
    }

    /**
     * 根据类型获取 ItemDelegate
     */
    fun getItemDelegate(viewType: Int): ItemDelegate<T> {
        val item = delegates[viewType]
        if (item != null) {
            return item
        } else {
            throw IllegalArgumentException("没有添加匹配的 ItemDelegate: viewType=$viewType")
        }
    }

    /**
     * 创建 Holder 时回调
     */
    fun onCreateViewHolder(adapter: MultiItemTypeAdapter<T>, holder: ViewHolder, viewType: Int): ViewHolder {
        val delegate = getItemDelegate(viewType)
        delegate.onCreateViewHolder(adapter, holder)
        return holder
    }

    /**
     * Holder 和数据进行绑定
     */
    fun onBindViewHolder(adapter: MultiItemTypeAdapter<T>, holder: ViewHolder, item: T, position: Int) {
        for (i in 0 until delegates.size()) {
            val delegate = delegates.valueAt(i)
            if (delegate.isForItemType(item, position)) {
                delegate.onBindViewHolder(adapter, holder, item, position)
                return
            }
        }
        throw IllegalArgumentException("没有添加匹配的 ItemDelegate position=$position")
    }
}