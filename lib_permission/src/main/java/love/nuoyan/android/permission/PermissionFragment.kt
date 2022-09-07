package love.nuoyan.android.permission

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment

/** 申请权限的fragment */
class PermissionFragment(val onResult: (result: Result<List<PermissionInfo>>) -> Unit) : Fragment() {
    private val requestPermissionCode = 9903                                                        // 权限的请求码
    private val mInfoList = arrayListOf<PermissionInfo>()

    private fun log(msg: String) {
        PermissionRequest.logCallback?.let { it("Permission ## $tag  $msg") }
    }

    private fun onSuccess(permissions: ArrayList<PermissionInfo>, msg: String? = null) {
        msg?.let { log(it) }
        removeThis()
        onResult(Result.success((permissions)))
    }

    private fun onFailed(msg: String) {
        log(msg)
        removeThis()
        onResult(Result.failure(RuntimeException(msg)))
    }

    /**
     * 提交要申请的权限, 此方法会提取出未授权的权限与已授权的权限，只对未授权的权限进行申请
     * @param permissions 权限数组
     */
    fun commitPermission(vararg permissions: String) {
        try {
            checkRegisteredPermissionInManifest(*permissions)
            mInfoList.clear()
            if (Build.VERSION.SDK_INT < 23) {                                                       // 6.0 以下 无法检测到权限
                for (permission in permissions) {
                    if (!TextUtils.isEmpty(permission)) {
                        mInfoList.add(PermissionInfo(permission, State.Unhandled))
                    }
                }
                onSuccess(mInfoList,"6.0 以下 无法检测到权限: $permissions")
            } else {
                val unGrantedPermissionsList = arrayListOf<String>()
                for (permission in permissions) {
                    if (!TextUtils.isEmpty(permission)) {
                        if (hasPermission(permission)) {
                            mInfoList.add(PermissionInfo(permission, State.Granted))
                            log("授予了权限: $permission")
                        } else {
                            unGrantedPermissionsList.add(permission)
                        }
                    }
                }
                if (unGrantedPermissionsList.isEmpty()) {
                    onSuccess(mInfoList)
                } else {
                    applyDynamicPermissions(*unGrantedPermissionsList.toTypedArray())               // 开始请求权限
                }
            }
        } catch (e: Exception) {
            onFailed("(error) commitPermission($permissions): $e")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            activity?.let {         // 页面已关闭不处理结果
                if (!it.isFinishing && requestPermissionCode == requestCode && permissions.size == grantResults.size) {
                    for (i in permissions.indices) {
                        val grant = grantResults[i]                             // 授予状态
                        val permission = permissions[i]                         // 权限名称
                        val permissionInfo = PermissionInfo(permission)         // 权限信息
                        when {
                            grant == PackageManager.PERMISSION_GRANTED -> {     // 如果授予权限
                                permissionInfo.state = State.Granted
                                log("授予了权限: $permission")
                            }
                            isSelectedNoTips(permission) -> {
                                permissionInfo.state = State.DeniedAndNoPrompt
                                log("拒绝了权限且不在提示: $permission")
                            }
                            else -> {
                                permissionInfo.state = State.Denied
                                log("拒绝了权限: $permission")
                            }
                        }
                        mInfoList.add(permissionInfo)
                    }
                    onSuccess(mInfoList)
                } else {
                    onFailed("(error) onRequestPermissionsResult($requestCode, $permissions, $grantResults)")
                }
            }
        } catch (e: Exception) {
            onFailed("(error) onRequestPermissionsResult($requestCode, $permissions, $grantResults): $e")
        }
    }

    // 是否拥有某个权限
    internal fun hasPermission(permission: String): Boolean {
        return try {
            Build.VERSION.SDK_INT < 23 || context?.let {
                PermissionChecker.checkSelfPermission(it, permission)
            } == PermissionChecker.PERMISSION_GRANTED
        } catch (e: Exception) {
            log("(error) hasPermission($permission): $e")
            false
        }
    }

    // 请求权限
    @TargetApi(23)
    private fun applyDynamicPermissions(vararg permissions: String) {
        requestPermissions(permissions, requestPermissionCode)
    }

    // 检查要请求的权限在 manifest 中是否注册
    private fun checkRegisteredPermissionInManifest(vararg permissions: String) {
        if (context != null) {
            val requestedPermissions = context?.packageManager?.getPackageInfo(
                requireContext().packageName, PackageManager.GET_PERMISSIONS)?.requestedPermissions
            if (requestedPermissions != null) {
                require(!listOf(requestedPermissions).containsAll(listOf(permissions))) {
                    "please register the permissions in manifest..."
                }
            } else {
                require(permissions.isNotEmpty()) { "please register the permissions in manifest..." }
            }
        } else {
            log("(error) checkRegisteredPermissionInManifest($permissions): context=null")
        }
    }

    // 这个权限是否勾选了不在提示, false 没有勾选不在提示，只是拒绝了某个权限
    private fun isSelectedNoTips(permission: String): Boolean {
        return if (activity != null) {
            !ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)
        } else {
            log("(error) isSelectedNoTips($permission): activity=null")
            false
        }
    }

    // 删除这个 fragment 在 manager 中
    internal fun removeThis() {
        try {
            parentFragmentManager.beginTransaction().remove(this).commitNow()
//            fragmentManager?.beginTransaction()?.remove(this)?.commitNow()
            log("remove this fragment")
        } catch (e: Exception) {
            log("(error) remove this fragment: $e")
        }
    }
}