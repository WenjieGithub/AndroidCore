package love.nuoyan.android.floating_kit.extension

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class SingleAdapter<T>(@get:LayoutRes val itemLayoutId: Int, dataList: List<T>?) : RecyclerView.Adapter<SingleAdapter.SingleViewHolder>() {
    private var mDataList = arrayListOf<T>()

    init {
        dataList?.let { addData(it) }
    }

    fun addData(data: List<T>) {
        if (data.isNotEmpty()) {
            mDataList.addAll(data)
        }
    }

    fun getData(position: Int): T {
        return mDataList[position]
    }

    fun clearData() {
        mDataList.clear()
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleViewHolder {
        val holder = SingleViewHolder(parent, itemLayoutId)
        onCreateViewHolder(holder)
        return holder
    }

    override fun onBindViewHolder(holder: SingleViewHolder, position: Int) {
        onBindViewHolder(holder, mDataList[position], position)
    }

    protected abstract fun onCreateViewHolder(holder: SingleViewHolder)
    protected abstract fun onBindViewHolder(holder: SingleViewHolder, t: T, position: Int)

    class SingleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        constructor(parent: ViewGroup, @LayoutRes id: Int) : this(
            LayoutInflater.from(parent.context).inflate(id, parent, false)
        )

        fun <T : View> getView(@IdRes viewId: Int): T {
            return itemView.findViewById(viewId)
        }
    }
}