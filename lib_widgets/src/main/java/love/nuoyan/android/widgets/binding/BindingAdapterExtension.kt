package love.nuoyan.android.widgets.binding

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableString
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.bumptech.glide.Glide
import com.bumptech.glide.request.BaseRequestOptions
import com.bumptech.glide.signature.ObjectKey
import love.nuoyan.android.widgets.banner.Banner
import love.nuoyan.android.widgets.dp2px

@BindingAdapter("android:layout_width")
fun View.setViewWidth(width: Int) {
    val params = this.layoutParams
    params.width = width
    this.requestLayout()
}

@BindingAdapter("android:layout_height")
fun View.setViewHeight(height: Int) {
    val params = this.layoutParams
    params.height = height
    this.requestLayout()
}

@BindingMethods(value = [BindingMethod(type = ImageView::class, attribute = "tint", method = "setImageTintList")])
class BindingMethodsTint

@BindingAdapter("tint")
fun ImageView.setTint(tint: Int) {
    imageTintList = ColorStateList.valueOf(tint)
}

@BindingAdapter("drawableTint")
fun TextView.setDrawableTint(color: Int) {
    TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(color))
}

@set:BindingAdapter("visibleOrGone")
var View.visibleOrGone
    get() = visibility == View.VISIBLE
    set(value) { visibility = if (value) View.VISIBLE else View.GONE }

@set:BindingAdapter("visible")
var View.visible
    get() = visibility == View.VISIBLE
    set(value) { visibility = if (value) View.VISIBLE else View.INVISIBLE }

@BindingAdapter(value = ["imageUrl", "imageError", "imagePlaceholder", "imageSignature", "imageOptions"], requireAll = false)
fun ImageView.setImageUrl(url: String?, error: Drawable?, placeholder: Drawable?, signature: Any?, options : BaseRequestOptions<*>?) {
    var request = Glide.with(this).load(url)
    signature?.let { request = request.signature(ObjectKey(it)) }
    placeholder?.let { request = request.placeholder(it) }
    error?.let { request = request.error(it) }
    options?.let { request = request.apply(options) }
    request.into(this)
}

@BindingAdapter(
    value = [
        "bg_alpha",
        "bg_solidColor",
        "bg_radius",
        "bg_radiusTopLeft",
        "bg_radiusTopRight",
        "bg_radiusBottomLeft",
        "bg_radiusBottomRight",
        "bg_strokeWidth",
        "bg_strokeColor",
        "bg_gradientColors",
        "bg_gradientType",
        "bg_gradientOrientation",
        "bg_gradientRadius",
        "bg_gradientCenterX",
        "bg_gradientCenterY"],
    requireAll = false)
fun View.setBackgroundDrawable(
    alpha: Int? = null,
    color: Int? = null,
    radius: Float? = null,
    radiusTopLeft: Float? = null,
    radiusTopRight: Float? = null,
    radiusBottomLeft: Float? = null,
    radiusBottomRight: Float? = null,
    sWidth: Float? = null,
    sColor: Int? = null,
    gColors: IntArray? = null,
    gType: Int = 0,
    gOrientation: Int = 0,
    gRadius: Float = 0f,
    gCenterX: Float = 0.5f,
    gCenterY: Float = 0.5f
) {
    if (color != null || (sWidth != null && sWidth > 0 && sColor != null) || gColors != null) {
        val drawable = GradientDrawable()
        alpha?.let { drawable.alpha = alpha }
        color?.let { drawable.setColor(it) }
        radius?.let { drawable.cornerRadius = radius.dp2px }
        radiusTopLeft?.let {
            val radiusArray = FloatArray(8)
            radiusArray[0] = radiusTopLeft.dp2px; radiusArray[1] = radiusArray[0]
            radiusTopRight?.let { radiusArray[2] = it.dp2px; radiusArray[3] = radiusArray[2] }
            radiusBottomLeft?.let { radiusArray[4] = it.dp2px; radiusArray[5] = radiusArray[4] }
            radiusBottomRight?.let { radiusArray[6] = it.dp2px; radiusArray[7] = radiusArray[6] }
            drawable.cornerRadii = radiusArray
        }
        sWidth?.let { sColor?.let { drawable.setStroke(sWidth.dp2px.toInt(), sColor) } }
        gColors?.let {
            drawable.gradientType = gType
            drawable.colors = gColors
            drawable.gradientRadius = gRadius
            drawable.setGradientCenter(gCenterX, gCenterY)
            drawable.orientation = when (gOrientation) {
                0 -> GradientDrawable.Orientation.LEFT_RIGHT
                1 -> GradientDrawable.Orientation.TOP_BOTTOM
                else -> GradientDrawable.Orientation.LEFT_RIGHT
            }
        }
        this.background = drawable
    }
}

@BindingAdapter(
    value = [
        "bg_alpha",
        "bg_solidColor",
        "bg_radius",
        "bg_radiusTopLeft",
        "bg_radiusTopRight",
        "bg_radiusBottomLeft",
        "bg_radiusBottomRight",
        "bg_strokeWidth",
        "bg_strokeColor",
        "bg_gradientColors",
        "bg_gradientType",
        "bg_gradientOrientation",
        "bg_gradientRadius",
        "bg_gradientCenterX",
        "bg_gradientCenterY"],
    requireAll = false)
fun View.setBackgroundDrawable(
    alpha: Int? = null,
    color: String? = null,
    radius: Float? = null,
    radiusTopLeft: Float? = null,
    radiusTopRight: Float? = null,
    radiusBottomLeft: Float? = null,
    radiusBottomRight: Float? = null,
    sWidth: Float? = null,
    sColor: String? = null,
    gColors: String? = null,
    gType: Int = 0,
    gOrientation: Int = 0,
    gRadius: Float = 0f,
    gCenterX: Float = 0.5f,
    gCenterY: Float = 0.5f
) {
    if (color != null || (sWidth != null && sWidth > 0 && sColor != null) || gColors != null) {
        val drawable = GradientDrawable()
        alpha?.let { drawable.alpha = alpha }
        color?.let { drawable.setColor(Color.parseColor(if (color.startsWith("#")) color else "#$color")) }
        radius?.let { drawable.cornerRadius = radius.dp2px }
        radiusTopLeft?.let {
            val radiusArray = FloatArray(8)
            radiusArray[0] = radiusTopLeft.dp2px; radiusArray[1] = radiusArray[0]
            radiusTopRight?.let { radiusArray[2] = it.dp2px; radiusArray[3] = radiusArray[2] }
            radiusBottomLeft?.let { radiusArray[4] = it.dp2px; radiusArray[5] = radiusArray[4] }
            radiusBottomRight?.let { radiusArray[6] = it.dp2px; radiusArray[7] = radiusArray[6] }
            drawable.cornerRadii = radiusArray
        }
        sWidth?.let { sColor?.let { drawable.setStroke(sWidth.dp2px.toInt(), Color.parseColor(if (sColor.startsWith("#")) sColor else "#$sColor")) } }
        gColors?.let {
            drawable.gradientType = gType

            drawable.colors = gColors.split(",").map { sc -> Color.parseColor(if (sc.startsWith("#")) sc else "#$sc") }.toIntArray()
            drawable.gradientRadius = gRadius
            drawable.setGradientCenter(gCenterX, gCenterY)
            drawable.orientation = when (gOrientation) {
                0 -> GradientDrawable.Orientation.LEFT_RIGHT
                1 -> GradientDrawable.Orientation.TOP_BOTTOM
                else -> GradientDrawable.Orientation.LEFT_RIGHT
            }
        }
        this.background = drawable
    }
}

@BindingAdapter("co_radius")
fun View.clipOutline(radius: Float) {
    if (radius == 0f) {
        clipToOutline = false
    } else {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, width, height, radius.dp2px)
            }
        }
        clipToOutline = true
    }
    invalidateOutline()
}


@BindingAdapter(
    value = [
        "shadow_roundRadiusTopLeft",
        "shadow_roundRadiusTopRight",
        "shadow_roundRadiusBottomRight",
        "shadow_roundRadiusBottomLeft",
        "shadow_radius",
        "shadow_color",
        "shadow_offsetX",
        "shadow_offsetY",
        "shadow_padding"],
    requireAll = false
)
fun View.addShadow(
    roundRadiusTopLeft: Float = 0f,
    roundRadiusTopRight: Float = 0f,
    roundRadiusBottomRight: Float = 0f,
    roundRadiusBottomLeft: Float = 0f,
    shadowRadius: Float = 0f,
    shadowColor: Int = Color.TRANSPARENT,
    offsetX: Float = 0f,
    offsetY: Float = 0f,
    padding: Float = 0f
) {
    post {
        if (shadowRadius > 0 && shadowColor != Color.TRANSPARENT && width > 0 && height > 0) {
            val sr = shadowRadius.dp2px
            val p = padding.dp2px

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint()
            val areas = RectF()
            val clipPath = Path()
            val radiusArray = FloatArray(8)

            val w = width.toFloat() - sr - p
            val h = height.toFloat() - sr - p
            areas.left = sr + offsetX.dp2px + p
            areas.top = sr + offsetY.dp2px + p
            areas.right = w
            areas.bottom = h

            radiusArray[0] = roundRadiusTopLeft.dp2px
            radiusArray[1] = radiusArray[0]
            radiusArray[2] = roundRadiusTopRight.dp2px
            radiusArray[3] = radiusArray[2]
            radiusArray[4] = roundRadiusBottomRight.dp2px
            radiusArray[5] = radiusArray[4]
            radiusArray[6] = roundRadiusBottomLeft.dp2px
            radiusArray[7] = radiusArray[6]

            clipPath.reset()
            clipPath.addRoundRect(areas, radiusArray, Path.Direction.CW)

            paint.maskFilter = BlurMaskFilter(sr, BlurMaskFilter.Blur.NORMAL)
            paint.style = Paint.Style.FILL
            paint.color = shadowColor
            canvas.drawPath(clipPath, paint)

            background = BitmapDrawable(resources, bitmap)
        }
        invalidateOutline()
    }
}

@BindingAdapter(
    value = [
        "drawableStart", "drawableStartWidth", "drawableStartHeight",
        "drawableTop", "drawableTopWidth", "drawableTopHeight",
        "drawableEnd", "drawableEndWidth", "drawableEndHeight",
        "drawableBottom", "drawableBottomWidth", "drawableBottomHeight"],
    requireAll = false
)
fun TextView.setDrawableStart(
    drawableStart: Drawable?, drawableStartWidth: Int?, drawableStartHeight: Int?,
    drawableTop: Drawable?, drawableTopWidth: Int?, drawableTopHeight: Int?,
    drawableEnd: Drawable?, drawableEndWidth: Int?, drawableEndHeight: Int?,
    drawableBottom: Drawable?, drawableBottomWidth: Int?, drawableBottomHeight: Int?,
) {
    drawableStart?.let {
        val width = drawableStartWidth ?: it.intrinsicWidth
        val height = drawableStartHeight ?: it.intrinsicHeight
        if (width >= 0 && height >= 0) {
            it.setBounds(0, 0, width, height)
        }
    }
    drawableTop?.let {
        val width = drawableTopWidth ?: it.intrinsicWidth
        val height = drawableTopHeight ?: it.intrinsicHeight
        if (width >= 0 && height >= 0) {
            it.setBounds(0, 0, width, height)
        }
    }
    drawableEnd?.let {
        val width = drawableEndWidth ?: it.intrinsicWidth
        val height = drawableEndHeight ?: it.intrinsicHeight
        if (width >= 0 && height >= 0) {
            it.setBounds(0, 0, width, height)
        }
    }
    drawableBottom?.let {
        val width = drawableBottomWidth ?: it.intrinsicWidth
        val height = drawableBottomHeight ?: it.intrinsicHeight
        if (width >= 0 && height >= 0) {
            it.setBounds(0, 0, width, height)
        }
    }
    setCompoundDrawables(drawableStart, drawableTop, drawableEnd, drawableBottom)
}

@BindingAdapter(value = ["spanSource", "spanIcon", "spanDrawable", "spanWidth", "spanHeight"], requireAll = false)
fun TextView.appendIcon(spanSource: CharSequence = "icon", spanIcon: Int?, spanDrawable: Drawable?, spanWidth: Int = 0, spanHeight: Int = 0) {
    val spanString = when {
        spanIcon != null -> {
            SpannableString(spanSource).apply { setSpan(DiyImageSpan(context, spanIcon, spanWidth, spanHeight), 0, spanSource.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
        }
        spanDrawable != null -> {
            SpannableString(spanSource).apply { setSpan(DiyImageSpan(spanDrawable, spanWidth, spanHeight), 0, spanSource.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
        }
        else -> { null }
    }
    spanString?.let { append(spanString) }
}

/**
 * @param cycle         是否可以循环滚动
 * @param autoScroll    是否可以自动滚动
 * @param margin        item 之间的间距，单位 dp
 * @param tlWidth       item 左边页面显露出来的宽度，单位 dp
 * @param brWidth       item 右边页面显露出来的宽度，单位 dp
 * @param intervalTime  如果可以自动滚动，则滚动的间隔时间为 time
 * @param direction     Banner 滚动的方向，为 0 则横向滚动，否则为纵向滚动
 */
@BindingAdapter(value = ["isCycle", "isAutoScroll", "margin", "tlWidth", "brWidth", "intervalTime", "direction"], requireAll = false)
fun Banner.initBanner(cycle: Boolean = false, autoScroll: Boolean = false, margin: Int = 0, tlWidth: Int = 0, brWidth: Int = 0, intervalTime: Long = 4000, direction: Int = 0) {
    setBanner(cycle, autoScroll, margin, tlWidth, brWidth, intervalTime, direction)
}