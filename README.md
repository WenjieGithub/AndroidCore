
# Floating Kit

## 需要引入的库
``` gradle
    appTestImplementation 'love.nuoyan.android:floating_kit:0.0.1'
    
    implementation 'androidx.appcompat:appcompat:1.5.1'
    implementation "com.google.android.material:material:1.6.1"
    implementation "androidx.recyclerview:recyclerview:1.2.1"
    implementation "androidx.viewpager2:viewpager2:1.0.0"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.5.1"
```

## 权限

库内已添加网络权限及获取网络状态权限
```gradle
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## FloatingKit 初始化

``` kotlin
    FloatingMagnetView.init(context)
```

## 配置环境切换

调用 FloatingMagnetView.initEnv() 初始化环境配置
可选，不调用时切换环境失效

``` kotlin
    FloatingMagnetView.initEnv(object : EnvironmentCallback {
        val array = arrayOf("生产环境", "准生产环境", "测试环境")
        override fun getEnvironmentArray(): Array<String> {
            return array
        }

        override fun getEnvironment(): String {
            // return 当前存储的环境
        }

        override fun onSelectEnvironment(env: String) {
            // 保存环境
        }
    }
```

EnvironmentCallback 接口
``` kotlin
/** 用于选择环境的回调类 */
interface EnvironmentCallback {
    /** 获取环境描述数组 */
    fun getEnvironmentArray(): Array<String>
    /** 获取当前环境 */
    fun getEnvironment(): String
    /** 环境选择后的回调, 表示选中的环境 */
    fun onSelectEnvironment(env: String)
}
```

## 配置 Log

可选
调用 FloatingMagnetView.initLog() 初始化日志标签，分组列表包括 tag 以及 tag 显示的颜色
不调用时使用库内提供的日志标签
``` kotlin
    mTagList = arrayListOf(Pair("Error", ContextCompat.getColor(context, R.color.log_error)))
    FloatingMagnetView.initLog(tagList)
```

调用 FloatingMagnetView.addLog 注入日志，最多显示 500 条
``` kotlin
    UtilsLog.logCallback = { info ->
        // tag      标记
        // msg      日志消息
        // time     调用时间(可选)
        // thread   线程(可选)
        // method   调用方法信息(可选)
        FloatingMagnetView.addLog(info.tag, info.msg, info.time, info.thread, info.method)
    }
```

## 添加扩展按钮

```
    FloatingMagnetView.addExtend(name, drawableId) { activity, view ->
        
    }
    // 可以省略图标, 使用默认图标
    FloatingMagnetView.addExtend("扩展") { activity, view ->
        Toast.makeText(this, "点击了扩展", Toast.LENGTH_LONG).show() 
    }
```



# 网络库

## 需要引入的依赖

``` gradle
    implementation "love.nuoyan.android:net:0.0.1"

//    kapt "com.squareup.moshi:moshi-kotlin-codegen:1.13.0"
//    ksp "com.squareup.moshi:moshi-kotlin-codegen:1.13.0"
```

## 初始化
``` kotlin
    /**
     * 上下文对象
     * 是否是 Debug 模式，可以使用不安全证书，默认 false
     * 缓存大小，单位 M，默认 200
     * 是否在网络不可用时，强制使用缓存，默认 true
     * 日志回调类，默认为空
     */
    NetService.init(context: Context, debug: Boolean = false, cacheSize: Int = 200, networkUnavailableForceCache: Boolean = true, logCallback : ((msg : String) -> Unit)? = null)

    // 需要单独配置 OkHttpClient（可选）
    NetService.okClient = NetService.okClient.newBuilder().***.build()
```

## 使用

``` kotlin
    GlobalScope.launch {
        val result = NetService.get<String>("url").apply {
            params("key", "value")
        }.build()
    }

    // 网络工具类，UtilsNet
    // Json 解析工具，UtilsJson
```

# permission

## 引入
``` groovy
    implementation 'love.nuoyan.android:permission:0.0.1'
```

## 使用

``` kotlin
    // 配置日志
    PermissionRequest.logCallback = {
        Log.d("permission", it)
    }
    // 开始请求权限
    PermissionRequest(this) {
        Log.d("permissions", it.toString())
    }.applyPermission("permissions 1", "permissions 2")
    // 检查是否有权限
    PermissionRequest(this) {
        Log.d("permissions", it.toString())
    }.hasPermission("permissions")
```

# qr

## 引入
``` groovy
    implementation 'love.nuoyan.android:qr:0.0.1'
```


# Utils

## 引入
``` groovy
    implementation 'com.caixin.android:utils:0.0.1'
```

## 初始化
```
    Utils.init(app: Application, debug: Boolean, key: String, defaultChannel: String, lifecycleCallback: LifecycleCallback?)
```

## 存储库
UtilsData : 安全存储(DataStore实现)，避免某些机型 MMKV 丢失数据问题
UtilsKV : 存储 Key-Value 工具类
UtilsCache : 用于存储缓存的 KV，可以随时清除

## 库单独添的加依赖
1. 引入了依赖：[MMKV 1.2.14](https://github.com/Tencent/MMKV)
2. 引入了依赖：[Walle 1.1.7](https://github.com/Meituan-Dianping/walle)

