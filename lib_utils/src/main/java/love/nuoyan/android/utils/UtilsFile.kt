package love.nuoyan.android.utils

import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object UtilsFile {
    /** 获取应用存储目录
     *  * @param type The type of files directory to return. May be {@code null}
     *            for the root of the files directory or one of the following
     *            constants for a subdirectory:
     *            {@link android.os.Environment#DIRECTORY_MUSIC},
     *            {@link android.os.Environment#DIRECTORY_PODCASTS},
     *            {@link android.os.Environment#DIRECTORY_RINGTONES},
     *            {@link android.os.Environment#DIRECTORY_ALARMS},
     *            {@link android.os.Environment#DIRECTORY_NOTIFICATIONS},
     *            {@link android.os.Environment#DIRECTORY_PICTURES}, or
     *            {@link android.os.Environment#DIRECTORY_MOVIES}.
     */
    fun getAppDir(type: String?): File? {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            Utils.appContext.getExternalFilesDir(type)
        } else {
            Utils.appContext.filesDir
        }
    }

    /** 获取应用缓存目录 */
    fun getAppCacheDir(): File? {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            Utils.appContext.externalCacheDir
        } else {
            Utils.appContext.cacheDir
        }
    }

    fun createNewFile(file: File) {
        if (!file.exists()) {
            file.parentFile?.let { mkdir(it) }
            file.createNewFile()
        }
    }

    fun mkdir(file: File) {
        if (!file.exists()) {
            val parent = file.parentFile
            if (parent != null && !parent.exists()) {
                mkdir(parent)
            }
            file.mkdir()
        }
    }

    /**
     * 获取空间总大小, 可用空间大小
     */
    fun getFileSystemSize(): Pair<Long, Long>? {
        return getAppCacheDir()?.let {
            val sf = StatFs(it.absolutePath)
            Pair(sf.totalBytes, sf.availableBytes)
//            val totalSize = sf.totalBytes                                 // 总大小
//            val availableSize = sf.availableBytes                         // 可用大小
//            Formatter.formatFileSize(Utils.appContext, sf.availableBytes) // 格式化可用大小
        }
    }

    /**
     * 获取文件或文件夹大小
     */
    fun getTotalSizeOfFile(file: File): Long {
        var total = 0L
        if (file.exists()) {
            if (file.isDirectory) {
                file.listFiles()?.forEach {
                    total += getTotalSizeOfFile(it)
                }
            } else {
                total += file.length()
            }
        }
        return total
    }

    /**
     * 删除文件，可以是文件或文件夹
     * @param delFile 要删除的文件夹或文件名
     * @return 删除成功返回true，否则返回false
     */
    fun delete(delFile: String): Boolean {
        val file = File(delFile)
        return if (file.exists()) {
            if (file.isFile) deleteSingleFile(delFile) else deleteDirectory(delFile)
        } else {
            false
        }
    }

    /**
     * 删除单个文件
     * @param filePathName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    fun deleteSingleFile(filePathName: String): Boolean {
        val file = File(filePathName)
        return if (file.exists() && file.isFile) {
            file.delete()
        } else {
            false
        }
    }

    /**
     * 删除目录及目录下的文件
     * @param filePath 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    fun deleteDirectory(filePath: String): Boolean {
        // 如果 dir 不以文件分隔符结尾，自动添加文件分隔符
        var dirPath = filePath
        if (!filePath.endsWith(File.separator)) dirPath = filePath + File.separator
        val dirFile = File(dirPath)

        // 如果 dir 对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory) {
            return false
        }
        var flag = true
        // 删除文件夹中的所有文件包括子目录
        dirFile.listFiles()?.let {
            for (file in it) {
                // 删除子文件
                if (file.isFile) {
                    flag = deleteSingleFile(file.absolutePath)
                    if (!flag) break
                } else if (file.isDirectory) {
                    flag = deleteDirectory(file.absolutePath)
                    if (!flag) break
                }
            }
        }
        if (!flag) {
            return false
        }
        // 删除当前目录
        return dirFile.delete()
    }

    /**
     * 删除目录下的文件
     * @param filePath 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    fun deleteDirectoryChildFile(filePath: String): Boolean {
        // 如果 dir 不以文件分隔符结尾，自动添加文件分隔符
        var dirPath = filePath
        if (!filePath.endsWith(File.separator)) dirPath = filePath + File.separator
        val dirFile = File(dirPath)

        // 如果 dir 对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory) {
            return false
        }
        var flag = true
        // 删除文件夹中的所有文件包括子目录
        dirFile.listFiles()?.let {
            for (file in it) {
                // 删除子文件
                if (file.isFile) {
                    flag = deleteSingleFile(file.absolutePath)
                    if (!flag) break
                } else if (file.isDirectory) {
                    flag = deleteDirectory(file.absolutePath)
                    if (!flag) break
                }
            }
        }
        return flag
    }

    /** 压缩文件或文件夹 */
    fun zip(srcFileString: String, zipFileString: String) {
        try {
            zip(File(srcFileString), File(zipFileString))
        } catch (e: Exception) {
            UtilsLog.logW("zipFolder: ${e.stackTraceToString()}")
        }
    }
    /** 压缩文件或文件夹 */
    fun zip(srcFile: File, zipFile: File) {
        try {
            val outZip = ZipOutputStream(FileOutputStream(zipFile))     // 创建ZIP
            zipFiles(srcFile, outZip)                                   // 压缩
            outZip.finish()                                             // 完成和关闭
            outZip.close()
        } catch (e: Exception) {
            UtilsLog.logW("zipFolder: ${e.stackTraceToString()}")
        }
    }

    private fun zipFiles(file: File, zipOutputSteam: ZipOutputStream) {
        try {
            if (file.isFile) {
                val zipEntry = ZipEntry(file.name)
                val inputStream = FileInputStream(file)
                zipOutputSteam.putNextEntry(zipEntry)
                var len: Int
                val buffer = ByteArray(4096)
                while (inputStream.read(buffer).also { len = it } != -1) {
                    zipOutputSteam.write(buffer, 0, len)
                }
                zipOutputSteam.closeEntry()
            } else {
                val fileList = file.listFiles()                                         // 文件夹
                if (fileList.isNullOrEmpty()) {                                         // 没有子文件和压缩
                    val zipEntry = ZipEntry(file.name + File.separator)
                    zipOutputSteam.putNextEntry(zipEntry)
                    zipOutputSteam.closeEntry()
                } else {                                                                // 子文件和递归
                    for (f in fileList) {
                        zipFiles(f, zipOutputSteam)
                    }
                }
            }
        } catch (e: Exception) {
            UtilsLog.logW("zipFiles: ${e.stackTraceToString()}")
        }
    }

    fun getUri(fileName: String): Uri? {
        return try {
            val file = File(fileName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(Utils.appContext, Utils.appContext.packageName + ".fileProvider", file)
            } else {
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            UtilsLog.logW("UtilsFile: ${e.stackTraceToString()}")
            null
        }
    }
    fun getUri(file: File): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(Utils.appContext, Utils.appContext.packageName + ".fileProvider", file)
            } else {
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            UtilsLog.logW("UtilsFile: ${e.stackTraceToString()}")
            null
        }
    }
}