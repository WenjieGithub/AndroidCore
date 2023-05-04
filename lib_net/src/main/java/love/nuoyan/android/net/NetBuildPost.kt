package love.nuoyan.android.net

import okhttp3.FormBody

/**
 * 描述: 网络请求构建
 */
class NetBuildPost<T>(url: String, tag: String) : NetBuild<T>(url, tag) {
    private val mFormBody = FormBody.Builder()

    /** 添加参数 */
    fun params(key: String, value: String) = apply {
        mFormBody.add(key, value)
    }

    override suspend fun build(): Result<T> {
        mRequestBuilder.url(url).tag(tag).post(mFormBody.build())
        return super.build()
    }
}