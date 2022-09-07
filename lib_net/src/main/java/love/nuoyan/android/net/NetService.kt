package love.nuoyan.android.net

import android.content.Context
import android.os.Environment
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/** 网络服务 */
object NetService {
    var networkUnavailableForceCache = true                                     // 网络不可用, 强制使用缓存
    var logCallback : ((msg : String) -> Unit)? = null                          // 日志回调
    val publicHeaders = hashMapOf<String, String>()                             // 公共头文件
    val publicParams = hashMapOf<String, String>()                              // 公共参数文件
    lateinit var mCache: Cache
    lateinit var okClient: OkHttpClient

    /**
     * 上下文对象
     * 是否是 Debug 模式，默认 false
     * 缓存大小，单位 M，默认 200
     * 是否在网络不可用时，强制使用缓存，默认 true
     * 日志回调类，默认为空
     */
    fun init(context: Context, debug: Boolean = false, cacheSize: Int = 200, networkUnavailableForceCache: Boolean = true, logCallback : ((msg : String) -> Unit)? = null) {
        NetService.networkUnavailableForceCache = networkUnavailableForceCache
        NetService.logCallback = logCallback

        (if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.externalCacheDir
        } else {
            context.cacheDir
        })?.let {
            mCache = Cache(File(it, "net"), (1024 * 1024 * cacheSize).toLong()) // 缓存文件目录，最大200 Mb 缓存大小
        }
        initOkHttpClient(debug)
        UtilsNet.initNet(context)
    }
    private fun initOkHttpClient(debug: Boolean) {
        val builder = OkHttpClient.Builder()
                .retryOnConnectionFailure(true)                                 // 设置出现错误进行重新连接
                .callTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)                           // 设置超时时间，默认超时 15秒
                .cache(mCache)
        if (debug) {                                                            // 如果 debug，可以使用不安全证书
            builder.connectionSpecs(listOf(ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))
        }
        okClient = builder.build()
    }


    inline fun <reified T> get(url: String, tag: String = "get"): NetBuildGet<T> {
        return NetBuildGet<T>(url, tag).apply {
            type = object : TypeReference<T>() {}.type
            addHeader(publicHeaders)
            for ((k, v) in publicParams) {
                params(k, v)
            }
        }
    }

    inline fun <reified T> post(url: String, tag: String = "post"): NetBuildPost<T> {
        return NetBuildPost<T>(url, tag).apply {
            type = object : TypeReference<T>() {}.type
            addHeader(publicHeaders)
            for ((k, v) in publicParams) {
                params(k, v)
            }
        }
    }

    inline fun <reified T> postJson(url: String, tag: String = "postJson"): NetBuildPostJson<T> {
        return NetBuildPostJson<T>(url, tag).apply {
            type = object : TypeReference<T>() {}.type
            addHeader(publicHeaders)
            for ((k, v) in publicParams) {
                params(k, v)
            }
        }
    }

    inline fun <reified T> postJsonString(url: String, tag: String = "postJsonString"): NetBuildPostJsonString<T> {
        return NetBuildPostJsonString<T>(url, tag).apply {
            type = object : TypeReference<T>() {}.type
            addHeader(publicHeaders)        // 不添加公参
        }
    }

    inline fun <reified T> upload(url: String, tag: String = "upload"): NetBuildUpload<T> {
        return NetBuildUpload<T>(url, tag).apply {
            type = object : TypeReference<T>() {}.type
            addHeader(publicHeaders)
            for ((k, v) in publicParams) {
                params(k, v)
            }
        }
    }

    fun download(url: String, tag: String = "download"): NetBuildDownload {
        return NetBuildDownload(url, tag).apply {
            addHeader(publicHeaders)
            for ((k, v) in publicParams) {
                params(k, v)
            }
        }
    }
}
