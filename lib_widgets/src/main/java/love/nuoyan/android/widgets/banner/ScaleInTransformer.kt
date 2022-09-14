package love.nuoyan.android.widgets.banner

import android.view.View
import androidx.viewpager2.widget.ViewPager2

class ScaleInTransformer(var mMinScale: Float = 0.85f) : ViewPager2.PageTransformer {
    private val defCenter = 0.5f

    override fun transformPage(view: View, position: Float) {
        val pageWidth = view.width
        val pageHeight = view.height
        view.pivotY = (pageHeight shr 1).toFloat()
        view.pivotX = (pageWidth shr 1).toFloat()
        if (position < -1) {                    // [-Infinity,-1)  This page is way off-screen to the left.
            view.scaleX = mMinScale
            view.scaleY = mMinScale
            view.pivotX = pageWidth.toFloat()
        } else if (position <= 1) {             // [-1,1]   Modify the default slide transition to shrink the page as well
            if (position < 0) {                 // 1-2:1[0,-1] ;2-1:1[-1,0]
                val scaleFactor = (1 + position) * (1 - mMinScale) + mMinScale
                view.scaleX = scaleFactor
                view.scaleY = scaleFactor
                view.pivotX = pageWidth * (defCenter + defCenter * -position)
            } else {                            // 1-2:2[1,0] ;2-1:2[0,1]
                val scaleFactor = (1 - position) * (1 - mMinScale) + mMinScale
                view.scaleX = scaleFactor
                view.scaleY = scaleFactor
                view.pivotX = pageWidth * ((1 - position) * defCenter)
            }
        } else {                                // (1,+Infinity]
            view.pivotX = 0f
            view.scaleX = mMinScale
            view.scaleY = mMinScale
        }
    }
}