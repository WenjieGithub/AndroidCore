package love.nuoyan.android.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object UtilsClipboard {
    // 获取系统剪贴板内容
    fun getClipContent(): String? {
        val manager = Utils.appContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if (manager?.hasPrimaryClip() == true) {
            val clipData = manager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val addedText = clipData.getItemAt(0)?.text
                if (!addedText.isNullOrEmpty()) {
                    return addedText.toString()
                }
            }
        }
        return null
    }

    // 清空剪贴板内容
    fun clearClipboard() {
        (Utils.appContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.let { manager ->
            try {
                manager.primaryClip?.let { manager.setPrimaryClip(it) }
                manager.setPrimaryClip(ClipData.newPlainText("",""))
            } catch (e: Exception) {
                UtilsLog.log(e.stackTraceToString())
            }
        }
    }

    /**
     * 实现文本复制功能
     *
     * @param content 复制的文本
     */
    fun copy2Clipboard(content: String) {
        (Utils.appContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.setPrimaryClip(ClipData.newPlainText(null, content))
    }
}