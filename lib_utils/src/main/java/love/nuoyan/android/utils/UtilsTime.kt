package love.nuoyan.android.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * 描述: 时间工具类
 */
object UtilsTime {
    private val TimeFormat = SimpleDateFormat("HH:mm:ss", Locale.PRC)
    private val DateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.PRC)
    private val DateTimeFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.PRC)

    // 获取当前时间
    val currentTime: String
        get() = TimeFormat.format(Date())

    // 获取当前日期
    val currentDate: String
        get() = DateFormat.format(Date())

    // 获取当前日期时间
    val currentDateTime: String
        get() = DateTimeFormat.format(Date())       // System.currentTimeMillis()

    // 获取过去第几天的日期
    fun getPastDate(past: Int): String {
        val calendar = Calendar.getInstance()
        calendar[Calendar.DAY_OF_YEAR] = calendar[Calendar.DAY_OF_YEAR] - past
        val today = calendar.time
        return DateFormat.format(today)
    }
}
