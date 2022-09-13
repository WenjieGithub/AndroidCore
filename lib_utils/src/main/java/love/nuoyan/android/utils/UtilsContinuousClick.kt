package love.nuoyan.android.utils

object UtilsContinuousClick {
    private const val MIN_DELAY_TIME = 1000
    private var lastClickTime: Long = 0

    fun isNotFastClick(): Boolean {
        val currentClickTime = System.currentTimeMillis()
        return if (currentClickTime - lastClickTime >= MIN_DELAY_TIME) {
            lastClickTime = currentClickTime
            true
        } else {
            false
        }
    }
}