package love.nuoyan.android.widgets.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

/**
 * 依赖于 AppBarLayout 底部控件向下滑动 Behavior
 */
public class BottomBarBehavior extends CoordinatorLayout.Behavior<View> {
    public BottomBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 确定所提供的子视图是否有另一个特定的同级视图作为布局从属。说明这个子控件是依赖AppBarLayout的
    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        return dependency instanceof AppBarLayout;
    }

    // 用于响应从属布局的变化
    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        float translationY = Math.abs(dependency.getTop());                                         // 获取跟随布局的顶部位置
        child.setTranslationY(translationY);
        return true;
    }
}
