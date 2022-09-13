package love.nuoyan.android.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import love.nuoyan.android.utils.kv.AbsKV
import love.nuoyan.android.utils.kv.HelperForMMKV
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dataStore")

/**
 * 安全存储(DataStore实现)，避免某些机型 MMKV 丢失数据问题
 */
object UtilsData {
    suspend fun get(key: String, def: Boolean? = null): Boolean? {
        return Utils.appContext.dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey(key)] ?: def
        }.firstOrNull()
    }
    suspend fun put(key: String, value: Boolean) {
        Utils.appContext.dataStore.edit { dataStore ->
            dataStore[booleanPreferencesKey(key)] = value
        }
    }
    suspend fun removeBoolean(key: String) {
        Utils.appContext.dataStore.edit { dataStore ->
            dataStore.remove(booleanPreferencesKey(key))
        }
    }
    suspend fun get(key: String, def: String? = null): String? {
        return Utils.appContext.dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(key)] ?: def
        }.firstOrNull()
    }
    suspend fun put(key: String, value: String) {
        Utils.appContext.dataStore.edit { dataStore ->
            dataStore[stringPreferencesKey(key)] = value
        }
    }
    suspend fun removeString(key: String) {
        Utils.appContext.dataStore.edit { dataStore ->
            dataStore.remove(stringPreferencesKey(key))
        }
    }
    suspend fun get(key: String, def: Set<String>? = null): Set<String>? {
        return Utils.appContext.dataStore.data.map { preferences ->
            preferences[stringSetPreferencesKey(key)] ?: def
        }.firstOrNull()
    }
    suspend fun put(key: String, value: Set<String>) {
        Utils.appContext.dataStore.edit { dataStore ->
            dataStore[stringSetPreferencesKey(key)] = value
        }
    }
    suspend fun removeStringSet(key: String) {
        Utils.appContext.dataStore.edit { dataStore ->
            dataStore.remove(stringSetPreferencesKey(key))
        }
    }
}

/**
 * 存储 Key-Value 工具类
 */
object UtilsKV: AbsKV() {
    internal fun init(context: Context, key: String? = null) {
        HelperForMMKV().initMMKV(context, key)
    }

    /** 重新设置存储密钥 */
    fun reCryptKey(key: String) {
        mKV?.reKey(key)
        UtilsCache.mKV?.reKey(key)
    }
}

/**
 * 用于存储缓存的 KV，可以随时清除
 */
object UtilsCache: AbsKV()
