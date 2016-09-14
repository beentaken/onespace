package com.sesame.onespace.utils;

import com.sesame.onespace.constant.Constant;

/**
 * Created by chongos on 10/22/15 AD.
 */
public class Log {

    public static void i(String msg) {
        if(Constant.DEBUG)
            android.util.Log.i(Constant.LOG_TAG, msg);
    }

    public static void i(String msg, Exception e) {
        if(Constant.DEBUG)
            android.util.Log.i(Constant.LOG_TAG, msg, e);
    }

    public static void e(String msg) {
        android.util.Log.e(Constant.LOG_TAG, msg);
    }

    public static void e(String msg, Exception e) {
        android.util.Log.e(Constant.LOG_TAG, msg, e);
    }

    public static void w(String msg) {
        android.util.Log.w(Constant.LOG_TAG, msg);
    }

    public static void w(String msg, Exception e) {
        android.util.Log.w(Constant.LOG_TAG, msg, e);
    }

    public static void d(String msg) {
        if(Constant.DEBUG)
            android.util.Log.d(Constant.LOG_TAG, msg);
    }

    public static void d(String msg, Exception e) {
        if(Constant.DEBUG)
            android.util.Log.d(Constant.LOG_TAG, msg, e);
    }

}
