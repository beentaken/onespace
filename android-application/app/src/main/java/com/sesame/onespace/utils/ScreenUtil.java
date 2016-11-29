package com.sesame.onespace.utils;

import android.content.Context;

/**
 * Created by chongos on 11/12/15 AD.
 */
public class ScreenUtil {

    public static float dpToPx(Context context, float dp) {
        if (context == null) {
            return -1;
        }
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static float pxToDp(Context context, float px) {
        if (context == null) {
            return -1;
        }
        return px / context.getResources().getDisplayMetrics().density;
    }

}
