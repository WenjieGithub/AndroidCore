package love.nuoyan.android.utils

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import love.nuoyan.android.utils.Utils.appContext
import java.util.*

/**
 * 描述: Toast 的封装使用工具类
 */
object UtilsToast {
    private var mToast: Toast? = null

    var textSize = 15f
    @ColorInt var textColor = Color.parseColor("#FFf8f8f8")
    @ColorInt var background = Color.parseColor("#b3000000")

    var xOffset = 0
    var yOffset = 176
    var gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM

    fun show(@StringRes resId: Int, vararg args: Any?) {
        try {
            show(String.format(appContext.resources.getString(resId), *args).trim { it <= ' ' })
        } catch (e: Exception) {
            show(appContext.resources.getString(resId))
            UtilsLog.log(Arrays.toString(e.stackTrace), "UtilsToast")
        }
    }
    fun show(format: CharSequence, vararg args: Any?) {
        try {
            show(String.format(format.toString(), *args).trim { it <= ' ' })
        } catch (e: Exception) {
            show(format)
            UtilsLog.log(Arrays.toString(e.stackTrace), "UtilsToast")
        }
    }
    private fun show(format: CharSequence) {
        Utils.appScope.launch(Main) {
            mToast?.cancel()
            val duration = if (format.length > 15) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            mToast = Toast(appContext)

            val view = TextView(appContext)
            view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            view.setPadding(8.dp2px.toInt(), 3.dp2px.toInt(), 8.dp2px.toInt(), 3.dp2px.toInt())
            view.text = format
            view.textSize = textSize
            view.setTextColor(textColor)
            view.background = GradientDrawable().apply {
                setColor(background)
                cornerRadius = 10f
            }
            mToast?.view = view
            mToast?.setGravity(gravity, xOffset, yOffset)
            mToast?.duration = duration
            mToast?.show()
        }
    }

    fun show(@DrawableRes imgRes: Int, format: CharSequence, gravity: Int = UtilsToast.gravity) {
        Utils.appScope.launch(Main) {
            mToast?.cancel()
            val duration = if (format.length > 15) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            mToast = Toast(appContext)

            val view = TextView(appContext)
            view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            view.setPadding(8.dp2px.toInt(), 3.dp2px.toInt(), 8.dp2px.toInt(), 3.dp2px.toInt())
            view.text = format
            view.textSize = textSize
            view.setTextColor(textColor)
            view.background = GradientDrawable().apply {
                setColor(background)
                cornerRadius = 10f
            }
            view.gravity = Gravity.CENTER_HORIZONTAL
            view.setCompoundDrawablesWithIntrinsicBounds(0, imgRes, 0, 0)
            view.compoundDrawablePadding = 5.dp2px.toInt()

            mToast?.view = view
            mToast?.setGravity(gravity, xOffset, yOffset)
            mToast?.duration = duration
            mToast?.show()
        }
    }
}