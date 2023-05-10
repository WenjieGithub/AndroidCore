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
        try {
            if (!UtilsNet.isConnected() && NetService.networkUnavailableForceCache) {   // 网络不可用开启缓存
                mRequestBuilder.cacheControl(CacheControl.FORCE_CACHE)                  // 设置使用缓存
            } else {
                mCacheControl?.let { mRequestBuilder.cacheControl(it) }                 // 使用缓存控制
            }
            val call = NetService.okClient.newCall(mRequestBuilder.build())
            mCallback?.invoke(call)
            Result.success(call.await())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun Call.await() = suspendCancellableCoroutine<T> {
        val start = System.currentTimeMillis()
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (it.isCancelled) return
                it.resumeWithException(e)
            }
            override fun onResponse(call: Call, response: Response) {
                if (it.isCancelled) return
                try {
                    parseResponse(isParseLogRequestBody, isParseLogResponseBody, response, System.currentTimeMillis() - start)
                } catch (e: Exception) {
                    NetService.logCallback?.invoke("ParseError:$separatorLine${e.stackTraceToString()}")
                }
                it.resumeWith(runCatching {
                    if (response.isSuccessful) {
                        convert<T>(type, response.body) ?: throw NullPointerException("Response body or type is null: $response")
                    } else {
                        throw RuntimeException("Http ${response.code} : $response")
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
                UtilsJson.formJson(type, body.string())
            } else {
                null
            }
        }
        internal fun parseResponse(isParseRequestBody: Boolean, isParseResponseBody: Boolean, response: Response, duration: Long) {
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
            NetService.logCallback?.invoke(
                """
                |isSuccess: ${response.isSuccessful}
                |Tag: ${request.tag()} 
                |Duration: $duration
                |${response.protocol} ${request.method} ${request.url}
                |${response.code} ${response.message}
                |RequestHeaders: $reqHeaders
                |   Content-Type : ${requestBody?.contentType()}
                |ResponseHeaders: $resHeaders
                |RequestBody:
                |   ${bodyToString(isParseRequestBody, request)}
                |ResponseBody:
                |   ${bodyToString(isParseResponseBody, responseBody)}
                """.trimMargin()
            )
        }
        private fun bodyToString(isParseRequestBody: Boolean, request: Request): String {
            val buffer = Buffer()
            return try {
                if (isParseRequestBody) {
                    request.newBuilder().build().body?.let {
                        val contentType = it.contentType()
                        val charset = contentType?.charset(StandardCharsets.UTF_8)
                        it.writeTo(buffer)
                        buffer.readString(charset ?: StandardCharsets.UTF_8)
                    } ?: ""
                } else {
                    "不解析 RequestBody"
                }
            } catch (e: Exception) {
                "parse request error: ${e.stackTraceToString()}"
            } finally {
                buffer.close()
            }
        }
        private fun bodyToString(isParseResponseBody: Boolean, responseBody: ResponseBody?): String {
            return try {
                if (isParseResponseBody && responseBody != null) {
                    responseBody.string()
                } else {
                    "不解析 ResponseBody"
                }
            } catch (e: Exception) {
                "parse response error: ${e.stackTraceToString()}"
            }
        }
    }
}
