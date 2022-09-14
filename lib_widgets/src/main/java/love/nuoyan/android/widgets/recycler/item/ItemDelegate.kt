package love.nuoyan.android.widgets.recycler.item

import androidx.annotation.LayoutRes
import love.nuoyan.android.widgets.recycler.MultiItemTypeAdapter

/**
 * ItemView 多类型支持代理
 */
interface ItemDelegate<T> {
    /**
     * 获取该 Item 的 LayoutId
     */
    @get:LayoutRes
    val itemLayoutId: Int

    /**
     * 这个数据是否是这个 Item 类型
     */
    fun isForItemType(item: T, position: Int): Boolean

    /**
     * Holder 创建时的回调
     */
    fun onCreateViewHolder(adapter: MultiItemTypeAdapter<T>, holder: ViewHolder)

    /**
     * Holder 和数据进行绑定
     */
    fun onBindViewHolder(adapter: MultiItemTypeAdapter<T>, holder: ViewHolder, item: T, position: Int)
}