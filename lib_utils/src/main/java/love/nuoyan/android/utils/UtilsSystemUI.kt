package love.nuoyan.android.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object UtilsSystemUI {
    /**
     * 沉浸式设置
     * @param decorFitsSystemWindows 表示是否沉浸，false 表示沉浸，true表示不沉浸
     */
    fun setDecorFitsSystemWindows(window: Window, decorFitsSystemWindows: Boolean = false) {
        WindowCompat.setDecorFitsSystemWindows(window, decorFitsSystemWindows)
    }
    /**
     * 设置状态栏底色，默认透明
     */
    fun setStatusBarColor(window: Window, statusBarColor: Int = Color.TRANSPARENT) {
        window.statusBarColor = statusBarColor
    }
    /**
     * 设置导航栏底色，默认透明
     */
    fun setNavigationBarColor(window: Window, navigationBarColor: Int = Color.TRANSPARENT, dividerColor: Int = Color.TRANSPARENT) {
        window.navigationBarColor = navigationBarColor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = dividerColor
        }
    }
    /**
     * 设置状态栏字体的颜色
     */
    fun setStatusBarsAppearanceLight(activity: Activity, appearanceLight: Boolean) {
        WindowCompat.getInsetsController(activity.window, activity.findViewById(android.R.id.content)).isAppearanceLightStatusBars = appearanceLight
    }
    /**
     * 设置导航栏字体的颜色
     */
    fun setNavigationBarsAppearanceLight(activity: Activity, appearanceLight: Boolean) {
        WindowCompat.getInsetsController(activity.window, activity.findViewById(android.R.id.content)).isAppearanceLightNavigationBars = appearanceLight
    }
    /**
     * 设置全屏
     */
    fun hideSystemUI(activity: Activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    /**
     * 退出全屏
     */
    fun showSystemUI(activity: Activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, true)
        val controller = WindowCompat.getInsetsController(activity.window, activity.findViewById(android.R.id.content))
        controller.show(WindowInsetsCompat.Type.systemBars())
    }
    /**
     * 隐藏状态栏
     */
    fun hideStatusBars(activity: Activity) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.findViewById(android.R.id.content))
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }
    /**
     * 显示状态栏
     */
    fun showStatusBars(activity: Activity) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.findViewById(android.R.id.content))
        controller.show(WindowInsetsCompat.Type.statusBars())
    }
    /**
     * 隐藏导航栏
     */
    fun hideNavigationBars(activity: Activity) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.findViewById(android.R.id.content))
        controller.hide(WindowInsetsCompat.Type.navigationBars())
    }
    /**
     * 显示导航栏
     */
    fun showNavigationBars(activity: Activity) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.findViewById(android.R.id.content))
        controller.show(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    /**
     * 是否正在显示导航栏
     */
    fun hasNavigationBars(activity: Activity): Boolean {
        val windowInsetsCompat = ViewCompat.getRootWindowInsets(activity.findViewById(android.R.id.content)) ?: return false
        return windowInsetsCompat.isVisible(WindowInsetsCompat.Type.navigationBars())
                    && windowInsetsCompat.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom > 0
    }
    /**
     * 获取导航栏高度
     */
    fun getNavigationBarsHeight(activity: Activity): Int {
        val windowInsetsCompat = ViewCompat.getRootWindowInsets(activity.findViewById(android.R.id.content)) ?: return 0
        return windowInsetsCompat.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    }
    /**
     * 获取状态栏的高度
     */
    fun getStatusBarsHeight(activity: Activity): Int {
        val windowInsetsCompat = ViewCompat.getRootWindowInsets(activity.findViewById(android.R.id.content)) ?: return 0
        return windowInsetsCompat.getInsets(WindowInsetsCompat.Type.statusBars()).top
    }
    /**
     * 隐藏键盘
     */
    fun hideSoftKeyboard(activity: Activity) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.findViewById(android.R.id.content))
        controller.hide(WindowInsetsCompat.Type.ime())
    }
    /**
     * 显示键盘
     */
    fun showSoftKeyboard(activity: Activity) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.findViewById(android.R.id.content))
        controller.show(WindowInsetsCompat.Type.ime())
    }
    /**
     * EditText 打开键盘
     */
    fun editShowKeyBoard(editText: EditText) {
        editText.isEnabled = true
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        val inputManager = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        editText.setSelection(editText.text.length)
        inputManager.showSoftInput(editText, 0)
    }
    /**
     * 监听键盘高度变化
     */
    fun addKeyBordHeightChangeCallBack(view: View, onAction:(height:Int) ->Unit){
        var posBottom: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val cb = object : WindowInsetsAnimation.Callback(DISPATCH_MODE_STOP) {
                override fun onProgress(insets: WindowInsets, animations: MutableList<WindowInsetsAnimation>): WindowInsets {
                    posBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom + insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                    onAction.invoke(posBottom)
                    return insets
                }
            }
            view.setWindowInsetsAnimationCallback(cb)
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
                posBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom + insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                onAction.invoke(posBottom)
                insets
            }
        }
    }



    /** 设置状态栏透明 */
    @Deprecated("")
    fun setStatusTranslucent(activity: Activity) {
        // 5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        // 两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        // 导航栏颜色也可以正常设置
        // window.navigationBarColor = Color.TRANSPARENT
    }
    /** 代码实现 android:fitsSystemWindows */
    @Deprecated("")
    fun setRootViewFitsSystemWindows(activity: Activity, fitSystemWindows: Boolean) {
        val winContent = activity.findViewById<ViewGroup>(android.R.id.content)
        if (winContent.childCount > 0) {
            val rootView = winContent.getChildAt(0) as ViewGroup
            rootView.fitsSystemWindows = fitSystemWindows
        }
    }
    @Deprecated("")
    @SuppressLint("PrivateApi")
    fun setSemiTransparentStatusBarColor(window: Window) {
        try {
            val decorViewClazz = Class.forName("com.android.internal.policy.DecorView")
            val field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor")
            field.isAccessible = true
            field.setInt(window.decorView, Color.TRANSPARENT) // 去掉高版本蒙层改为透明
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}