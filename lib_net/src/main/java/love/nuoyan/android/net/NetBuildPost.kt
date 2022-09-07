package love.nuoyan.android.net

import okhttp3.FormBody

/**
 * 描述: 网络请求构建
 * 作者: WJ
 * 时间: 2020/4/1
 * 版本: 1.0
 */
class NetBuildPost<T>(url: String, tag: String) : NetBuild<T>(url, tag) {
    private val mFormBody = FormBody.Builder()

    /** 添加参数 */
    fun params(key: String, value: String) = apply {
        mFormBody.add(key, value)
    }

    override suspend fun build(): T {
        mRequestBuilder.url(url).tag(tag).post(mFormBody.build())
        return super.build()
    }
}