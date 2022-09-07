package love.nuoyan.android.floating_kit

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import love.nuoyan.android.floating_kit.ui.KitActivity
import love.nuoyan.android.floating_kit.ui.KitLogFragment
import love.nuoyan.android.floating_kit.extension.EnvConfig
import love.nuoyan.android.floating_kit.extension.Extension
import love.nuoyan.android.floating_kit.extension.LogInfo
import love.nuoyan.android.floating_kit.extension.dip2px
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// 磁力吸附悬浮窗
class FloatingMagnetView private constructor(context: Context) : FrameLayout(context) {
    companion object {
        private val marginEdge = 16f.dip2px()                                           // 边缘距离
        private val mStatusBarHeight = Extension.getStatusBarHeight()                   // 状态栏高
        private lateinit var mFloatingView: FloatingMagnetView

        // 环境相关
        internal var envConfig: EnvConfig? = null
        // 日志相关
        internal val logList = arrayListOf<LogInfo>()
        internal val logLiveData = MutableLiveData<ArrayList<LogInfo>>()

        /** 初始化环境 环境描述数组 当前环境 */
        fun initEnvironment(envArray: Array<String>, currentEnv: String) {
            envConfig = EnvConfig(envArray, currentEnv)
            setFloatingViewInfo()
        }
        /** 环境选择后的回调, 表示选中的环境，应该保存 */
        fun addEnvironmentCallback(callback: (env: String) -> Unit) {
            envConfig?.addCallback(callback)
        }
        /** 初始化日志 */
        fun initLog(logTags: ArrayList<Pair<String, Int>>) {
            KitLogFragment.mTagList.clear()
            KitLogFragment.mTagList.add(Pair("全部", 0))
            KitLogFragment.mTagList.addAll(logTags)
        }
        /** 注入日志 */
        fun addLog(tag: String, msg: String, time: String = "", thread: String = "", method: String = "") {
            GlobalScope.launch(Dispatchers.Default) {
                synchronized(this) {
                    val info = LogInfo(tag, msg, time, thread, method)
                    if (logList.size > 1000) {
                        logList.removeFirstOrNull()
                    }
                    logList.add(info)
                    logLiveData.postValue(logList)
                }
            }
        }
        /** 添加扩展按钮 */
        fun addExtend(name: String, src: Int = R.drawable.lib_floating_kit_extend_icon, click: (activity: AppCompatActivity, view: View) -> Unit) {
            KitActivity.addItem(name, src, click)
        }

        fun init(application: Application) {
            Extension.application = application
            // View
            mFloatingView = FloatingMagnetView(application)
            mFloatingView.y = 500f
            val params = MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(marginEdge, mStatusBarHeight, marginEdge, 500)
            mFloatingView.layoutParams = params
            mFloatingView.bringToFront()
            mFloatingView.elevation = 10f
            setFloatingViewInfo()
            // 生命周期
            application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {
                    if (activity !is KitActivity) {
                        attach(activity)
                    }
                }
                override fun onActivityPaused(activity: Activity) {
                    if (activity !is KitActivity) {
                        detach()
                    }
                }
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {}
            })

            KitLogFragment.mTagList.addAll(
                arrayListOf(
                    Pair("Msg", ContextCompat.getColor(Extension.application, R.color.lib_floating_kit_log_msg)),
                    Pair("Error", ContextCompat.getColor(Extension.application, R.color.lib_floating_kit_log_error)),
                    Pair("Warn", ContextCompat.getColor(Extension.application, R.color.lib_floating_kit_log_warn)),
                    Pair("Debug", ContextCompat.getColor(Extension.application, R.color.lib_floating_kit_log_debug)),
                    Pair("Net", ContextCompat.getColor(Extension.application, R.color.lib_floating_kit_log_net)),
                    Pair("Component", ContextCompat.getColor(Extension.application, R.color.lib_floating_kit_log_component)),
                    Pair("Lifecycle", ContextCompat.getColor(Extension.application, R.color.lib_floating_kit_log_other))
                )
            )
        }

        @SuppressLint("SetTextI18n")
        fun setFloatingViewInfo() {
            val envView = mFloatingView.findViewById<TextView>(R.id.kit_floating_env)
            envView.visibility = if (envConfig == null) {
                GONE
            } else {
                envView.text = envConfig?.currentEnv
                VISIBLE
            }
            mFloatingView.findViewById<TextView>(R.id.kit_floating_version).text = "V ${Extension.getVersionName()}"
        }

        private fun attach(activity: Activity) {
            detach()
            (activity.window.decorView as? ViewGroup)?.addView(mFloatingView)
        }
        private fun detach() {
            (mFloatingView.parent as? ViewGroup)?.removeView(mFloatingView)
        }
    }

    private val mTouchTimeThreshold = 200                           // 触摸时间阈值
    private val mMoveAnimator = MoveAnimator()                      // 移动动画

    private var mOriginalRawX = 0f                                  // 手指屏幕原始坐标 x
    private var mOriginalRawY = 0f                                  // 手指屏幕原始坐标 x
    private var mOriginalX = 0f                                     // View 原始坐标 x
    private var mOriginalY = 0f                                     // View 原始坐标 x

    private var mLastTouchDownTime: Long = 0                        // 最后按下时间

    private var isNearestLeft = true                                // 左边最近

    private var mScreenWidth = Extension.getScreenWidth()           // 屏幕宽
    private var mScreenHeight = Extension.getScreenHeight()         // 屏幕高

    init {
        isClickable = true
        inflate(context, R.layout.lib_floating_kit_floating_layout, this)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        (parent as? ViewGroup)?.let {
            mScreenWidth = it.width
            mScreenHeight = it.height
            restrictedLocation()
        }
    }

    private fun restrictedLocation() {
        isNearestLeft = x < (mScreenWidth / 2)
        x = if (isNearestLeft) marginEdge.toFloat() else (mScreenWidth - marginEdge - width).toFloat()

        if (y < mStatusBarHeight) {
            y = mStatusBarHeight.toFloat() + marginEdge
        }
        if (y > mScreenHeight - height) {
            y = mScreenHeight - height.toFloat()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            var desY: Float
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    mOriginalX = x
                    mOriginalY = y
                    mOriginalRawX = event.rawX
                    mOriginalRawY = event.rawY
                    mLastTouchDownTime = System.currentTimeMillis()
                    mMoveAnimator.stop()
                }
                MotionEvent.ACTION_MOVE -> {
                    x = mOriginalX + event.rawX - mOriginalRawX
                    // 限制不可超出屏幕高度
                    desY = mOriginalY + event.rawY - mOriginalRawY
                    if (desY < mStatusBarHeight) {
                        desY = mStatusBarHeight.toFloat() + marginEdge
                    }
                    if (desY > mScreenHeight - height) {
                        desY = mScreenHeight - height.toFloat()
                    }
                    y = desY
                }
                MotionEvent.ACTION_UP -> {
                    if (System.currentTimeMillis() - mLastTouchDownTime < mTouchTimeThreshold) {
                        x = mOriginalX
                        y = mOriginalY
                        val intent = Intent(Extension.application, KitActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        Extension.application.startActivity(intent)
                    } else {
                        isNearestLeft = x < (mScreenWidth / 2)
                        val moveDistance = if (isNearestLeft) marginEdge else mScreenWidth - marginEdge - width
                        mMoveAnimator.start(moveDistance.toFloat(), y)
                    }
                }
            }
            return true
        }
        return false
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        val moveDistance = if (isNearestLeft) marginEdge else mScreenWidth - marginEdge - width
        mMoveAnimator.start(moveDistance.toFloat(), y)
    }

    private fun move(deltaX: Float, deltaY: Float) {
        x += deltaX
        y += deltaY
    }

    inner class MoveAnimator : Runnable {
        private val handler = Handler(Looper.getMainLooper())
        private var destinationX = 0f
        private var destinationY = 0f
        private var startingTime: Long = 0

        fun start(x: Float, y: Float) {
            destinationX = x
            destinationY = y
            startingTime = System.currentTimeMillis()
            handler.post(this)
        }

        fun stop() {
            handler.removeCallbacks(this)
        }

        override fun run() {
            if (rootView == null || rootView.parent == null) {
                return
            }
            val progress = 1f.coerceAtMost((System.currentTimeMillis() - startingTime) / 400f)
            val deltaX: Float = (destinationX - x) * progress
            val deltaY: Float = (destinationY - y) * progress
            move(deltaX, deltaY)
            if (progress < 1) {
                handler.post(this)
            }
        }
    }
}