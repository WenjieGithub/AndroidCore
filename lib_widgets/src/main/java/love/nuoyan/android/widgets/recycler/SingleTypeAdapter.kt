package love.nuoyan.android.widgets.recycler

import love.nuoyan.android.widgets.recycler.item.ItemDelegate
import love.nuoyan.android.widgets.recycler.item.ViewHolder

/**
 * RecyclerView 单一类型适配器
 */
abstract class SingleTypeAdapter<T>(layoutId: Int, dataList: List<T>?) : MultiItemTypeAdapter<T>() {
    protected abstract fun onCreateViewHolder(holder: ViewHolder)
    protected abstract fun onBindViewHolder(holder: ViewHolder, t: T, position: Int)

    init {
        addItemDelegate(object : ItemDelegate<T> {
            override val itemLayoutId: Int
                get() = layoutId

            override fun isForItemType(item: T, position: Int): Boolean {
                return true
            }

            override fun onCreateViewHolder(adapter: MultiItemTypeAdapter<T>, holder: ViewHolder) {
                this@SingleTypeAdapter.onCreateViewHolder(holder)
            }

            override fun onBindViewHolder(adapter: MultiItemTypeAdapter<T>, holder: ViewHolder, item: T, position: Int) {
                this@SingleTypeAdapter.onBindViewHolder(holder, item, position)
            }
        })
        dataList?.let { addData(it) }
    }
}
