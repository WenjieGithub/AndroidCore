package love.nuoyan.android.utils

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import java.lang.ref.WeakReference
import java.util.*

object UtilsLifecycle {
    // 应用是否是前台显示
    val isFrontLiveData = MutableLiveData(false)
    // 当前显示的 Activity, 弱引用, 有可能为空
    var currentActivity: WeakReference<Activity?> = WeakReference(null)
    // Activity 堆栈, 仅创建时加入, 销毁时移除
    val activityStack = Stack<Activity>()

    /** 注册状态监听，仅在Application中使用  */
    fun registerActivityLifecycle(application: Application, listener: LifecycleCallback) {
        application.registerActivityLifecycleCallbacks(listener)
    }

    fun unRegisterActivityLifecycle(application: Application, listener: LifecycleCallback) {
        application.unregisterActivityLifecycleCallbacks(listener)
    }
}

open class LifecycleCallback : FragmentManager.FragmentLifecycleCallbacks(), ActivityLifecycleCallbacks {
    private val logTag = "Lifecycle"
    private var activityStartCount = 0                                  // 打开的Activity数量统计

    open fun onFront(activity: Activity) {
        UtilsLifecycle.isFrontLiveData.postValue(true)
        UtilsLog.log("onFront(): ${activity.javaClass.name}", logTag)
    }
    open fun onBack() {
        UtilsLifecycle.isFrontLiveData.postValue(false)
        UtilsLog.log("onBack()", logTag)
        UtilsLog.finish()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is AppCompatActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(this, true)
        }
        UtilsLifecycle.activityStack.add(activity)
        UtilsLog.log("onActivityCreated(): ${activity.javaClass.name}", logTag)
    }
    override fun onActivityStarted(activity: Activity) {
        activityStartCount++
        if (activityStartCount == 1) {                                  // 数值从0变到1说明是从后台切到前台
            onFront(activity)
        }
        UtilsLog.log("onActivityStarted(): ${activity.javaClass.name}", logTag)
    }
    override fun onActivityResumed(activity: Activity) {
        UtilsLifecycle.currentActivity = WeakReference(activity)
        UtilsLog.log("onActivityResumed(): ${activity.javaClass.name}", logTag)
    }
    override fun onActivityPaused(activity: Activity) {
        UtilsLog.log("onActivityPaused(): ${activity.javaClass.name}", logTag)
    }
    override fun onActivityStopped(activity: Activity) {
        activityStartCount--
        if (activityStartCount == 0) {                                  // 数值从1到0说明是从前台切到后台
            onBack()
        }
        UtilsLog.log("onActivityStopped(): ${activity.javaClass.name}", logTag)
    }
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        UtilsLog.log("onActivitySaveInstanceState(): ${activity.javaClass.name}", logTag)
        UtilsLog.finish()
    }
    override fun onActivityDestroyed(activity: Activity) {
        UtilsLog.log("onActivityDestroyed(): ${activity.javaClass.name}", logTag)
        UtilsLog.finish()
        if (activity is AppCompatActivity) {
            activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(this)
        }
        UtilsLifecycle.activityStack.remove(activity)
    }


    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        super.onFragmentAttached(fm, f, context)
        UtilsLog.log("onFragmentAttached(): ${f.javaClass.name}", logTag)
    }
    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        super.onFragmentCreated(fm, f, savedInstanceState)
        UtilsLog.log("onFragmentCreated(): ${f.javaClass.name}", logTag)
    }
    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
        super.onFragmentViewCreated(fm, f, v, savedInstanceState)
        UtilsLog.log("onFragmentViewCreated(): ${f.javaClass.name}", logTag)
    }
    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        super.onFragmentStarted(fm, f)
        UtilsLog.log("onFragmentStarted(): ${f.javaClass.name}", logTag)
    }
    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        super.onFragmentResumed(fm, f)
        UtilsLog.log("onFragmentResumed(): ${f.javaClass.name}", logTag)
    }
    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        super.onFragmentPaused(fm, f)
        UtilsLog.log("onFragmentPaused(): ${f.javaClass.name}", logTag)
    }
    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        super.onFragmentStopped(fm, f)
        UtilsLog.log("onFragmentStopped(): ${f.javaClass.name}", logTag)
    }
    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        super.onFragmentSaveInstanceState(fm, f, outState)
        UtilsLog.log("onFragmentSaveInstanceState(): ${f.javaClass.name}", logTag)
    }
    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentViewDestroyed(fm, f)
        UtilsLog.log("onFragmentViewDestroyed(): ${f.javaClass.name}", logTag)
    }
    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        super.onFragmentDetached(fm, f)
        UtilsLog.log("onFragmentDetached(): ${f.javaClass.name}", logTag)
    }
    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentDestroyed(fm, f)
        UtilsLog.log("onFragmentDestroyed(): ${f.javaClass.name}", logTag)
    }
}