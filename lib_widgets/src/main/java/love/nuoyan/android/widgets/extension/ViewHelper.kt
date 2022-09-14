package love.nuoyan.android.widgets.extension

import android.animation.*
import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import love.nuoyan.android.widgets.dp2px
import kotlin.math.abs

/**
 * 支持: 下拉关闭
 */
class ViewHelper<T : View> {
    private var mView: T? = null

    private var mViewWidth = 0
    private var mViewHeight = 0

    private val mPaint = Paint()
    private val mClipPath = Path()
    private val mAreas = RectF()
    private val mRadiusArray = FloatArray(8)

    init {
        mPaint.reset()
        mPaint.color = Color.WHITE
        mPaint.isAntiAlias = true
    }

    fun onAttachedToWindow(view: T) {
        mView = view
    }

    fun onDetachedFromWindow() {
        mView = null
    }

    fun onSizeChanged(w: Int, h: Int) {
        mAreas.set(0f, 0f, w.toFloat(), h.toFloat())
        refreshRegion()
    }

    fun onClipDraw(canvas: Canvas) {
//        mPaint.color = mShadowColor
//        mPaint.isAntiAlias = true
//        canvas.drawRoundRect(mAreas, 10f, 10f, mPaint)
//        canvas.drawPath(mClipPath, mPaint)
    }

    fun setCornersRadius(lt: Float = 0f, rt: Float = 0f, rb: Float = 0f, lb: Float = 0f): ViewHelper<T> {
        mRadiusArray[0] = lt.dp2px
        mRadiusArray[1] = mRadiusArray[0]
        mRadiusArray[2] = rt.dp2px
        mRadiusArray[3] = mRadiusArray[2]
        mRadiusArray[4] = rb.dp2px
        mRadiusArray[5] = mRadiusArray[4]
        mRadiusArray[6] = lb.dp2px
        mRadiusArray[7] = mRadiusArray[6]
        refreshRegion()
        return this
    }

    private fun refreshRegion() {
        mView?.let {
            mViewWidth = it.width
            mViewHeight = it.height

            mClipPath.reset()
            mClipPath.addRoundRect(mAreas, mRadiusArray, Path.Direction.CW)
        }
    }


    // 下拉关闭支持
    private var mEndTranslationX = 0f
    private var mEndTranslationY = 0f
    private var mPreviousX = 0f
    private var mPreviousY = 0f
    private var mDropdownCloseDistance = 0
    private var mDropdownCloseDistance2 = 0
    private var isScrollingUp = false
    private var isDropdownClose = true
    private var mDropdownCloseBackground: Drawable? = null
    private var mDropdownCloseListener: DropdownCloseListener? = null

    fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isDropdownClose && ev.pointerCount == 1) {  // 判断几指操作，大于1时认为在对图片进行放大缩小操作，不拦截事件 交由下一个控件处理
            val x = ev.rawX
            val y = ev.rawY
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    mPreviousX = x
                    mPreviousY = y
                    mDropdownCloseDistance2 = if (mDropdownCloseDistance == 0) mViewHeight / 3 else mDropdownCloseDistance
                }
                MotionEvent.ACTION_MOVE -> {
                    val diffX = x - mPreviousX
                    val diffY: Float = y - mPreviousY
                    if (diffY > 0 && abs(diffX) + 50 < abs(diffY)) {    // 当下拉时，Y轴移动距离大于X轴50个单位时拦截事件，进入onTouchEvent开始处理下滑退出效果
                        return true
                    }
                }
            }
        }
        return false
    }

    fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isDropdownClose) {
            val x = ev.rawX
            val y = ev.rawY
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    mPreviousX = x
                    mPreviousY = y
                    mDropdownCloseDistance2 = if (mDropdownCloseDistance == 0) mViewHeight / 3 else mDropdownCloseDistance
                }
                MotionEvent.ACTION_MOVE -> {
                    val diffX = x - mPreviousX
                    val diffY: Float = y - mPreviousY
                    isScrollingUp = diffY <= 0      // 判断手指向上还是向下移动，关联手指抬起后的动画位移方向
                    mView?.translationX = diffX
                    mView?.translationY = diffY
                    if (diffY > 0) {
                        var scale: Float = 1 - diffY / (mViewHeight - mPreviousY)
                        scale = if (scale > 0.5) scale else 0.5f
                        scale = if (scale > 1) 1f else scale
                        mView?.scaleX = scale
                        mView?.scaleY = scale
                    } else {
                        mView?.scaleX = 1f
                        mView?.scaleY = 1f
                    }
                    if (!isScrollingUp && mDropdownCloseBackground != null) {
                        var alpha = (255 * abs(diffY) / mDropdownCloseDistance2).toInt() // 透明度跟随手指的移动距离发生变化
                        alpha = alpha.coerceAtMost(255)
                        mDropdownCloseBackground?.alpha = 255 - alpha
                        mDropdownCloseListener?.onClosedProgress(alpha / 255f)
                    }
                }
                MotionEvent.ACTION_UP -> if (!isScrollingUp && abs(mView!!.translationY) > mDropdownCloseDistance2) { // 滑动距离超过临界值才执行退出动画，默认临界值为控件高度1/3
                    layoutExitAnim() // 执行退出动画
                } else {
                    layoutRecoverAnim() // 执行恢复动画
                }
            }
            return true
        }
        return false
    }

    @SuppressLint("ObjectAnimatorBinding")
    fun layoutExitAnim() {
        mView?.let {
            val p1 = PropertyValuesHolder.ofFloat("translationY", it.translationY, mEndTranslationY)
            val p2 = PropertyValuesHolder.ofFloat("translationX", it.translationX, mEndTranslationX)
            val p3 = PropertyValuesHolder.ofFloat("scaleX", mView!!.scaleX, 0.2f)
            val p4 = PropertyValuesHolder.ofFloat("scaleY", mView!!.scaleY, 0.2f)
            val p5 = PropertyValuesHolder.ofFloat("alpha", mView!!.alpha, 0f)
            val exitAnim = ObjectAnimator.ofPropertyValuesHolder(mView, p1, p2, p3, p4, p5)
            val alpha = if (mDropdownCloseBackground != null) mDropdownCloseBackground!!.alpha else 255
            exitAnim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (mDropdownCloseBackground != null) {                                             // 动画结束时将背景置为完全透明
                        mDropdownCloseBackground!!.alpha = 0
                    }
                    mDropdownCloseListener?.onClosed() // 执行回调，退出页面
                }
            })
            exitAnim.addUpdateListener { animation ->
                mDropdownCloseBackground?.alpha = (alpha * (1 - (animation.currentPlayTime - animation.startDelay) * 1f / animation.duration)).toInt()
            }
            exitAnim.duration = 300
            exitAnim.start()
        }
    }

    private fun layoutRecoverAnim() {
        mView?.animate()?.translationX(0f)?.translationY(0f)?.scaleX(1f)?.scaleY(1f)?.setDuration(100)?.start() // 从手指抬起的地方恢复到原点
        mDropdownCloseBackground?.alpha = 255 // 将背景置为完全不透明
        mDropdownCloseListener?.onRecover()
    }

    // 下拉关闭后动画停止的位置
    fun setDropdownCloseTranslation(x: Float, y: Float) {
        mEndTranslationX = x - mViewWidth / 2f
        mEndTranslationY = y - mViewHeight / 2f
    }

    fun setDropdownCloseGradualBackground(drawable: Drawable?) {
        mDropdownCloseBackground = drawable
    }

    // 默认为 View 高度 / 3
    fun setDropdownCloseDistance(distance: Int) {
        mDropdownCloseDistance = distance
    }

    fun setDropdownCloseListener(listener: DropdownCloseListener?) {
        mDropdownCloseListener = listener
    }

    interface DropdownCloseListener {
        // 正在滑动
        fun onClosedProgress(progress: Float)

        // 关闭布局
        fun onClosed()

        // 恢复布局，滑动结束并且没有触发关闭
        fun onRecover()
    }
}