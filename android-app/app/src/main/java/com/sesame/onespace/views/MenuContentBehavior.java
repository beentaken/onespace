package com.sesame.onespace.views;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;

import com.sesame.onespace.utils.ScreenUtil;

/**
 * Created by chongos on 11/12/15 AD.
 */
public class MenuContentBehavior extends CoordinatorLayout.Behavior<RelativeLayout> {

    private Context mContext;
    private int toolbarHeight;
    private float overlapTop;

    public MenuContentBehavior(Context context, AttributeSet attrs) {
        mContext = context;

        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        toolbarHeight = context.getResources().getDimensionPixelSize(tv.resourceId);
        overlapTop = ScreenUtil.dpToPx(context, 30);
    }


    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, RelativeLayout child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, RelativeLayout child, View dependency) {
        float currentAppBarHeight = dependency.getHeight() + dependency.getY();
        float overlapTopPercent = (currentAppBarHeight - toolbarHeight) / dependency.getHeight();

        if(currentAppBarHeight <= toolbarHeight)
            child.setY(toolbarHeight);
        else
            child.setY(dependency.getHeight() + dependency.getY() - overlapTop * overlapTopPercent);

        return true;
    }


}

