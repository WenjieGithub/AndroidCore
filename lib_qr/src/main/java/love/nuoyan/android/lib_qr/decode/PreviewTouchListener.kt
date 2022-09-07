package love.nuoyan.android.lib_qr.decode

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.view.View.OnTouchListener

class PreviewTouchListener(context: Context) : OnTouchListener {
    private val mScaleGestureDetector: ScaleGestureDetector
    private var mCustomTouchListener: ((delta: Float) -> Unit)? = null  // 放大

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        mScaleGestureDetector.onTouchEvent(event)
        return true
    }

    fun setCustomTouchListener(customTouchListener: ((delta: Float) -> Unit)?) {
        mCustomTouchListener = customTouchListener
    }

    // 缩放监听
    var onScaleGestureListener: OnScaleGestureListener = object : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val delta = detector.scaleFactor
            mCustomTouchListener?.let { it(delta) }
            return true
        }
    }

    init {
        mScaleGestureDetector = ScaleGestureDetector(context, onScaleGestureListener)
    }
}
