package love.nuoyan.android.net

import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * 描述: 网络请求构建
 * 作者: WJ
 * 时间: 2020/4/1
 * 版本: 1.0
 */
class NetBuildGet<T>(url: String, tag: String) : NetBuild<T>(url, tag) {
    private val mUrlBuilder = url.toHttpUrl().newBuilder()

    /** 添加参数 */
    fun params(key: String, value: String) = apply {
        mUrlBuilder.addQueryParameter(key, value)
    }

    override suspend fun build(): T {
        mRequestBuilder.url(mUrlBuilder.build()).tag(tag).get()
        return super.build()
    }
}