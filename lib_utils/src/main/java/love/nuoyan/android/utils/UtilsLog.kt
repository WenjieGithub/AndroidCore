package love.nuoyan.android.utils

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.io.RandomAccessFile
import java.lang.Runnable
import java.nio.channels.FileChannel
import kotlin.experimental.xor
import kotlin.system.exitProcess

/**
 * 日志工具类
 * Error 日志 debug 模式下会抛出运行时异常
 * Debug 日志 不会记录在日志文件内
 */
object UtilsLog {
    private var isInit = false
    private var logList = mutableListOf<LogInfo>()

    private lateinit var logFile: File
    private lateinit var accessFileChannel: FileChannel

    private lateinit var handlerThread: HandlerThread
    private lateinit var writerHandler: Handler

    var logCallback: ((info: LogInfo) -> Unit)? = null

    fun getLogFilePath(): String? {
        return try {
            logFile.absolutePath
        } catch (e: Exception) {
            logE("UtilsLog ## $e")
            null
        }
    }

    internal fun init(context: Context, saveDay: Int = 2) {
        Utils.appScope.launch(IO) {
            try {
                (if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                    context.externalCacheDir
                } else {
                    context.cacheDir
                })?.let {
                    handlerThread = HandlerThread("Log")
                    handlerThread.start()
                    writerHandler = Handler(handlerThread.looper)

                    val dirFile = File(it, "log")
                    if (!dirFile.exists() || !dirFile.isDirectory) {
                        UtilsFile.mkdir(dirFile)
                    }
                    logFile = File(dirFile, "Log_" + UtilsTime.currentDateTime.replace(".", "").replace(":", ""))
                    if (!logFile.exists()) {
                        logFile.createNewFile()
                    }
                    accessFileChannel = RandomAccessFile(logFile, "rw").channel
                    isInit = true
                    UtilsCrash().initCrash()

                    dirFile.listFiles()?.let { files ->
                        val pastDate = UtilsTime.getPastDate(saveDay).replace(".", "")
                        for (file in files) {
                            try {
                                val date = file.name.substring(4, 12)
                                if (pastDate.toInt() - date.toInt() > 0) {
                                    file.delete()
                                }
                            } catch (e: Exception) {
                                logE("UtilsLog ## $e")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                logE("UtilsLog ## $e")
            }
        }
    }

    fun log(msg: String, tag: String = "Msg") {
        synchronized(this) {
            if (msg.isNotEmpty()) {
                val info = LogInfo(tag, if (msg.length > 2000) {
                    msg.substring(0, 2000)
                } else {
                    msg
                })
                if (Utils.isDebug) {
                    if (tag == "Error") {
                        throw RuntimeException(info.toString())
                    } else {
                        when (tag) {
                            "Warn" -> Log.w("Cx$tag", info.toString())
                            "Debug" -> Log.d("Cx$tag", info.toString())
                            else -> Log.i("Cx$tag", info.toString())
                        }
                        logCallback?.let { it(info) }
                    }
                }
                if (isInit && tag != "Debug") {
                    logList.add(info)
                    if (logList.size > 30) {
                        finish()
                    }
                }
            }
        }
    }
    fun logE(msg: String) {
        log(msg, "Error")
    }
    fun logW(msg: String) {
        log(msg, "Warn")
    }
    fun logD(msg: String) {
        log(msg, "Debug")
    }


    fun finish() {
        writerHandler.post(WriteRunnable(logList))
        logList = mutableListOf()
    }

    internal fun crash(msg: String) {
        try {
            log(msg, "Crash")
            finish()
        } catch (e: Exception) {
            Log.e("CxError", "写 Log 文件出错: ", e)
        }
    }

    private fun encrypt(bytes: ByteArray): ByteArray {
        val key = 0x95.toByte()
        bytes.forEachIndexed { index, byte ->
            bytes[index] = byte xor key
        }
        return bytes
    }

    class LogInfo internal constructor(
        val tag: String,                                    // 标记
        val msg: String                                     // 日志消息
    ) {
        var time: String = UtilsTime.currentDateTime        // 调用时间
        var thread: String = Thread.currentThread().name    // 线程
        var method: String = getMethodInfo()                // 调用方法信息

        private fun getMethodInfo(): String {
            val trace = Thread.currentThread().stackTrace
            var stackOffset = -1

            for (i in 4 until trace.size) {
                val name = trace[i].className
                if (name != UtilsLog::class.java.name && name != javaClass.name) {
                    stackOffset = i - 1
                    break
                }
            }
            var methodCount = 2                                     // 显示调用层级数量
            if (methodCount + stackOffset > trace.size) {
                methodCount = trace.size - stackOffset
            }
            val builder = StringBuilder()
            var l = ""
            for (i in methodCount downTo 1) {
                val stackIndex = i + stackOffset
                if (stackIndex >= trace.size) {
                    continue
                }
                val name = trace[stackIndex].className
                builder.append(l)
                    .append(name.substring(name.lastIndexOf(".") + 1))
                    .append(".")
                    .append(trace[stackIndex].methodName)
                    .append(" ")
                    .append(" (")
                    .append(trace[stackIndex].fileName)
                    .append(":")
                    .append(trace[stackIndex].lineNumber)
                    .append(")")
                    .append(Utils.separatorLine)
                l += "    "
            }
            if (builder.isNotEmpty()) {
                builder.delete(builder.length - 1, builder.length)
            }
            return builder.toString()
        }

        override fun toString(): String {
            return "$thread   $time   $msg"
        }

        fun toJson(): JSONObject {
            return JSONObject()
                .put("tag", tag)
                .put("msg", msg)
                .put("time", time)
                .put("thread", thread)
                .put("method", method)
        }
    }

    class WriteRunnable(private val list: List<LogInfo>) : Runnable {
        private val sep = "日志工具类".toByteArray(Charsets.UTF_8)
        override fun run() {
            try {
                val ja = JSONArray()
                for (info in list) {
                    ja.put(info.toJson())
                }
                val strBytes = UtilsEncrypt.zipString(ja.toString()).toByteArray(Charsets.UTF_8) + sep
                val mappedByteBuffer = accessFileChannel.map(FileChannel.MapMode.READ_WRITE, logFile.length(), strBytes.size.toLong())
                mappedByteBuffer.put(strBytes)
                mappedByteBuffer.force()
            } catch (e: Exception) {
                Log.e("LogError", "写 Log 文件出错: ", e)
            }
        }
    }
}

// 崩溃异常捕获
internal class UtilsCrash: Thread.UncaughtExceptionHandler {
    private val mUIThreadId = Thread.currentThread().id
    private val mHandler = Thread.getDefaultUncaughtExceptionHandler()  // 系统默认的 UncaughtException 处理类

    fun initCrash() {
        Thread.setDefaultUncaughtExceptionHandler(this)                 // 设置该 CrashHandler 为程序的默认处理器
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        val out = ByteArrayOutputStream()
        try {
            e.printStackTrace(PrintStream(out))
            UtilsLog.crash("Crash ## $e  cause: $out")
        } catch (e1: Exception) {
            e1.printStackTrace()
            UtilsLog.crash("Crash Error ## $e1  ## $e")
        } finally {
            out.close()
        }

        if (t.id != mUIThreadId &&
            e.stackTrace.isNotEmpty() &&
            e.stackTrace[0].toString().contains("com.google.android.gms") &&
            e.message != null &&
            e.message!!.contains("Results have already been set")
        ) {
            return                                                      // non-UI thread
        }

        if (mHandler != null && UtilsLifecycle.isFrontLiveData.value!!) {
            mHandler.uncaughtException(t, e)                            // 系统处理方式
        } else {
            Process.killProcess(Process.myPid())
            exitProcess(1)                                              // java 虚拟机退出，结束程序
        }
    }
}