package love.nuoyan.android.widgets.banner

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import love.nuoyan.android.widgets.binding.visibleOrGone
import love.nuoyan.android.widgets.dp2px

/**
 * 横向指示器
 */
class IndicatorView: View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var unColor = Color.GRAY
    var selectedColor = Color.WHITE
    var indicatorRatio = 1.0f                   // 指示器比例; 拉伸圆为矩形，控制该比例，default 1.0
    var indicatorRadius = 3.5f.dp2px            // 指示器圆角半径，default 3.5dp
    var indicatorSelectedRatio = 1.0f           // 指示器选定比例; 拉伸圆为矩形，控制该比例，默认比例和indicatorRatio一致
    var indicatorSelectedRadius = 3.5f.dp2px    // 指示器选定圆角半径，default 3.5dp
    var indicatorSpacing = 10f.dp2px            // 指示器间距
    var indicatorStyle = IndicatorStyle.Circle

    private val interpolator: Interpolator = DecelerateInterpolator()

    private var offset = 0f
    private var selectedPage = 0
    private var pagerCount = 0                  // ViewPage 页数量

    private var rectF = RectF()
    private var indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun attachBanner(banner: Banner) {
        banner.getAdapter()?.let {
            setLayout(it.getRealItemCount())
        }
        banner.pageDataChanged = {
            banner.getAdapter()?.let {
                setLayout(it.getRealItemCount())
            }
        }
        banner.pageScrolledListener = { position, positionOffset ->
            setPageScrolled(position, positionOffset)
        }
    }
    fun setLayout(pagerCount: Int) {
        this.pagerCount = pagerCount
        visibleOrGone = pagerCount > 1
        requestLayout()
    }
    fun setPageScrolled(position: Int, positionOffset: Float) {
        selectedPage = position
        offset = positionOffset
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
    }
    private fun getRatioRadius() = indicatorRadius * indicatorRatio
    private fun getRatioSelectedRadius() = indicatorSelectedRadius * indicatorSelectedRatio
    private fun measureWidth(widthMeasureSpec: Int): Int {
        val mode = MeasureSpec.getMode(widthMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        return when (mode) {
            MeasureSpec.EXACTLY -> width
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                val ratioSelectedRadius = getRatioSelectedRadius()
                val ratioRadius = getRatioRadius()
                val diameterDistance = ratioSelectedRadius.coerceAtLeast(ratioRadius) * 2 * pagerCount
                val spacingDistance = (pagerCount - 1) * indicatorSpacing
                val al = ratioSelectedRadius - ratioRadius
                (diameterDistance + spacingDistance + al + paddingLeft + paddingRight).toInt()
            }
            else -> 0
        }
    }
    private fun measureHeight(heightMeasureSpec: Int): Int {
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        return when (mode) {
            MeasureSpec.EXACTLY -> height
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                val ratioSelectedRadius = getRatioSelectedRadius()
                val ratioRadius = getRatioRadius()
                val diameterDistance = ratioSelectedRadius.coerceAtLeast(ratioRadius) * 2
                (diameterDistance + paddingTop + paddingBottom).toInt()
            }
            else -> 0
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (pagerCount > 0) {
            val midY = height / 2f + 0.5f
            when (indicatorStyle) {
                IndicatorStyle.Circle -> drawCircle(canvas, midY)
                IndicatorStyle.CircleRect -> drawCircleRect(canvas, midY)
                IndicatorStyle.Dash -> drawDash(canvas, midY)
                IndicatorStyle.BigCircle -> drawBigCircle(canvas, midY)
                IndicatorStyle.Bezier -> drawBezier(canvas, midY)
            }
        }
    }


    private fun drawCircle(canvas: Canvas, midY: Float) {
        drawPagerCountCircle(canvas, midY)
        val indicatorStartX = indicatorStartX(selectedPage)
        val nextIndicatorStartX = indicatorStartX((selectedPage + 1) % pagerCount)
        val ratioRadius = getRatioSelectedRadius()
        val left = indicatorStartX - ratioRadius
        val right = indicatorStartX + ratioRadius
        val nextLeft = nextIndicatorStartX - ratioRadius
        val nextRight = nextIndicatorStartX + ratioRadius
        val leftX = left + (nextLeft - left) * interpolatedOffset()
        val rightX = right + (nextRight - right) * interpolatedOffset()
        rectF[leftX, midY - indicatorSelectedRadius, rightX] = midY + indicatorSelectedRadius
        indicatorPaint.color = selectedColor
        canvas.drawRoundRect(rectF, indicatorSelectedRadius, indicatorSelectedRadius, indicatorPaint)
    }
    private fun drawPagerCountCircle(canvas: Canvas, midY: Float) {
        indicatorPaint.color = unColor
        for (i in 0 until pagerCount) {
            val startCx: Float = indicatorStartX(i)
            val ratioIndicatorRadius = getRatioRadius()
            val left = startCx - ratioIndicatorRadius
            val top = midY - indicatorRadius
            val right = startCx + ratioIndicatorRadius
            val bottom = midY + indicatorRadius
            rectF[left, top, right] = bottom
            canvas.drawRoundRect(rectF, indicatorRadius, indicatorRadius, indicatorPaint)
        }
    }
    private fun indicatorStartX(index: Int): Float {
        val ratioRadius = getRatioRadius()
        val ratioSelectedRadius = getRatioSelectedRadius()
        val ratioIndicatorRadius = ratioRadius.coerceAtLeast(ratioSelectedRadius)
        val centerSpacing = ratioIndicatorRadius * 2.0f + indicatorSpacing
        val centerX = ratioIndicatorRadius + paddingLeft + centerSpacing * index
        /*
           为了适配INDICATOR_DASH样式， measure 中默认多增加了 ratioIndicatorRadius - ratioRadius 的高度和宽度
           除了INDICATOR_DASH样式下，其他样式需要增加indicatorSelectedRadius一半的距离，让位置居中。
         */
        return centerX + (if (indicatorStyle == IndicatorStyle.Dash) 0f else (ratioIndicatorRadius - ratioRadius) / 2f)
    }
    private fun interpolatedOffset(): Float {
        return interpolator.getInterpolation(offset)
    }


    private fun drawCircleRect(canvas: Canvas, midY: Float) {
        drawPagerCountCircle(canvas, midY)
        val indicatorStartX = indicatorStartX(selectedPage)
        val ratioRadius = getRatioSelectedRadius()
        val left = indicatorStartX - ratioRadius
        val right = indicatorStartX + ratioRadius
        val offset = interpolatedOffset()
        var distance = indicatorSpacing + getRatioRadius().coerceAtLeast(ratioRadius) * 2
        val leftX: Float
        val rightX: Float
        if ((selectedPage + 1) % pagerCount == 0) {
            distance *= -selectedPage.toFloat()
            leftX = left + (distance * offset * 2).coerceAtLeast(distance)
            rightX = right + (distance * (offset - 0.5f) * 2.0f).coerceAtMost(0f)
        } else {
            leftX = left + (distance * (offset - 0.5f) * 2.0f).coerceAtLeast(0f)
            rightX = right + (distance * offset * 2).coerceAtMost(distance)
        }
        rectF[leftX, midY - indicatorSelectedRadius, rightX] = midY + indicatorSelectedRadius
        indicatorPaint.color = selectedColor
        canvas.drawRoundRect(rectF, indicatorSelectedRadius, indicatorSelectedRadius, indicatorPaint)
    }
    private fun drawDash(canvas: Canvas, midY: Float) {
        val offset = interpolatedOffset()
        //默认dash的长度，设置ratio控制长度
        val ratioSelectedRadius = getRatioSelectedRadius()
        val ratioIndicatorRadius = getRatioRadius()
        val distance = ratioSelectedRadius - ratioIndicatorRadius
        val distanceOffset = distance * offset
        val nextPage = (selectedPage + 1) % pagerCount
        val isNextFirst = nextPage == 0
        indicatorPaint.color = unColor
        for (i in 0 until pagerCount) {
            var startCx = indicatorStartX(i)
            if (isNextFirst) startCx += distanceOffset
            val left = startCx - ratioIndicatorRadius
            val top = midY - indicatorRadius
            val right = startCx + ratioIndicatorRadius
            val bottom = midY + indicatorRadius
            if (selectedPage + 1 <= i) {
                rectF[left + distance, top, right + distance] = bottom
            } else {
                rectF[left, top, right] = bottom
            }
            canvas.drawRoundRect(rectF, indicatorRadius, indicatorRadius, indicatorPaint)
        }
        indicatorPaint.color = selectedColor
        if (offset < 0.99f) {
            var leftX = indicatorStartX(selectedPage) - ratioSelectedRadius
            if (isNextFirst) leftX += distanceOffset
            val rightX = leftX + ratioSelectedRadius * 2 + distance - distanceOffset
            rectF[leftX, midY - indicatorSelectedRadius, rightX] = midY + indicatorSelectedRadius
            canvas.drawRoundRect(rectF, indicatorSelectedRadius, indicatorSelectedRadius, indicatorPaint)
        }
        if (offset > 0.1f) {
            val nextRightX = indicatorStartX(nextPage) + ratioSelectedRadius + if (isNextFirst) distanceOffset else distance
            val nextLeftX = nextRightX - ratioSelectedRadius * 2 - distanceOffset
            rectF[nextLeftX, midY - indicatorSelectedRadius, nextRightX] = midY + indicatorSelectedRadius
            canvas.drawRoundRect(rectF, indicatorSelectedRadius, indicatorSelectedRadius, indicatorPaint)
        }
    }
    private fun drawBigCircle(canvas: Canvas, midY: Float) {
        drawPagerCountCircle(canvas, midY)
        val offset = interpolatedOffset()
        val indicatorStartX = indicatorStartX(selectedPage)
        val nextIndicatorStartX = indicatorStartX((selectedPage + 1) % pagerCount)
        val ratioRadius = getRatioRadius()
        val maxRadius = indicatorSelectedRadius
        val maxRatioRadius = maxRadius * indicatorSelectedRatio
        val leftRadius = maxRatioRadius - (maxRatioRadius - ratioRadius) * offset
        val rightRadius = ratioRadius + (maxRatioRadius - ratioRadius) * offset
        val topOrBottomOffset = (maxRadius - indicatorRadius) * offset
        indicatorPaint.color = selectedColor
        if (offset < 0.99f) {
            val top = midY - maxRadius + topOrBottomOffset
            val left = indicatorStartX - leftRadius
            val right = indicatorStartX + leftRadius
            val bottom = midY + maxRadius - topOrBottomOffset
            rectF[left, top, right] = bottom
            canvas.drawRoundRect(rectF, leftRadius, leftRadius, indicatorPaint)
        }
        if (offset > 0.1f) {
            val top = midY - indicatorRadius - topOrBottomOffset
            val left = nextIndicatorStartX - rightRadius
            val right = nextIndicatorStartX + rightRadius
            val bottom = midY + indicatorRadius + topOrBottomOffset
            rectF[left, top, right] = bottom
            canvas.drawRoundRect(rectF, rightRadius, rightRadius, indicatorPaint)
        }
    }
    private val path = Path()
    private val accelerateInterpolator = AccelerateInterpolator()
    private fun drawBezier(canvas: Canvas, midY: Float) {
        drawPagerCountCircle(canvas, midY)
        val indicatorStartX = indicatorStartX(selectedPage)
        val nextIndicatorStartX = indicatorStartX((selectedPage + 1) % pagerCount)
        val leftX: Float = indicatorStartX + (nextIndicatorStartX - indicatorStartX) * accelerateInterpolator.getInterpolation(offset)
        val rightX = indicatorStartX + (nextIndicatorStartX - indicatorStartX) * interpolatedOffset()
        val ratioSelectedRadius = getRatioSelectedRadius()
        val minRadius = indicatorSelectedRadius * 0.57f
        val minRatioRadius = minRadius * indicatorSelectedRatio
        val leftRadius = ratioSelectedRadius + (minRatioRadius - ratioSelectedRadius) * interpolatedOffset()
        val rightRadius: Float = minRatioRadius + (ratioSelectedRadius - minRatioRadius) * accelerateInterpolator.getInterpolation(offset)
        val leftTopOrBottomOffset = (indicatorSelectedRadius - minRadius) * interpolatedOffset()
        val rightTopOrBottomOffset: Float = (indicatorSelectedRadius - minRadius) * accelerateInterpolator.getInterpolation(offset)
        indicatorPaint.color = selectedColor
        rectF[leftX - leftRadius, midY - indicatorSelectedRadius + leftTopOrBottomOffset, leftX + leftRadius] =
            midY + indicatorSelectedRadius - leftTopOrBottomOffset
        canvas.drawRoundRect(rectF, leftRadius, leftRadius, indicatorPaint)
        rectF[rightX - rightRadius, midY - minRadius - rightTopOrBottomOffset, rightX + rightRadius] = midY + minRadius + rightTopOrBottomOffset
        canvas.drawRoundRect(rectF, rightRadius, rightRadius, indicatorPaint)
        path.reset()
        path.moveTo(rightX, midY)
        path.lineTo(rightX, midY - minRadius - rightTopOrBottomOffset)
        path.quadTo(rightX + (leftX - rightX) / 2.0f, midY, leftX, midY - indicatorSelectedRadius + leftTopOrBottomOffset)
        path.lineTo(leftX, midY + indicatorSelectedRadius - leftTopOrBottomOffset)
        path.quadTo(rightX + (leftX - rightX) / 2.0f, midY, rightX, midY + minRadius + rightTopOrBottomOffset)
        path.close()
        canvas.drawPath(path, indicatorPaint)
    }

    enum class IndicatorStyle {
        Circle, CircleRect, Dash, BigCircle, Bezier
    }
}