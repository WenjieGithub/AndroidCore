package love.nuoyan.android.widgets.recycler.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView 的通用 ViewHolder
 */
class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    constructor(parent: ViewGroup, @LayoutRes id: Int) : this(
        LayoutInflater.from(parent.context).inflate(id, parent, false)
    )

    /** 通过 viewId 获取控件 */
    fun <T : View> getView(@IdRes viewId: Int): T {
        return itemView.findViewById(viewId)
    }

    /** 获取 viewId */
    fun getViewId(): Int {
        return itemView.id
    }
}