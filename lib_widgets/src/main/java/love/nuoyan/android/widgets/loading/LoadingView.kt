package love.nuoyan.android.widgets.loading

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val mCircleCount = 12                               // 小圆总数
    private val mCircleDegree = (360 / mCircleCount)            // 小圆圆心之间间隔角度差
    private val mWholeCircleRadius = FloatArray(mCircleCount)   // 记录所有小圆半径
    private val mWholeCircleColors = IntArray(mCircleCount)     // 记录所有小圆颜色
    private var mMaxCircleRadius = 0f                           // 小圆最大半径

    private var mPaint = Paint()                                // 画笔
    private var mAnimator: ValueAnimator? = null
    private var mAnimateValue = 0

    private var mSize = 0                                       // 控件大小

    var animatorDuration = 1000L                                // 动画时长
    var circleColor = Color.parseColor("#888888")               // 小圆颜色

    init {
        // 初始化画笔
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.FILL
        mPaint.color = circleColor
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initValue(w.coerceAtMost(h))
    }

    // 初始化所有小圆半径、颜色
    private fun initValue(size: Int) {
        mSize = size
        val minCircleRadius = mSize / (mCircleCount * 2f)
        for (i in 0 until mCircleCount) {
            when (i) {
                7 -> {
                    mWholeCircleRadius[i] = minCircleRadius * 1.25f
                    mWholeCircleColors[i] = (255 * 0.7f).toInt()
                }
                8 -> {
                    mWholeCircleRadius[i] = minCircleRadius * 1.5f
                    mWholeCircleColors[i] = (255 * 0.8f).toInt()
                }
                9, 11 -> {
                    mWholeCircleRadius[i] = minCircleRadius * 1.75f
                    mWholeCircleColors[i] = (255 * 0.9f).toInt()
                }
                10 -> {
                    mWholeCircleRadius[i] = minCircleRadius * 2f
                    mWholeCircleColors[i] = 255
                }
                else -> {
                    mWholeCircleRadius[i] = minCircleRadius
                    mWholeCircleColors[i] = (255 * 0.5f).toInt()
                }
            }
        }
        mMaxCircleRadius = minCircleRadius * 2
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (mSize > 0) {
            // 每隔 mCircleDegree * mAnimateValue 角度，绘制所有小圆
            canvas.rotate(mCircleDegree * mAnimateValue.toFloat(), mSize / 2f, mSize / 2f)
            for (i in 0 until mCircleCount) {
                mPaint.alpha = mWholeCircleColors[i]        // 设置小圆颜色
                canvas.drawCircle(
                    mSize / 2f,
                    mMaxCircleRadius,
                    mWholeCircleRadius[i],
                    mPaint
                )                                           // 每隔 mCircleDegree 角度，绘制一个小圆
                canvas.rotate(mCircleDegree.toFloat(), mSize / 2f, mSize / 2f)
            }
        }
    }

    // 动画监听
    private val mUpdateListener = AnimatorUpdateListener { animation ->
        mAnimateValue = animation.animatedValue as Int
        invalidate()
    }

    // 开始动画
    private fun start() {
        if (mAnimator == null) {
            mAnimator = ValueAnimator.ofInt(0, mCircleCount - 1)
            mAnimator?.addUpdateListener(mUpdateListener)
            mAnimator?.duration = animatorDuration
            mAnimator?.repeatMode = ValueAnimator.RESTART
            mAnimator?.repeatCount = ValueAnimator.INFINITE
            mAnimator?.interpolator = LinearInterpolator()
            mAnimator?.start()
        } else if (!mAnimator!!.isStarted) {
            mAnimator?.start()
        }
    }

    // 停止动画
    private fun stop() {
        if (mAnimator != null) {
            mAnimator?.removeUpdateListener(mUpdateListener)
            mAnimator?.removeAllUpdateListeners()
            mAnimator?.cancel()
            mAnimator = null
        }
    }

    // View 依附 Window 时停止动画
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    // View 脱离 Window 时停止动画
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    // 根据 View 可见性变化开始/停止动画
    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) {
            start()
        } else {
            stop()
        }
    }
}
