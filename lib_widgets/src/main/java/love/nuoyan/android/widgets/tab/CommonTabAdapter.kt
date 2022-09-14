package love.nuoyan.android.widgets.tab

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView

abstract class CommonTabAdapter: TabAdapter() {
    var normalColor = Color.GRAY
    var selectedColor = Color.BLACK

    abstract fun onViewCreated(view: TextView, position: Int)

    override fun onCreateView(parent: ViewGroup, position: Int): TextView {
        val childView = TextView(parent.context)
        childView.gravity = Gravity.CENTER
        childView.setTextColor(normalColor)
        onViewCreated(childView, position)
        return childView
    }
    override fun onSelected(item: TabItem) {
        (item.itemView as? TextView)?.setTextColor(selectedColor)
    }
    override fun onDeselected(item: TabItem) {
        (item.itemView as? TextView)?.setTextColor(normalColor)
    }
    override fun onLeave(item: TabItem, leavePercent: Float, leftToRight: Boolean) {
        val color = ArgbEvaluatorHolder.eval(leavePercent, selectedColor, normalColor)
        (item.itemView as? TextView)?.setTextColor(color)
    }
    override fun onEnter(item: TabItem, enterPercent: Float, leftToRight: Boolean) {
        val color = ArgbEvaluatorHolder.eval(enterPercent, normalColor, selectedColor)
        (item.itemView as? TextView)?.setTextColor(color)
    }
}