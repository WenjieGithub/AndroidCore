package love.nuoyan.android.widgets.binding

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan

class DiyImageSpan : ImageSpan {
    private var mIsReduce = false
    private var mWidth = 0   // sp
    private var mHeight = 0  // sp
    constructor(drawable: Drawable, width: Int = 0, height: Int = 0, isReduce: Boolean = false): super(drawable) {
        mIsReduce = isReduce
        mWidth = width
        mHeight = height
    }
    constructor(context: Context, resourceId: Int, width: Int = 0, height: Int = 0, isReduce: Boolean = false): super(context, resourceId) {
        mIsReduce = isReduce
        mWidth = width
        mHeight = height
    }

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val d = drawable
        val rect = d.bounds
        if (fm != null) {
            val fmPaint = paint.fontMetricsInt
            val fontHeight = fmPaint.bottom - fmPaint.top
            val drHeight = rect.bottom - rect.top
            val top = drHeight / 2 - fontHeight / 4
            val bottom = drHeight / 2 + fontHeight / 4
            fm.ascent = -bottom
            fm.top = -bottom
            fm.bottom = top
            fm.descent = top
        }
        return rect.right
    }
    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val b = drawable
        if (mIsReduce) {
            b.setBounds(0, 0, b.intrinsicWidth - 10, b.intrinsicHeight - 10)
        } else if (mWidth > 0 && mHeight > 0) {
            b.setBounds(0, 0, mWidth, mHeight)
        } else {
            b.setBounds(0, 0, b.intrinsicWidth, b.intrinsicHeight)
        }
        var mTop = top
        var mBottom = bottom
        mTop += 5
        mBottom -= 5
        canvas.save()
        val transY = (mBottom - mTop - b.bounds.bottom) / 2 + mTop
        canvas.translate(x, transY.toFloat())
        b.draw(canvas)
        canvas.restore()
    }
}