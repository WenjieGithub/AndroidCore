package love.nuoyan.android.net

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * 描述: 网络请求构建
 */
class NetBuildPostJson<T>(url: String, tag: String) : NetBuild<T>(url, tag) {
    private val jsonObject = JSONObject()

    /** 添加参数 json */
    fun paramsJson(json: String) = apply {
        if (json.isNotEmpty()) {
            val jo = JSONObject(json)
            for (k in jo.keys()) {
                jsonObject.put(k, jo[k])
            }
        }
    }

    /** 添加参数 JSONObject */
    fun paramsJson(jo: JSONObject) = apply {
        for (k in jo.keys()) {
            jsonObject.put(k, jo[k])
        }
    }

    /** 添加参数 */
    fun params(key: String, value: Any?) = apply {
        if (value != null) {
            jsonObject.put(key, value)
        }
    }

    /** 添加参数 */
    fun paramsMap(params: Map<String, Any>) = apply {
        for (e in params) {
            params(e.key, e.value)
        }
    }

    override suspend fun build(): Result<T> {
        mRequestBuilder.url(url).tag(tag).post(jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
        return super.build()
    }
}