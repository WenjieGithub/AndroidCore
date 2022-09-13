package love.nuoyan.android.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation

object UtilsAnimation {
    /**
     * 从控件的顶部移动到控件所在位置，
     * @param Duration 动画时间
     */
    fun topMoveToViewLocation(v: View, Duration: Long = 500) {
        v.visibility = View.VISIBLE
        val showAction = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f
        )
        showAction.duration = Duration
        v.clearAnimation()
        v.animation = showAction
    }

    /**
     * 从控件所在位置移动到控件的顶部
     * @param Duration 动画时间
     */
    fun moveToViewTop(v: View, Duration: Long = 500) {
        val hiddenAction = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f
        )
        hiddenAction.duration = Duration
        v.clearAnimation()
        v.animation = hiddenAction
        hiddenAction.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) { v.visibility = View.GONE }
            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    /**
     * 从控件的底部移动到控件所在位置
     * @param Duration 动画时间
     */
    fun bottomMoveToViewLocation(v: View, Duration: Long = 500) {
        v.visibility = View.VISIBLE
        val showAction = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f
        )
        showAction.duration = Duration
        v.clearAnimation()
        v.animation = showAction
    }

    /**
     * 从控件所在位置移动到控件的底部
     * @param Duration 动画时间
     */
    fun moveToViewBottom(v: View, Duration: Long = 500) {
        val hiddenAction = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 1.0f
        )
        hiddenAction.duration = Duration
        v.clearAnimation()
        v.animation = hiddenAction
        hiddenAction.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) { v.visibility = View.GONE }
            override fun onAnimationRepeat(animation: Animation) {}
        })
    }
}