package love.nuoyan.android.permission


/** 权限的实体类：权限名称; 权限状态,默认为被拒绝的权限; */
data class PermissionInfo(
    val name: String,
    var state: State = State.Denied
) {
    /** 是否授予了某项权限, 6.0 以下也默认为已经有权限 */
    val isGranted: Boolean
        get() = state == State.Granted || state == State.Unhandled

    /** 是否显示请求权限的合理描述, 用户勾选了不在提示, 给用户一个友好的提示 */
    val isShowRequestRational: Boolean
        get() = state == State.DeniedAndNoPrompt
}

/** 权限状态 */
enum class State {
    /** 授予了权限 */
    Granted,
    /** 拒绝了权限 */
    Denied,
    /** 拒绝了权限并勾选不在提示 */
    DeniedAndNoPrompt,
    /** 不能处理的权限(Android 6.0 以下手机，请用try catch 捕获无权限的异常) */
    Unhandled;
}