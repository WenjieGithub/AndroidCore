package love.nuoyan.android.utils

import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import love.nuoyan.android.utils.Utils

object UtilsNotification {
    /**
     * 检查通知推送的开关状态
     */
    fun checkPushSwitchStatus(): Boolean {
        return NotificationManagerCompat.from(Utils.appContext).areNotificationsEnabled()
    }

    /**
     * 跳转到 APP 的通知设置界面
     * 部分国产手机中没有 APP 通知设置页面
     * 部分国产手机 APP 通知设置界面中没有开启和关闭的操作
     */
    fun toNotificationSetting() {
        try {
            val intent = Intent()
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            // 8.0及以后版本使用这两个 extra.  >=API 26
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, Utils.appContext.packageName)
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, Utils.appContext.applicationInfo.uid)
            // 5.0-7.1 使用这两个 extra.  <= API 25, >=API 21
            intent.putExtra("app_package", Utils.appContext.packageName)
            intent.putExtra("app_uid", Utils.appContext.applicationInfo.uid)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            Utils.appContext.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // 其他低版本或者异常情况，走该节点。进入APP设置界面
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.putExtra("package", Utils.appContext.packageName)
            // val uri = Uri.fromParts("package", packageName, null)
            // intent.data = uri
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            Utils.appContext.startActivity(intent)
        }
    }
}