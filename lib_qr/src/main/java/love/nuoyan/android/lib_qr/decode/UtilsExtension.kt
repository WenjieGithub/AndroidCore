package love.nuoyan.android.lib_qr.decode

import android.content.res.Resources
import android.util.TypedValue

internal val Int.dp2px
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics)