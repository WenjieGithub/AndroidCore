package love.nuoyan.android.net

import com.squareup.moshi.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object UtilsJson {
    var moshi: Moshi = Moshi.Builder().add(JsonAdapterUnit()).build()

    fun newParameterizedType(raw: Type, vararg args: Type): ParameterizedType {
        return Types.newParameterizedType(raw, *args)
    }

    fun <T> getAdapter(type: Type): JsonAdapter<T> {
        return moshi.adapter(type)
    }

    fun <T> getAdapter(type: Class<T>): JsonAdapter<T> {
        return moshi.adapter(type)
    }

    fun <T> formJson(type: Type, json: String): T? {
        return moshi.adapter<T>(type).fromJson(json)
    }

    inline fun <reified T> formJson(json: String): T? {
        (object : TypeReference<T>() {}.type)?.let {
            return moshi.adapter<T>(it).fromJson(json)
        }
        return null
    }

    inline fun <reified T> toJson(t: T): String? {
        (object : TypeReference<T>() {}.type)?.let {
            return moshi.adapter<T>(it).toJson(t)
        }
        return null
    }
}

abstract class TypeReference<T> : Comparable<TypeReference<T>> {
    val type: Type? = (javaClass.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.get(0)
    override fun compareTo(other: TypeReference<T>) = 0
}

class JsonAdapterUnit : JsonAdapter<Unit>() {
    @FromJson
    override fun fromJson(reader: JsonReader): Unit? {
        return null
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Unit?) {
    }
}