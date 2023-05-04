package love.nuoyan.android.net

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * 描述: 网络请求构建，不添加公参
 * 作者: WJ
 * 时间: 2020/4/1
 * 版本: 1.0
 */
class NetBuildPostJsonString<T>(url: String, tag: String) : NetBuild<T>(url, tag) {
    private var json = ""

    /** 添加参数 json */
    fun paramsJson(json: String) = apply {
        this.json = json
    }

    override suspend fun build(): Result<T> {
        mRequestBuilder.url(url).tag(tag).post(json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
        return super.build()
    }
}