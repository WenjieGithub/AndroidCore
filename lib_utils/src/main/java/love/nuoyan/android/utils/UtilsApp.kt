package love.nuoyan.android.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.os.Process
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import kotlin.math.sqrt

object UtilsApp {
    /** 获取进程名称 */
    fun getProcessName(): String? {
        return getProcessName(Utils.appContext)
    }

    // 获取进程名称
    fun getProcessName(context: Context): String? {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        am?.runningAppProcesses?.let { runningApps ->
            for (proInfo in runningApps) {
                if (proInfo.pid == Process.myPid()) {
                    if (proInfo.processName != null) {
                        return proInfo.processName
                    }
                }
            }
        }
        return null
    }

    /** 获取应用版本号 */
    fun getVersionCode(): Long {
        try {
            val pm = Utils.appContext.packageManager
            val packageInfo = pm.getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS)
            return packageInfo.versionCode.toLong()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return -1
    }

    /** 获取应用版本名 */
    fun getVersionName(): String {
        return try {
            val pm = Utils.appContext.packageManager
            val packageInfo = pm.getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    /** 获取包名 */
    fun getPackageName(): String {
        return Utils.appContext.packageName
    }

    /** 获取系统版本 */
    fun getSysVersion(): String? {
        return Build.VERSION.RELEASE
    }

    /** 获取 SDK 版本 */
    fun getSdkVersion(): Int {
        return Build.VERSION.SDK_INT
    }

    /** 获取系统型号 */
    fun getSysModel(): String? {
        return Build.MODEL
    }

    /** 获取 设备厂商 */
    fun getManufacturer(): String? {
        return Build.MANUFACTURER
    }

    /** 获取 手机品牌 */
    fun getBrand(): String? {
        return Build.BRAND
    }

    /** 获取 UUID */
    fun getUUID(): String {
        return UUID.randomUUID().toString()
    }

    /** 获取 DeviceId */
    fun getDeviceId(): String {
//    var OAID: String? = UtilsKV.getString("Key_OAID")
//    var WebId: String? = UtilsKV.getString("Key_WebId")
//    var StandbyId: String? = UtilsKV.getString("Key_StandbyId")
        var id = UtilsKV.getString("getDeviceIdKey")
        if (id.isNullOrEmpty()) {
            val merge = "${System.currentTimeMillis()}${getBrand()}${getManufacturer()}${getUUID()}"
            id = UtilsEncrypt.md5(merge)
            UtilsKV.put("getDeviceIdKey", id)
        }
        UtilsLog.log("getDeviceId: $id")
        return id
    }

    /** 退出应用 */
    fun exitAPP() {
        (Utils.appContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.appTasks?.let { appTaskList ->
            for (appTask in appTaskList) {
                appTask.finishAndRemoveTask()
            }
        }
    }

    /** 弹出软键盘 */
    fun showInput(view: View, isOpenAnimation: Boolean) {
        view.isFocusable = true
        view.requestFocus()
        if (isOpenAnimation) {
            val shake = AnimationUtils.loadAnimation(view.context, R.anim.lib_utils_edit_shake_anim)
            view.startAnimation(shake)
        }
        view.isFocusableInTouchMode = true
        val inputManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputManager?.showSoftInput(view, 0)
    }

    /** 隐藏软键盘 */
    fun hideInput(activity: Activity) {
        try {
            val im = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            val v = activity.currentFocus
            if (im != null && v != null) {
                im.hideSoftInputFromWindow(
                    v.applicationWindowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        } catch (e: Exception) {
            UtilsLog.logW(e.toString())
        }
    }

    /** 去除了状态栏等装饰，并不准确 */
    fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    /** 屏幕绝对高度 */
    fun getScreenRealHeight(windowManager: WindowManager): Int {
        val outSize = Point()
        windowManager.defaultDisplay.getRealSize(outSize)
        return outSize.y
    }

    /** 屏幕宽度 */
    fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    /** 获取状态栏高度 */
    fun getStatusBarHeight(): Int {
        val resources = Resources.getSystem()
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            35.dp2px.toInt()
        }
    }

    /** 是否存在 NavigationBar */
    fun hasNavigationBar(): Boolean {
        val wm = Utils.appContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        if (wm != null) {
            val display = wm.defaultDisplay
            val size = Point()
            val realSize = Point()
            display.getSize(size)
            display.getRealSize(realSize)
            return realSize.x != size.x || realSize.y != size.y
        }
        return true
    }

    /** 获取 NavigationBar 的高度 */
    fun getNavigationBarHeight(): Int {
        return if (hasNavigationBar()) {
            val resources = Resources.getSystem()
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    /** 控制键盘不遮挡布局 */
    fun controlKeyboardLayout(root: View, scrollToView: View) {
        root.viewTreeObserver.addOnGlobalLayoutListener {
            val rectR = Rect()
            root.getWindowVisibleDisplayFrame(rectR)                            // 获取root在窗体的可视区域
            val rootInvisibleHeight = root.height - (rectR.bottom - rectR.top)  // 当前视图高度减去现在所看到的视图的高度
            if (rootInvisibleHeight > 200) {                                    // 若rootInvisibleHeight高度大于100，则说明当前视图上移了，说明软键盘弹出了
                val yr = root.y + root.height
                val ys = scrollToView.y + scrollToView.height                   // 软键盘弹出来的时候
                val scrollHeight = rootInvisibleHeight - (yr - ys)              // 平移的高度和两个view底部差值
                if (scrollHeight > 0) {
                    scrollToView.translationY = -scrollHeight
                }
            } else {
                scrollToView.translationY = 0f                                  // 软键盘没有弹出来的时候
            }
        }
    }

    /** 是否 Root */
    fun isDeviceRooted(): Boolean {
        val su = "su"
        val locations = arrayOf(
            "/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/",
            "/system/bin/failsafe/", "/data/local/xbin/", "/data/local/bin/", "/data/local/",
            "/system/sbin/", "/usr/bin/", "/vendor/bin/"
        )
        for (location in locations) {
            if (File(location + su).exists()) {
                return true
            }
        }
        return false
    }

    /** 是否首次安装这个版本 */
    fun isFirstInstallationVersion(): Boolean {
        return UtilsKV.get("isFirstInstallationVersion", "") != getVersionName()
    }

    /** 首次安装这个版本，添加标记 */
    fun putFirstInstallationVersionTag() {
        UtilsKV.put("isFirstInstallationVersion", getVersionName())
    }

    /** 是否首次安装这个应用 */
    fun isFirstInstallationApp(): Boolean {
        return UtilsKV.get("isFirstInstallationApp", true)
    }

    /** 首次安装这个应用, 添加标记 */
    fun putFirstInstallationAppTag() {
        UtilsKV.put("isFirstInstallationApp", false)
    }

    /** 获取 IP 地址 */
    fun getIPAddress(useIPv4: Boolean = true): String? {
        try {
            val nis = NetworkInterface.getNetworkInterfaces()
            val adds = LinkedList<InetAddress>()
            while (nis.hasMoreElements()) {
                val ni = nis.nextElement()
                // To prevent phone of xiaomi return "10.0.2.15"
                if (!ni.isUp || ni.isLoopback) continue
                val addresses = ni.inetAddresses
                while (addresses.hasMoreElements()) {
                    adds.addFirst(addresses.nextElement())
                }
            }
            for (add in adds) {
                if (!add.isLoopbackAddress) {
                    val hostAddress = add.hostAddress
                    val isIPv4 = hostAddress.indexOf(':') < 0
                    if (useIPv4) {
                        if (isIPv4) return hostAddress
                    } else {
                        if (!isIPv4) {
                            val index = hostAddress.indexOf('%')
                            return if (index < 0) hostAddress.toUpperCase(Locale.ROOT) else hostAddress.substring(0, index).toUpperCase(Locale.ROOT)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /** 获取屏幕尺寸(英寸) */
    fun getScreenInch(activity: Activity): Double {
        var inch = 0.0
        try {
            val realWidth: Int
            val realHeight: Int
            val display = activity.windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)

            val size = Point()
            display.getRealSize(size)
            realWidth = size.x
            realHeight = size.y

            inch = formatDouble(sqrt(realWidth / metrics.xdpi * (realWidth / metrics.xdpi) + realHeight / metrics.ydpi * (realHeight / metrics.ydpi).toDouble()))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return inch
    }
    private fun formatDouble(d: Double, newScale: Int = 1): Double {
        val bd = BigDecimal(d)
        return bd.setScale(newScale, RoundingMode.HALF_UP).toDouble()
    }

    /** 是否为 Pad, 屏幕尺寸大于 inch(默认7) 英寸或系统获取为 Pad */
    fun isPad(activity: Activity, inch: Int = 7): Boolean {
        val isPad = (Utils.appContext.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
        val screen = getScreenInch(activity) > inch
        return screen || isPad
    }

    /** 获取随机数 */
    fun getRandom(min: Int, max: Int): Int {
        return (min..max).random()
    }
}

