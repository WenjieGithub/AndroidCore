package love.nuoyan.android.widgets.recycler

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import love.nuoyan.android.widgets.recycler.item.ViewHolder
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList

/**
 * RecyclerView 多类型适配器
 */
abstract class DiffMultiTypeAdapter<T> : RecyclerView.Adapter<ViewHolder>() {
    private val mDataList = arrayListOf<T>()
    private val mDelegates = SparseArrayCompat<ItemDelegate<T>>()
    /** 添加 Item 类型  */
    fun addItemDelegate(itemViewDelegate: ItemDelegate<T>) = apply { mDelegates.put(itemViewDelegate.itemLayoutId, itemViewDelegate) }

    private val mDiffCallback = object : DiffUtil.Callback() {
        var newDataList: List<T>? = null
        override fun getOldListSize() = mDataList.size
        override fun getNewListSize() = newDataList?.size ?: 0
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return try {
                adapterAreItemsTheSame(mDataList[oldItemPosition], newDataList!![newItemPosition])
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return try {
                adapterAreContentsTheSame(mDataList[oldItemPosition], newDataList!![newItemPosition])
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return try {
                adapterGetChangePayload(mDataList[oldItemPosition], newDataList!![newItemPosition])
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    // Item是否相同
    abstract fun adapterAreItemsTheSame(oldItem: T, newItem: T): Boolean
    // Item是否相同的内容
    abstract fun adapterAreContentsTheSame(oldItem: T, newItem: T): Boolean
    // 高效执行，局部刷新，没有数据变化则返回null
    open fun adapterGetChangePayload(oldItem: T, newItem: T): Any? {
        return null
    }
    open fun onDataChange() {}

    fun getData(position: Int): T { return mDataList[position] }
    fun getData(): ArrayList<T> { return arrayListOf<T>().apply{ addAll(mDataList) } }
    suspend fun setData(data: List<T>?, detectMoves: Boolean = false) {
        withContext(Default) {
            mDiffCallback.newDataList = data
            val result = DiffUtil.calculateDiff(mDiffCallback, detectMoves)
            withContext(Main) {
                result.dispatchUpdatesTo(this@DiffMultiTypeAdapter)
                mDataList.clear()
                data?.let { mDataList.addAll(it) }
                onDataChange()
            }
        }
    }
    fun addData(data: List<T>) {
        if (data.isNotEmpty()) {
            val ps = mDataList.size
            mDataList.addAll(data)
            notifyItemRangeInserted(ps, data.size)
            onDataChange()
        }
    }
    fun addData(index: Int, data: List<T>) {
        if (data.isNotEmpty()) {
            mDataList.addAll(index, data)
            notifyItemRangeInserted(index, data.size)
            onDataChange()
        }
    }
    fun addData(data: T) {
        mDataList.add(data)
        notifyItemInserted(mDataList.size - 1)
        onDataChange()
    }
    fun addData(index: Int, data: T) {
        mDataList.add(index, data)
        notifyItemInserted(index)
        onDataChange()
    }
    fun removeData(index: Int) {
        if (index in 0 until mDataList.size) {
            mDataList.removeAt(index).also { notifyItemRemoved(index) }
            onDataChange()
        }
    }
    fun removeData(data: T) {
        val p = mDataList.indexOf(data)
        if (p >= 0) {
            mDataList.removeAt(p)
            notifyItemRemoved(p)
            onDataChange()
        }
    }
    suspend fun removeData(set: Set<T>) {
        val list = getData()
        list.removeAll(set)
        setData(list)
        onDataChange()
    }
    fun clearData() {
        mDataList.clear()
        onDataChange()
    }


    override fun getItemCount() = mDataList.size
    override fun getItemViewType(position: Int): Int {
        // 根据数据获取 Item 类型
        for (index in 0 until mDelegates.size()) {
            val key = mDelegates.keyAt(index)
            val value = mDelegates.valueAt(index)
            if (value.isForItemType(mDataList[position], position)) {
                return key
            }
        }
        throw IllegalArgumentException("没有添加匹配的 ItemDelegate: position=$position")
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent, viewType).apply {
            val delegate = mDelegates[viewType]
            if (delegate != null) {
                delegate.onCreateViewHolder(this@DiffMultiTypeAdapter, this)
            } else {
                throw IllegalArgumentException("没有添加匹配的 ItemDelegate: viewType=$viewType")
            }
        }
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mDataList[position]
        for (index in 0 until mDelegates.size()) {
            val value = mDelegates.valueAt(index)
            if (value.isForItemType(data, position)) {
                value.onBindViewHolder(this, holder, data, position)
                return
            }
        }
        throw IllegalArgumentException("没有添加匹配的 ItemDelegate position=$position  item=$data")
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val data = mDataList[position]
            for (index in 0 until mDelegates.size()) {
                val value = mDelegates.valueAt(index)
                if (value.isForItemType(data, position)) {
                    value.onBindViewHolder(this, holder, data, payloads[0], position)
                    return
                }
            }

            throw IllegalArgumentException("没有添加匹配的 ItemDelegate position=$position  item=$data")
        }
    }
}

/** ItemView 多类型支持代理 */
interface ItemDelegate<T> {
    /** 获取该 Item 的 LayoutId */
    @get:LayoutRes val itemLayoutId: Int
    /** 这个数据是否是这个 Item 类型 */
    fun isForItemType(item: T, position: Int): Boolean
    /** Holder 创建时的回调 */
    fun onCreateViewHolder(adapter: DiffMultiTypeAdapter<T>, holder: ViewHolder)
    /** Holder 和数据进行绑定 */
    fun onBindViewHolder(adapter: DiffMultiTypeAdapter<T>, holder: ViewHolder, item: T, position: Int)
    /** Holder 和数据进行绑定 高效局部刷新, 需要重写 adapterGetChangePayload */
    fun onBindViewHolder(adapter: DiffMultiTypeAdapter<T>, holder: ViewHolder, item: T, payloads: Any, position: Int)
}