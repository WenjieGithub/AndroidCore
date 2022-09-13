package love.nuoyan.android.utils.kv

import android.content.Context
import android.os.Environment
import love.nuoyan.android.utils.UtilsCache
import love.nuoyan.android.utils.UtilsKV
import love.nuoyan.android.utils.UtilsLog
import com.tencent.mmkv.MMKV
import com.tencent.mmkv.MMKVHandler
import com.tencent.mmkv.MMKVLogLevel
import com.tencent.mmkv.MMKVRecoverStrategic
import java.io.File

internal class HelperForMMKV : MMKVHandler {
    fun initMMKV(context: Context, key: String? = null) {
        try {
            (if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                context.getExternalFilesDir(null)
            } else {
                context.filesDir
            })?.let { file ->
                MMKV.initialize(context, "${file.absolutePath}${File.separator}kvStore")
                MMKV.registerHandler(this)
                MMKV.registerContentChangeNotify { UtilsLog.log("UtilsKV ## ContentChangeNotify: $it") }
            }
            UtilsKV.mKV = MMKV.mmkvWithID("KVStore", MMKV.MULTI_PROCESS_MODE, key)
            UtilsCache.mKV = MMKV.mmkvWithID("KVCache", MMKV.MULTI_PROCESS_MODE, key)
        } catch (e: Exception) {
            UtilsLog.log("UtilsKV ## 初始化异常: ${e.stackTraceToString()}")
        }
    }

    // 日志转发
    override fun wantLogRedirecting(): Boolean {
        return true
    }
    // 接收日志
    override fun mmkvLog(level: MMKVLogLevel?, file: String?, line: Int, func: String?, message: String?) {
        UtilsLog.log("UtilsKV ## <${level?.toString()} -- $file : $line :: $func> $message")
    }
    // rcr 校验失败
    override fun onMMKVCRCCheckFail(mmapID: String?): MMKVRecoverStrategic {
        UtilsLog.log("UtilsKV ## onMMKVCRCCheckFail: $mmapID}")
        return MMKVRecoverStrategic.OnErrorRecover
    }
    // 文件长度错误
    override fun onMMKVFileLengthError(mmapID: String?): MMKVRecoverStrategic {
        UtilsLog.log("UtilsKV ## onMMKVFileLengthError: $mmapID")
        return MMKVRecoverStrategic.OnErrorRecover
    }
}