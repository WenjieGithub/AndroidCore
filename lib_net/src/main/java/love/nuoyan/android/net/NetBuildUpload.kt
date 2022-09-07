package love.nuoyan.android.net

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.*
import java.io.File

/**
 * 描述: 网络请求构建
 * 作者: WJ
 * 时间: 2020/4/1
 * 版本: 1.0
 */
class NetBuildUpload<T>(url: String, tag: String) : NetBuild<T>(url, tag) {
    private val mRequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
    private var mListener: ((total: Long, update: Long, progress: Float)-> Unit)? = null

    /** 添加参数 */
    fun params(key: String, value: String) = apply {
        mRequestBody.addFormDataPart(key, value)
    }

    /**
     * 添加文件
     * http://www.iana.org/assignments/media-types/media-types.xhtml
     * media-types :
     * "image/ *"
     * "application/json; charset=utf-8"
     */
    fun addFile(key: String, file: File, contentType: String) = apply {
        mRequestBody.addFormDataPart(key, file.name, file.asRequestBody(contentType.toMediaTypeOrNull()))
    }

    fun addUpdateProgress(listener: (total: Long, update: Long, progress: Float) -> Unit) = apply {
        mListener = listener
    }

    override suspend fun build(): T {
        setParseLog(false, isParseResponseBody = true)
        if (mListener == null) {
            mRequestBuilder.url(url).tag(tag).post(mRequestBody.build())
        } else {
            mRequestBuilder.url(url).tag(tag).post(UploadRequestBody(mRequestBody.build(), mListener!!))
        }
        return super.build()
    }
}

class UploadRequestBody(
    private val mRequestBody: RequestBody,
    private var mListener: (total: Long, update: Long, progress: Float) -> Unit
) : RequestBody() {
    override fun contentType() = mRequestBody.contentType()

    override fun contentLength() = mRequestBody.contentLength()

    override fun isOneShot() = true

    override fun writeTo(sink: BufferedSink) {
        val totalBytes = contentLength()
        val progressSink = object : ForwardingSink(sink) {
            private var bytesWritten = 0L
            override fun write(source: Buffer, byteCount: Long) {
                bytesWritten += byteCount
                mListener(totalBytes, bytesWritten, (bytesWritten.toFloat() / totalBytes))
                super.write(source, byteCount)
            }
        }.buffer()
        mRequestBody.writeTo(progressSink)
        progressSink.flush()
    }
}