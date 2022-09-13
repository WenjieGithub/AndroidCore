package love.nuoyan.android.utils

import android.provider.Settings
import love.nuoyan.android.utils.id.HelperForWebId
import java.util.*

/**
 * 获取浏览器指纹, AndroidId, UUID
 */
object UtilsId {
    private const val KeyOAID = "Key_OAID"
    private const val KeyWebId = "Key_WebId"
    private const val KeyAndroidId = "Key_AndroidId"
    private const val KeyStandbyId = "Key_StandbyId"

    var WebId: String? = UtilsKV.getString(KeyWebId)
        internal set(value) {
            field = value
            value?.let {
                UtilsKV.put(KeyWebId, it)
                UtilsLog.log("UtilsId ## WebId: $it")
            }
        }
    var AndroidId: String? = UtilsKV.getString(KeyAndroidId)
        internal set(value) {
            field = value
            value?.let {
                UtilsKV.put(KeyAndroidId, it)
                UtilsLog.log("UtilsId ## AndroidId: $it")
            }
        }
    var StandbyId: String? = UtilsKV.getString(KeyStandbyId)           // 备用 ID, 为空则随机生成 UUID
        internal set(value) {
            field = value
            value?.let {
                UtilsKV.put(KeyStandbyId, it)
                UtilsLog.log("UtilsId ## StandbyId: $it")
            }
        }

    var OAID: String? = UtilsKV.getString(KeyOAID)
        set(value) {
            field = value
            value?.let {
                UtilsKV.put(KeyOAID, it)
                UtilsLog.log("UtilsId ## KeyOAID: $it")
            }
        }

    fun init() {
        if (WebId.isNullOrEmpty()) {
            HelperForWebId().init(Utils.appContext)
        }
        if (AndroidId.isNullOrEmpty()) {
            AndroidId = Settings.System.getString(Utils.appContext.contentResolver, Settings.Secure.ANDROID_ID)
        }
        if (StandbyId.isNullOrEmpty()) {
            StandbyId = UUID.randomUUID().toString()
        }
    }
}