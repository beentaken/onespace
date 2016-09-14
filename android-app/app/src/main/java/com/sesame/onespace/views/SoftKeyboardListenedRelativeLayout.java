package com.sesame.onespace.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chongos on 8/13/15 AD.
 */
public class SoftKeyboardListenedRelativeLayout extends RelativeLayout {

    private boolean isKeyboardShown = false;
    private List<SoftKeyboardListener> lsners=new ArrayList<SoftKeyboardListener>();
    private float layoutMaxH = 0f;
    private static final float DETECT_ON_SIZE_PERCENT = 0.8f;

    public SoftKeyboardListenedRelativeLayout(Context context) {
        super(context);
    }

    public SoftKeyboardListenedRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("NewApi")
    public SoftKeyboardListenedRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int newH = MeasureSpec.getSize(heightMeasureSpec);
        if (newH > layoutMaxH) {
            layoutMaxH = newH;
        }
        if (layoutMaxH != 0f) {
            final float sizePercent = newH / layoutMaxH;
            if (!isKeyboardShown && sizePercent <= DETECT_ON_SIZE_PERCENT) {
                isKeyboardShown = true;
                for (final SoftKeyboardListener lsner : lsners) {
                    lsner.onSoftKeyboardShow();
                }
            } else if (isKeyboardShown && sizePercent > DETECT_ON_SIZE_PERCENT) {
                isKeyboardShown = false;
                for (final SoftKeyboardListener lsner : lsners) {
                    lsner.onSoftKeyboardHide();
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void addSoftKeyboardLsner(SoftKeyboardListener lsner) {
        lsners.add(lsner);
    }

    public void removeSoftKeyboardLsner(SoftKeyboardListener lsner) {
        lsners.remove(lsner);
    }

    public interface SoftKeyboardListener {
        void onSoftKeyboardShow();
        void onSoftKeyboardHide();
    }
}