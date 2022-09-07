package love.nuoyan.android.floating_kit.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.res.Resources
import android.graphics.Point
import android.os.Environment
import android.util.DisplayMetrics
import android.view.WindowManager
import java.io.File
import java.math.BigDecimal
import java.net.InetAddress
import java.net.NetworkInterface
import java.security.MessageDigest
import java.util.*
import kotlin.math.sqrt


internal fun Float.dip2px(): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (this * scale + 0.5f).toInt()
}

object Extension {
    lateinit var application: Application

    /** 屏幕宽度 */
    fun getScreenWidth(): Int {
        val dm = Resources.getSystem().displayMetrics
        return dm.widthPixels
    }

    /** 去除了状态栏等装饰，并不准确 */
    fun getScreenHeight(): Int {
        val dm = Resources.getSystem().displayMetrics
        return dm.heightPixels
    }

    /** 获取状态栏高度 */
    fun getStatusBarHeight(): Int {
        val resources = Resources.getSystem()
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            30f.dip2px()
        }
    }

    /** 获取应用存储目录 */
    fun getAppDir(context: Context, type: String?): File? {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.getExternalFilesDir(type)
        } else {
            context.filesDir
        }
    }

    /** 获取应用缓存目录 */
    fun getAppCacheDir(context: Context): File? {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.externalCacheDir
        } else {
            context.cacheDir
        }
    }

    /** 删除目录及目录下的文件 */
    fun deleteDirectory(dirFile: File): Boolean {
        if (dirFile.exists() && dirFile.isDirectory) {
            var flag = true
            dirFile.listFiles()?.let {                  // 删除文件夹中的所有文件包括子目录
                for (file in it) {                      // 删除子文件
                    if (file.isFile) {
                        flag = deleteFile(file)
                        if (!flag) break
                    } else if (file.isDirectory) {
                        flag = deleteDirectory(file)
                        if (!flag) break
                    }
                }
            }
            if (flag) {
                return dirFile.delete()                 // 删除当前目录
            }
        }
        return false
    }

    /** 删除文件 */
    private fun deleteFile(file: File): Boolean {
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    /** 获取应用版本名 */
    @SuppressLint("PackageManagerGetSignatures")
    fun getVersionName(): String? {
        try {
            val pm: PackageManager = application.packageManager
            val packageInfo: PackageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pm.getPackageInfo(application.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                pm.getPackageInfo(application.packageName, PackageManager.GET_SIGNATURES)
            }
            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    fun getIPAddress(useIPv4: Boolean): String? {
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

    fun isDeviceRooted(): Boolean {
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun getAppSignature(packageName: String): Array<Signature>? {
        return if (packageName.isNotEmpty()) {
            try {
                val pm: PackageManager = application.packageManager
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val pi = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    pi?.signingInfo?.apkContentsSigners
                } else {
                    null
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    fun getAppSignatureHash(packageName: String, algorithm: String): String? {
        return if (packageName.isNotEmpty()) {
            val signature = getAppSignature(packageName)
            if (signature.isNullOrEmpty()) {
                ""
            } else {
                when (algorithm) {
                    "MD5" -> md5(signature[0].toByteArray()).replace("(?<=[0-9A-F]{2})[0-9A-F]{2}".toRegex(), ":$0")
                    "SHA1" -> sha1(signature[0].toByteArray()).replace("(?<=[0-9A-F]{2})[0-9A-F]{2}".toRegex(), ":$0")
                    "SHA256" -> sha256(signature[0].toByteArray()).replace("(?<=[0-9A-F]{2})[0-9A-F]{2}".toRegex(), ":$0")
                    else -> ""
                }
            }
        } else {
            ""
        }
    }

    fun md5(array: ByteArray): String {
        val digest = MessageDigest.getInstance("MD5")
        val result = digest.digest(array)
        return toHex(result)                        // 转成16进制后是32字节
    }

    fun sha1(byteArray: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val result = digest.digest(byteArray)
        return toHex(result)
    }

    fun sha256(byteArray: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val result = digest.digest(byteArray)
        return toHex(result)
    }

    // 转成16进制
    private fun toHex(byteArray: ByteArray): String {
        return with(StringBuilder()) {
            byteArray.forEach {
                val hex = it.toInt() and (0xFF)
                val hexStr = Integer.toHexString(hex).toUpperCase(Locale.ROOT)
                if (hexStr.length == 1) {
                    append("0").append(hexStr)
                } else {
                    append(hexStr)
                }
            }
            toString()
        }
    }


    /**
     * 屏幕绝对高度
     */
    fun getScreenRealHeight(windowManager: WindowManager): Int {
        val outSize = Point()
        windowManager.defaultDisplay.getRealSize(outSize)
        return outSize.y
    }

    fun getScreenInch(context: Activity): Double {
        var inch = 0.0
        try {
            val realWidth: Int
            val realHeight: Int
            val display = context.windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)

            val size = Point()
            display.getRealSize(size)
            realWidth = size.x
            realHeight = size.y

            inch = formatDouble(
                sqrt(realWidth / metrics.xdpi * (realWidth / metrics.xdpi) + realHeight / metrics.ydpi * (realHeight / metrics.ydpi).toDouble()),
                1
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return inch
    }

    /**
     * Double类型保留指定位数的小数，返回double类型（四舍五入）
     * newScale 为指定的位数
     */
    private fun formatDouble(d: Double, newScale: Int): Double {
        val bd = BigDecimal(d)
        return bd.setScale(newScale, BigDecimal.ROUND_HALF_UP).toDouble()
    }
}

data class LogInfo(
    val tag: String,            // 标记
    val msg: String,            // 日志消息
    var time: String = "",      // 调用时间
    var thread: String = "",    // 线程
    var method: String = ""     // 调用方法信息
) {
    override fun toString(): String {
        return "$thread   $time   $msg"
    }
}