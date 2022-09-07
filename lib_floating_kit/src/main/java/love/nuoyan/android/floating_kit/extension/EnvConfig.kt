package love.nuoyan.android.floating_kit.extension

class EnvConfig(val array: Array<String>, var currentEnv: String) {
    private val callbackList = mutableListOf<(env: String) -> Unit>()

    fun addCallback(callback: (env: String) -> Unit) {
        callbackList.add(callback)
        callback(currentEnv)
    }

    fun onCallback(env: String) {
        currentEnv = env
        for (callback in callbackList) {
            callback(currentEnv)
        }
    }
}