package love.nuoyan.android.widgets.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import love.nuoyan.android.widgets.recycler.item.ItemDelegate
import love.nuoyan.android.widgets.recycler.item.ItemDelegateManager
import love.nuoyan.android.widgets.recycler.item.ViewHolder
import kotlin.collections.ArrayList

/**
 * RecyclerView 多类型适配器
 */
open class MultiItemTypeAdapter<T> : RecyclerView.Adapter<ViewHolder>() {
    protected val dataList = arrayListOf<T>()
    private var mItemDelegateManager = ItemDelegateManager<T>()

    /** 添加 Item 类型  */
    fun addItemDelegate(itemViewDelegate: ItemDelegate<T>) = apply { mItemDelegateManager.addItemDelegate(itemViewDelegate) }

    fun getData(position: Int): T { return dataList[position] }
    fun getData(): ArrayList<T> { return arrayListOf<T>().apply{ addAll(dataList) } }
    fun addData(data: List<T>) { if (data.isNotEmpty()) { dataList.addAll(data).also { onDataChange() } } }
    fun addData(index: Int, data: List<T>) { if (data.isNotEmpty()) { dataList.addAll(index, data).also { onDataChange() } } }
    fun addData(data: T) { dataList.add(data).also { onDataChange() } }
    fun addData(index: Int, data: T) { dataList.add(index, data).also { onDataChange() } }
    fun removeData(index: Int): T? { return if (index in 0 until dataList.size) { dataList.removeAt(index).also { onDataChange() } } else null }
    fun removeData(data: T): Boolean { return dataList.remove(data).also { onDataChange() } }
    fun removeData(c: Collection<T>): Boolean { return dataList.removeAll(c).also { onDataChange() } }
    fun clearData() { dataList.clear().also { onDataChange() } }

    open fun onDataChange() {}

    override fun getItemCount() = dataList.size
    override fun getItemViewType(position: Int) = mItemDelegateManager.getItemType(dataList[position], position)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return mItemDelegateManager.onCreateViewHolder(this, ViewHolder(parent, mItemDelegateManager.getItemDelegate(viewType).itemLayoutId), viewType)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mItemDelegateManager.onBindViewHolder(this, holder, dataList[position], position)
    }
}