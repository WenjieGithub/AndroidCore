package love.nuoyan.android.net

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.*
import java.io.File
import java.io.RandomAccessFile
import kotlin.coroutines.resumeWithException

/**
 * 描述: 下载网络请求构建，返回结果：code，msg；code = 0 代表结束下载任务，-1 代表异常，1 代表正在下载，2 代表下载完成，3 代表暂停状态，4 代表恢复下载状态
 */
class NetBuildDownload(url: String, tag: String) : NetBuild<Unit>(url, tag) {
    private val mUrlBuilder = url.toHttpUrl().newBuilder()
    private var mListener: ((code: Int, msg: String, total: Long, update: Long, file: File?)-> Unit)? = null
    private var mFile: File? = null
    private var mPause = false
    private var mNetBuildCallback: ((netBuild: NetBuildDownload) -> Unit)? = null       // netBuild 回调

    /** 添加下载监听器 */
    fun addDownloadProgress(listener: (code: Int, msg: String, total: Long, update: Long, file: File?) -> Unit) = apply { mListener = listener }
    /** 保存文件的位置 */
    fun setSaveFile(file: File) = apply { mFile = file }
    /** 添加参数 */
    fun params(key: String, value: String?) = apply {
        if (value != null) {
            mUrlBuilder.addQueryParameter(key, value)
        }
    }
    /** 添加参数 */
    fun paramsMap(params: Map<String, String>) = apply {
        for (e in params) {
            params(e.key, e.value)
        }
    }
    /** 获取 NetBuildDownload，可用于下载暂停等 */
    fun onNetBuildDownload(callback: (netBuild: NetBuildDownload) -> Unit) = apply { mNetBuildCallback = callback }

    override suspend fun build() = withContext(Dispatchers.Default) {
        mRequestBuilder.url(mUrlBuilder.build()).tag(tag).get()
        try {
            if (mFile == null || mFile!!.isDirectory) {
                mListener?.let { it(-1, "参数错误，保存文件为空或它是一个目录", 0, 0, mFile) }
            } else {
                if (!UtilsNet.isConnected() && NetService.networkUnavailableForceCache) {   // 网络不可用开启缓存
                    mRequestBuilder.cacheControl(CacheControl.FORCE_CACHE)                  // 设置使用缓存
                } else {
                    mCacheControl?.let { mRequestBuilder.cacheControl(it) }                 // 使用缓存控制
                }
                if (mFile!!.exists() && mFile!!.length() >= 0) {
                    setHeader("Range", "bytes=${mFile!!.length()}-")
                }
                val call = NetService.okClient.newCall(mRequestBuilder.build())
                mCallback?.invoke(call)
                mNetBuildCallback?.let { it(this@NetBuildDownload) }
                call.awaitDownload()
                mListener?.let { it(0, "下载任务结束", 0, 0, mFile) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun pause() {
        mPause = true
        mListener?.let { it(3, "下载暂停", 0, 0, mFile) }
    }
    suspend fun resume(): Result<Unit> {
        mPause = false
        mListener?.let { it(4, "下载恢复", 0, 0, mFile) }
        return build()
    }

    private suspend fun Call.awaitDownload() = suspendCancellableCoroutine {
        val start = System.currentTimeMillis()
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetService.logCallback?.invoke("Net ## $e")
                if (it.isCancelled) return
                it.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                parseResponse(true, isParseResponseBody = false, response = response, duration = System.currentTimeMillis() - start)
                if (it.isCancelled) return
                it.resumeWith(runCatching {
                    if (response.isSuccessful) {
                        onResponse(response, it)
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

    @Throws(Exception::class)
    private fun onResponse(response: Response, cc: CancellableContinuation<Unit>) {
        val body = response.body ?: throw NullPointerException("The response body is empty")
        val inputStream = body.byteStream()
        val accessFile = RandomAccessFile(mFile, "rw")
        try {
            val parentFile = mFile!!.parentFile
            if (parentFile != null) {
                createFolder(parentFile)
            }

            var contentLength = body.contentLength()
            if (contentLength < 0) {
                contentLength = 0
                mListener?.let { it(-1, "下载内容为空", contentLength, 0, mFile) }
                return
            }
            var mDownloadByte = 0L
            // 移动文件指针到断点续传的位置
            if (mFile!!.length() >= 0) {
                if (mFile!!.length() >= contentLength) {
                    mListener?.let { it(1, "下载内容已经下载", contentLength, mFile!!.length(), mFile) }
                    return
                } else {
                    mDownloadByte = mFile!!.length()
                    accessFile.seek(mDownloadByte)
                }
            }

            var readLength: Int
            val bytes = ByteArray(8192)
            while (inputStream.read(bytes).also { readLength = it } != -1) {
                if (cc.isCancelled || mPause) return
                mDownloadByte += readLength.toLong()
                accessFile.write(bytes, 0, readLength)
                mListener?.let { it(1, "正在下载", contentLength, mDownloadByte, mFile) }
            }
            mListener?.let { it(2, "下载完成", contentLength, mDownloadByte, mFile) }
        } catch (e: Exception) {
            throw e
        } finally {
            body.close()
            inputStream.close()
            accessFile.close()
        }
    }

    /** 创建文件夹*/
    private fun createFolder(targetFolder: File): Boolean {
        if (targetFolder.exists()) {
            if (targetFolder.isDirectory) {
                return true
            }
            targetFolder.delete()
        }
        return targetFolder.mkdirs()
    }
}