package love.nuoyan.android.widgets.extension

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * 下拉关闭的 Layout
 */
class FrameLayoutExtension: FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val mHelper: ViewHelper<FrameLayoutExtension> = ViewHelper()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mHelper.onAttachedToWindow(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHelper.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mHelper.onSizeChanged(w, h)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return mHelper.onInterceptTouchEvent(ev!!) || super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return mHelper.onTouchEvent(event!!) || super.onTouchEvent(event)
    }
}