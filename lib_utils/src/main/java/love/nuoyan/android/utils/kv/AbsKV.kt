package love.nuoyan.android.utils.kv

import android.os.Parcelable
import com.tencent.mmkv.MMKV

abstract class AbsKV {
    internal var mKV: MMKV? = null

    fun put(key: String, value: Int) = mKV?.encode(key, value) ?: false
    fun put(key: String, value: Long) = mKV?.encode(key, value) ?: false
    fun put(key: String, value: Float) = mKV?.encode(key, value) ?: false
    fun put(key: String, value: Double) = mKV?.encode(key, value) ?: false
    fun put(key: String, value: String) = mKV?.encode(key, value) ?: false
    fun put(key: String, value: Boolean) = mKV?.encode(key, value) ?: false
    fun put(key: String, value: ByteArray) = mKV?.encode(key, value) ?: false
    fun put(key: String, value: Parcelable) = mKV?.encode(key, value) ?: false
    fun put(key: String, value: Set<String>) = mKV?.encode(key, value)  ?: false


    fun get(key: String, defaultValue: Int) = mKV?.decodeInt(key, defaultValue) ?: defaultValue
    fun get(key: String, defaultValue: Long) = mKV?.decodeLong(key, defaultValue) ?: defaultValue
    fun get(key: String, defaultValue: Float) = mKV?.decodeFloat(key, defaultValue) ?: defaultValue
    fun get(key: String, defaultValue: Double) = mKV?.decodeDouble(key, defaultValue) ?: defaultValue
    fun get(key: String, defaultValue: Boolean) = mKV?.decodeBool(key, defaultValue) ?: defaultValue
    fun get(key: String, defaultValue: String) = mKV?.decodeString(key, defaultValue) ?: defaultValue
    fun getString(key: String) = mKV?.decodeString(key)

    fun <T : Parcelable> get(key: String, clazz: Class<T>, defaultValue: T? = null): T? {
        return mKV?.decodeParcelable(key, clazz, defaultValue) ?: defaultValue
    }
    fun getSet(key: String, defaultValue: Set<String>? = null, clazz: Class<out MutableSet<*>> = HashSet::class.java): Set<String>? {
        return mKV?.decodeStringSet(key, defaultValue, clazz) ?: defaultValue
    }


    fun remove(key: String) { mKV?.removeValueForKey(key) }
    fun remove(keys: Array<String>) { mKV?.removeValuesForKeys(keys) }

    fun allKeys() = mKV?.allKeys()
    fun containsKey(key: String) = mKV?.containsKey(key) ?: false

    fun clear() {
        mKV?.clearMemoryCache()
        mKV?.clearAll()
    }
}