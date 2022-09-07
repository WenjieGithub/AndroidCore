package love.nuoyan.android.core.example

import android.content.res.Resources
import android.util.TypedValue

val Int.dp2px
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )