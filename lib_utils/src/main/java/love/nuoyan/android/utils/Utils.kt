package love.nuoyan.android.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope

/**
 * 基础工具类
 */
object Utils {
    lateinit var appContext: Context                                        // Application
        private set
    lateinit var appScope: CoroutineScope                           // 携程空间
        private set
    var isDebug = false                                                     // 是否 debug 模式
        private set
    var isAllPackage = false                                                // 是否全量打包，组件化用
        private set
    val separatorLine = System.getProperty("line.separator") ?: "\n"        // 换行符


    /**
     * UtilsId 需要另行初始化
     * @param context
     * @param applicationScope  应用级携程空间
     * @param debug             是否测试模式
     * @param key               日志及kv缓存密钥
     * @param defaultChannel    默认的渠道地址
     */
    fun init(context: Context, applicationScope: CoroutineScope, debug: Boolean, allPackage: Boolean, key: String, defaultChannel: String) {
        appContext = context.applicationContext
        appScope = applicationScope
        isDebug = debug
        isAllPackage = allPackage
        // 初始化顺序不能变
        UtilsLog.init(appContext)
        UtilsKV.init(appContext, key)
        UtilsChannel.init(defaultChannel)
        UtilsLog.log(
            StringBuilder()
                .append(separatorLine)
                .append("MODEL: ${UtilsApp.getSysModel()}$separatorLine")
                .append("Brand: ${UtilsApp.getBrand()}$separatorLine")
                .append("Manufacturer: ${UtilsApp.getManufacturer()}$separatorLine")
                .append("Android SDK: ${UtilsApp.getSdkVersion()}$separatorLine")
                .append("Android Version: ${UtilsApp.getSysVersion()}$separatorLine")
                .append("AppDebug: $isDebug$separatorLine")
                .append("AppVersion: ${UtilsApp.getVersionName()}$separatorLine")
                .append("AppChannel: ${UtilsChannel.getChannel()}")
                .toString()
        )
    }
}