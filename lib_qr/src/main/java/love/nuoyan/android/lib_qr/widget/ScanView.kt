package love.nuoyan.android.lib_qr.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import love.nuoyan.android.lib_qr.decode.dp2px

class ScanView : View {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val scanRect = RectF()
    private val lineRect = RectF()
    private val scanPath = Path()

    private var scanMarginWith = 0f                     // 扫描框边缘宽度
    private var scanLineTop = 0f                        // 扫描线位置
    private var alpha = 0xFF                            // 透明度
    private var linearGradient: LinearGradient? = null
    private var valueAnimator: ValueAnimator? = null

    private var frameBoundsColor = Color.parseColor("#FFE69533")    // 边框颜色
    private var lineColor = Color.parseColor("#FFE69533")           // 线的颜色, 会做半透明处理
    private var shadowColor = Color.parseColor("#4D000000")         // 阴影颜色
    private var lineHigh = 4.dp2px                                  // 扫描线高度

    private val corWidth = 4.dp2px
    private val corLength = 19.dp2px
    private val radius = 2.dp2px

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        paint.strokeWidth = 2f
        paint.style = Paint.Style.FILL
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        scanMarginWith = width / 10f
        val scanMarginHeight = height / 6f
        val scanWith = width - 2 * scanMarginWith

        var scanBottom = scanMarginHeight + scanWith
        if (scanBottom >= height - scanMarginHeight) {
            scanBottom = height - 2 * scanMarginHeight
        }

        scanRect.set(scanMarginWith, scanMarginHeight, width - scanMarginWith, scanBottom)
        valueAnimator = ValueAnimator.ofFloat(scanRect.top, scanRect.bottom)
        scanPath.reset()
        scanPath.addRoundRect(scanRect, 2.dp2px, 2.dp2px, Path.Direction.CCW)
        linearGradient = LinearGradient(
            scanMarginWith, 0f, width - scanMarginWith, 0f,
            intArrayOf(Color.TRANSPARENT, lineColor, Color.TRANSPARENT), null,
            Shader.TileMode.CLAMP
        )
        startAnim()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawFrameBounds(canvas, scanRect)
        drawLine(canvas)
        drawShadow(canvas)
    }
    // 绘制取景框边框
    private fun drawFrameBounds(canvas: Canvas, frame: RectF) {
        if (frameBoundsColor != Color.TRANSPARENT) {
            paint.color = frameBoundsColor
            paint.alpha = 0xFF

            // 左上角
            canvas.drawRoundRect(
                frame.left,
                frame.top,
                frame.left + corWidth,
                frame.top + corLength,
                radius, radius, paint
            )
            canvas.drawRoundRect(
                frame.left,
                frame.top,
                frame.left + corLength,
                frame.top + corWidth,
                radius, radius, paint
            )
            // 右上角
            canvas.drawRoundRect(
                frame.right,
                frame.top,
                frame.right - corWidth,
                frame.top + corLength,
                radius, radius, paint
            )
            canvas.drawRoundRect(
                frame.right - corLength,
                frame.top,
                frame.right,
                frame.top + corWidth,
                radius, radius, paint
            )
            // 左下角
            canvas.drawRoundRect(
                frame.left,
                frame.bottom - corLength,
                frame.left + corWidth,
                frame.bottom,
                radius, radius, paint
            )
            canvas.drawRoundRect(
                frame.left,
                frame.bottom - corWidth,
                frame.left + corLength,
                frame.bottom,
                radius, radius, paint
            )
            // 右下角
            canvas.drawRoundRect(
                frame.right - corWidth,
                frame.bottom - corLength,
                frame.right,
                frame.bottom,
                radius, radius, paint
            )
            canvas.drawRoundRect(
                frame.right - corLength,
                frame.bottom - corWidth,
                frame.right,
                frame.bottom,
                radius, radius, paint
            )
        }
    }
    private fun drawLine(canvas: Canvas) {
        paint.alpha = alpha
        lineRect.set(scanMarginWith, scanLineTop, width - scanMarginWith, scanLineTop + lineHigh)
        paint.shader = linearGradient
        canvas.drawRect(lineRect, paint)
        paint.shader = null
    }
    private fun drawShadow(canvas: Canvas) {
        paint.color = shadowColor
        canvas.clipRect(0f, 0f, width.toFloat(), height.toFloat())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipOutPath(scanPath)
        } else {
            canvas.clipPath(scanPath, Region.Op.XOR)
        }
        canvas.drawColor(shadowColor)
    }

    private fun startAnim() {
        valueAnimator?.apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            duration = 4000
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                scanLineTop = animation.animatedValue as Float
                val startHideHeight = (scanRect.bottom - scanRect.top) / 6
                alpha = if (scanRect.bottom - scanLineTop <= startHideHeight) {
                        ((scanRect.bottom - scanLineTop).toDouble() / startHideHeight * 100).toInt()
                } else {
                    100
                }
                postInvalidate()
            }
            start()
        }
    }

    fun cancelAnim() {
        valueAnimator?.cancel()
    }
}