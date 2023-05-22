package love.nuoyan.android.utils

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.lang.Runnable
import kotlin.system.exitProcess

import java.io.FileOutputStream
import java.util.zip.Deflater

/**
 * 日志工具类
 * Debug 日志 不会记录在日志文件内
 */
object UtilsLog {
    var logCallback: ((info: LogInfo) -> Unit)? = null

    private var isInit = false
    private var logList = mutableListOf<LogInfo>()

    private lateinit var logFile: File
    private lateinit var writerHandler: Handler

    internal fun init(context: Context, saveDay: Int = 2) {
        try {
            (if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                context.externalCacheDir
            } else {
                context.cacheDir
            })?.let {
                val handlerThread = HandlerThread("Log")
                handlerThread.start()
                writerHandler = Handler(handlerThread.looper)

                val dirFile = File(it, "log")
                if (!dirFile.exists() || !dirFile.isDirectory) {
                    UtilsFile.mkdir(dirFile)
                }
                logFile = File(dirFile, "Log_" + UtilsTime.currentDate.replace(".", "").replace(":", ""))
                if (!logFile.exists()) {
                    logFile.createNewFile()
                }
                isInit = true
                UtilsCrash().initCrash()

                Utils.appScope.launch(IO) {
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
            }
        } catch (e: Exception) {
            logE("UtilsLog ## $e")
        }
    }

    fun getLogFilePath(): String? {
        return try {
            logFile.absolutePath
        } catch (e: Exception) {
            logE("UtilsLog ## $e")
            null
        }
    }

    fun log(msg: String, tag: String = "Msg") {
        if (msg.isNotEmpty()) {
            val info = LogInfo(tag, msg)
            if (Utils.isDebug) {
                when (tag) {
                    "Error" -> Log.e("Cx$tag", info.msg)
                    "Warn" -> Log.w("Cx$tag", info.msg)
                    "Debug" -> Log.d("Cx$tag", info.msg)
                    else -> Log.i("Cx$tag", info.msg)
                }
                logCallback?.let { it(info) }
            }
            if (isInit && tag != "Debug") {
                logList.add(info)
                if (logList.size > 30) {
                    finish()
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
        val list = logList
        logList = mutableListOf()
        writerHandler.post(WriteRunnable(list))
    }

    internal fun crash(msg: String) {
        try {
            log(msg, "Crash")
            finish()
        } catch (e: Exception) {
            logE("写 Log 文件出错: ${e.stackTraceToString()}")
        }
    }

    class WriteRunnable(private val list: List<LogInfo>) : Runnable {
        private val sep = "l-o-g-".toByteArray()
        override fun run() {
            val fos = FileOutputStream(logFile, true)
            try {
                for (info in list) {
                    fos.write(zipBytes(info.toBytes()))
                    fos.write(sep)
                    fos.flush()
                }
            } catch (e: Exception) {
                logE( "写 Log 文件出错: ${e.stackTraceToString()}")
            } finally {
                fos.close()
            }
        }

        private fun zipBytes(bs: ByteArray): ByteArray {
            if (bs.isEmpty()) {
                return bs
            }
            val deflate = Deflater(Deflater.BEST_COMPRESSION)       // 使用指定的压缩级别创建一个新的压缩器。
            val out = ByteArrayOutputStream(256)
            return try {
                deflate.setInput(bs)                                // 设置压缩输入数据。
                deflate.finish()                                    // 当被调用时，表示压缩应该以输入缓冲区的当前内容结束。
                val bytes = ByteArray(256)
                while (!deflate.finished()) {                       // 压缩输入数据并用压缩数据填充指定的缓冲区。
                    val length = deflate.deflate(bytes)
                    out.write(bytes, 0, length)
                }
                out.toByteArray()
            } catch (e: Exception) {
                "压缩失败: ${e.stackTraceToString()}".toByteArray()
            } finally {
                deflate.end()
                out.close()
            }
        }
    }

    class LogInfo internal constructor(
        val tag: String,                                    // 标记
        val msg: String,                                    // 日志消息
        var time: String = UtilsTime.currentTime            // 调用时间
    ) {
        fun toBytes(): ByteArray {
            return "$tag-^-$time-^-".toByteArray() + msg.toByteArray()
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
                crash("$e  cause: $out")
            } catch (e1: Exception) {
                e1.printStackTrace()
                crash("$e1  ## $e")
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
                exitProcess(1)                                       // java 虚拟机退出，结束程序
            }
        }
    }
}