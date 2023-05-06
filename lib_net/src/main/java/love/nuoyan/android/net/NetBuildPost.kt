package love.nuoyan.android.net

import okhttp3.FormBody

/**
 * 描述: 网络请求构建
 */
class NetBuildPost<T>(url: String, tag: String) : NetBuild<T>(url, tag) {
    private val mFormBody = FormBody.Builder()

    /** 添加参数 */
    fun params(key: String, value: String?) = apply {
        if (value != null) {
            mFormBody.add(key, value)
        }
    }

    /** 添加参数 */
    fun paramsMap(params: Map<String, String>) = apply {
        for (e in params) {
            params(e.key, e.value)
        }
    }

    override suspend fun build(): Result<T> {
        mRequestBuilder.url(url).tag(tag).post(mFormBody.build())
        return super.build()
    }
}