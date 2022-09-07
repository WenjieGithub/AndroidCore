package love.nuoyan.android.net

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * 描述: 网络请求构建
 * 作者: WJ
 * 时间: 2020/4/1
 * 版本: 1.0
 */
class NetBuildPostJson<T>(url: String, tag: String) : NetBuild<T>(url, tag) {
    private val jsonObject = JSONObject()

    /** 添加参数 json */
    fun paramsJson(json: String) = apply {
        val jo = JSONObject(json)
        for (k in jo.keys()) {
            jsonObject.put(k, jo[k])
        }
    }

    /** 添加参数 JSONObject */
    fun paramsJson(jo: JSONObject) = apply {
        for (k in jo.keys()) {
            jsonObject.put(k, jo[k])
        }
    }

    /** 添加参数 */
    fun params(key: String, value: String) = apply {
        jsonObject.put(key, value)
    }

    override suspend fun build(): T {
        mRequestBuilder.url(url).tag(tag).post(jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
        return super.build()
    }
}