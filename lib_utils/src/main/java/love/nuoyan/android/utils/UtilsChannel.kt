package love.nuoyan.android.utils

import com.meituan.android.walle.WalleChannelReader

object UtilsChannel {
    private lateinit var defaultChannel: String

    fun init(defaultChannel: String) {
        UtilsChannel.defaultChannel = defaultChannel
    }

    fun getChannel(): String {
        var channel = defaultChannel
        try {
            channel = WalleChannelReader.getChannel(Utils.appContext) ?: defaultChannel
        } catch (e: Exception) {
            UtilsLog.logW(e.stackTraceToString())
        }
        return channel
    }
}