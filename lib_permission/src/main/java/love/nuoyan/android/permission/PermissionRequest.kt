package love.nuoyan.android.permission

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

/**
 * 权限请求
 */
class PermissionRequest(fragmentManager: FragmentManager, onResult: (result: Result<List<PermissionInfo>>) -> Unit) {
    constructor(fragment: Fragment, onResult: (result: Result<List<PermissionInfo>>) -> Unit) : this(fragment.childFragmentManager, onResult)

    constructor(activity: FragmentActivity, onResult: (result: Result<List<PermissionInfo>>) -> Unit) : this(activity.supportFragmentManager, onResult)

    companion object {
        var logCallback: ((msg: String) -> Unit)? = null
    }

    private var mInit = false
    private lateinit var mFragment: PermissionFragment              // 请求动态权限的 Fragment

    init {
        try {
            mFragment = PermissionFragment(onResult)
            fragmentManager.beginTransaction()
                .add(mFragment, "PermissionFragment(${System.currentTimeMillis()})")
                .commitNow()
            mInit = true
            logCallback?.let { it("Permission ## ${mFragment.tag}  add this fragment") }
        } catch (e: Exception) {
            val msg = "${mFragment.tag}  (error) add this fragment: $e"
            logCallback?.let { it("Permission ## $msg") }
            onResult(Result.failure(RuntimeException(msg)))
        }
    }

    /**
     * 请求权限
     *
     * @param permissions   权限数组
     * @return LiveData 权限信息列表
     */
    fun applyPermission(vararg permissions: String) {
        if (mInit) {
            require(permissions.isNotEmpty()) { "permissions no nulls allowed..." }
            mFragment.commitPermission(*permissions)
        }
    }

    /**
     * 判断是否有某个权限
     *
     * @param permission 权限名称
     * @return 是否有某个存储权限
     */
    fun hasPermission(permission: String): Boolean? {
        return if (mInit) {
            val has = mFragment.hasPermission(permission)
            mFragment.removeThis()
            has
        } else {
            null
        }
    }
}