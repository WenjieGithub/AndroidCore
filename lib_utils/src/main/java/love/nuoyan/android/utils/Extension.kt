package love.nuoyan.android.utils

import android.content.res.Resources
import android.util.TypedValue

val Float.dp2px
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )
val Int.dp2px
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

val Float.sp2px
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )
val Int.sp2px
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

val Float.px2dp
    get() = this / Resources.getSystem().displayMetrics.density
val Int.px2dp
    get() = this / Resources.getSystem().displayMetrics.density


val Float.px2sp
    get() = this / Resources.getSystem().displayMetrics.scaledDensity
val Int.px2sp
    get() = this / Resources.getSystem().displayMetrics.scaledDensity