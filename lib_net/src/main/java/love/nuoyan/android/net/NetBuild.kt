package love.nuoyan.android.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.Buffer
import okio.IOException
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import kotlin.coroutines.resumeWithException

/**
 * 网络请求构建
 */
abstract class NetBuild<T> internal constructor(protected val url: String, protected var tag: String) {
    var type: Type? = null                                              // 泛型类型
    var call: Call? = null                                              // okHttp Call 可用于取消
    protected var mCacheControl: CacheControl? = null                   // 请求缓存控制
    protected val mRequestBuilder = Request.Builder()                   // 请求构建器
    protected var mCallback: ((call: Call) -> Unit)? = null             // Call 回调

    protected var isParseLogRequestBody: Boolean = true
    protected var isParseLogResponseBody: Boolean = true

    fun setParseLog(isParseRequestBody: Boolean, isParseResponseBody: Boolean) = apply {
        isParseLogRequestBody = isParseRequestBody
        isParseLogResponseBody = isParseResponseBody
    }

    /** 添加请求头, 去重 */
    fun setHeader(key: String, value: String) = apply { mRequestBuilder.header(key, value) }
    /** 添加请求头, 可重复 key */
    fun addHeader(key: String, value: String) = apply { mRequestBuilder.addHeader(key, value) }
    /** 添加请求头, 可重复 key */
    fun addHeader(map: Map<String, String>) = apply {
        for ((k, v) in map) {
            addHeader(k, v)
        }
    }
    /** 设置缓存控制器 */
    fun cacheControl(cache: CacheControl) = apply { mCacheControl = cache }
    /** 获取 call，可用于取消等 */
    fun onCall(callback: (call: Call) -> Unit) = apply { mCallback = callback }

    open suspend fun build() = withContext(Dispatchers.Default) {
        if (!UtilsNet.isConnected() && NetService.networkUnavailableForceCache) {   // 网络不可用开启缓存
            mRequestBuilder.cacheControl(CacheControl.FORCE_CACHE)                  // 设置使用缓存
        } else {
            mCacheControl?.let { mRequestBuilder.cacheControl(it) }                 // 使用缓存控制
        }
        call = NetService.okClient.newCall(mRequestBuilder.build())
        mCallback?.let { it(call!!) }
        call!!.await()
    }

    private suspend fun Call.await() = suspendCancellableCoroutine<T> {
        val start = System.currentTimeMillis()
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetService.logCallback?.let { log -> log("Net ## $e") }
                if (it.isCancelled) return
                it.resumeWithException(e)
            }
            override fun onResponse(call: Call, response: Response) {
                if (it.isCancelled) return
                parseResponse(isParseLogRequestBody, isParseLogResponseBody, response, System.currentTimeMillis() - start).let { msg ->
                    NetService.logCallback?.let { log -> log("Net ## $msg") }
                }
                it.resumeWith(runCatching {
                    if (response.isSuccessful) {
                        convert<T>(type, response.body) ?: throw NullPointerException("Response body or type is null: $response")
                    } else {
                        throw RuntimeException("Http ${response.code} ${response.message}: $response")
                    }
                })
            }
        })

        it.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {
                // Ignore cancel exception
            }
        }
    }

    companion object {
        private val separatorLine = System.getProperty("line.separator") ?: "\n"
        private fun<D> convert(type: Type?, body: ResponseBody?): D? {
            return if (body != null && type != null) {
                if (type == object : TypeReference<String>() {}.type) {
                    body.string() as D
                } else {
                    UtilsJson.formJson(type, body.string())
                }
            } else {
                null
            }
        }

        internal fun parseResponse(isParseRequestBody: Boolean, isParseResponseBody: Boolean, response: Response, duration: Long): String {
            val request = response.request
            val requestHeaders = request.headers
            val requestBody = if (isParseRequestBody) request.body else null
            val responseHeaders = response.headers
            val responseBody = if (isParseResponseBody) response.peekBody(Long.MAX_VALUE) else null

            var reqHeaders = ""
            for ((k, v) in requestHeaders) {
                reqHeaders += "$separatorLine   $k : $v "
            }
            var resHeaders = ""
            for ((k, v) in responseHeaders) {
                resHeaders += "$separatorLine   $k : $v "
            }
            return """$separatorLine
                |Tag: ${request.tag()} 
                |Duration: $duration
                |${response.protocol} ${request.method} ${request.url}
                |${response.code} ${response.message}
                |RequestHeaders: $reqHeaders
                |   Content-Type : ${requestBody?.contentType()}
                |RequestBody: $separatorLine   ${bodyToString(isParseRequestBody, request)}
                |ResponseHeaders: $resHeaders
                |ResponseBody: $separatorLine   ${bodyToString(isParseResponseBody, responseBody)}
                """.trimMargin()
        }

        private fun bodyToString(isParseRequestBody: Boolean, request: Request): String? {
            return if (isParseRequestBody) {
                val buffer = Buffer()
                try {
                    val body = request.newBuilder().build().body
                    body?.let {
                        if (body.contentLength() > 3000) {
                            "内容过长 length=${body.contentLength()}"
                        } else {
                            val contentType = body.contentType()
                            val charset = contentType?.charset(StandardCharsets.UTF_8)
                            it.writeTo(buffer)
                            val bodyString = buffer.readString(charset ?: StandardCharsets.UTF_8)
                            bodyString
                        }
                    }
                } catch (e: java.lang.Exception) {
                    e.toString()
                } finally {
                    buffer.close()
                }
            } else {
                "不解析 RequestBody"
            }
        }
        private fun bodyToString(isParseResponseBody: Boolean, responseBody: ResponseBody?): String? {
            return if (isParseResponseBody) {
                responseBody?.let {
                    if (it.contentLength() > 3000) {
                        "内容过长 length=${it.contentLength()}"
                    } else {
                        it.string()
                    }
                }
            } else {
                "不解析 ResponseBody"
            }
        }
    }
}
